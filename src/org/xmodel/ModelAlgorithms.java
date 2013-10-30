/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelAlgorithms.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.zip.CRC32;
import org.xmodel.concurrent.MasterSlaveListener;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.SLog;
import org.xmodel.util.Fifo;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.AttributeNode;
import org.xmodel.xpath.PathElement;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.EqualityExpression;
import org.xmodel.xpath.expression.EqualityExpression.Operator;
import org.xmodel.xpath.expression.FilteredExpression;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.LiteralExpression;
import org.xmodel.xpath.expression.PathExpression;
import org.xmodel.xpath.expression.PredicateExpression;
import org.xmodel.xpath.expression.RootExpression;
import org.xmodel.xpath.function.CollectionFunction;


/**
 * This class encapsulates some common algorithms used in the model package.
 */
public class ModelAlgorithms implements IAxis
{
  /**
   * Find the best match for the specified object from the specified list of objects
   * using an algorithm does not require performing a diff. If the object being tested
   * has an non-zero length id, then the object is matched by id. Else if the object 
   * has a non-null value, then the object is matched by type and value. Else the
   * object with the longest matching ancestry is returned. Ancestors are compared 
   * using this same algorithm.
   * @param list The list of objects to be considered.
   * @param object The object to be matched.
   * @return Returns the best match for the specified object.   
   */
  public static IModelObject findFastMatch( List<IModelObject> list, IModelObject object)
  {
    IModelObject match = findFastSimpleMatch( list, object);
    if ( match != null) return match;

    // compare ancestry
    int maxDepth = 0;
    for( IModelObject node: list)
    {
      IModelObject ancestor1 = object;
      IModelObject ancestor2 = node;
      int depth = 0;
      while( ancestor1 != null && ancestor2 != null)
      {
        if ( !ancestor1.getType().equals( ancestor2.getType())) break;
        if ( !ancestor1.getAttribute( "id").equals( ancestor2.getAttribute( "id"))) break;
        ancestor1 = ancestor1.getParent();
        ancestor2 = ancestor2.getParent();
        depth++;
      }
      if ( maxDepth < depth)
      {
        maxDepth = depth;
        match = node;
      }
    }        
    
    return (maxDepth > 0)? match: null;
  }
  
  /**
   * Find the best match for the specified object from the specified list of objects
   * using an algorithm does not require performing a diff. If the object being tested
   * has an non-zero length id, then the object is matched by id. Else if the object 
   * has a non-null value, then the object is matched by type and value. Else if there
   * is a unique match by type in the list then it is returned.
   * @param list The list of objects to be considered.
   * @param object The object to be matched.
   * @return Returns null or the best match for the specified object.   
   */
  public static IModelObject findFastSimpleMatch( List<IModelObject> list, IModelObject object)
  {
    // look for exact match
    IModelObject referent = dereference( object);
    for( IModelObject node: list)
    {
      IModelObject nodeReferent = dereference( node);
      if ( referent == nodeReferent) return node;
    }
    
    // look for best match
    Object id = object.getAttribute( "id");
    if ( id != null)
    {
      for( IModelObject node: list)
        if ( node.getAttribute( "id").equals( id))
          return node;
    }
    else
    {
      Object value = object.getValue();
      if ( value != null)
      {
        for( IModelObject node: list)
        {
          Object nodeValue = node.getValue();
          if ( nodeValue != null && nodeValue.equals( value))
            return node;
        }
      }
      else
      {
        int matches = 0;
        IModelObject match = null;
        String type = object.getType();
        for( IModelObject node: list)
          if ( node.getType().equals( type))
          {
            match = node;
            if ( ++matches > 1) break;
          }
        if ( matches == 1) return match;
      }
    }
    
    return null;
  }
  
  /**
   * Find the best match for the specified object from the specified list of objects. 
   * The algorithm works in three stages. Firstly the list is narrowed down by type.
   * Secondly the remaining list is narrowed by id (if the object has an id). Thirdly 
   * each candidate is diffed against the object and a change-set is created. The object 
   * which has the fewest change records is considered the best match.
   * @param list The list of objects to be considered.
   * @param object The object to be matched.
   * @return Returns the best match for the specified object.
   */
  public static IModelObject findBestMatch( List<IModelObject> list, IModelObject object)
  {
    // narrow candidates by type
    String type = object.getType();
    List<IModelObject> candidates = new ArrayList<IModelObject>( list.size());
    for( IModelObject candidate: list)
      if ( candidate.getType().equals( type))
        candidates.add( candidate);
    
    // return single candidate
    if ( candidates.size() == 0) return null;
    if ( candidates.size() == 1) return candidates.get( 0);
    
    // narrow candidates by id
    Object id = object.getAttribute( "id");
    if ( id != null)
    {
      for( IModelObject candidate: candidates)
        if ( candidate.getAttribute( "id").equals( id))
          return candidate;
    }
    
    // diff candidates
    IModelObject minCandidate = null;
    int minRecords = Integer.MAX_VALUE;
    XmlDiffer differ = new XmlDiffer();
    for( IModelObject candidate: candidates)
    {
      ChangeSet changeSet = new ChangeSet();
      differ.diff( candidate, object, changeSet);
      if ( minRecords > changeSet.getSize())
      {
        minRecords = changeSet.getSize();
        minCandidate = candidate;
      }
    }
    
    return minCandidate;
  }
  
  /**
   * Find the common ancestor (if it exists) of the two objects specified.  If the objects
   * do not share an ancestor then return null.
   * @param object1 The first object.
   * @param object2 The second object.
   * @return Returns null or the common ancestor of the two objects.
   */
  public static IModelObject findCommonAncestor( IModelObject object1, IModelObject object2)
  {
    // find common ancestor
    IModelObject ancestor = null;
    
    // get this depth
    int thisDepth = 0;
    for ( IModelObject object = object1; object != null; object = object.getParent()) thisDepth++;
    thisDepth--;
    
    // get peer depth
    int peerDepth = 0;
    for ( IModelObject object = object2; object != null; object = object.getParent()) peerDepth++;
    peerDepth--;
    
    
    // start at the same level
    IModelObject lhs = object1;
    IModelObject rhs = object2;
    int startDepth = (peerDepth < thisDepth)? peerDepth: thisDepth;
    for ( int i=thisDepth; i>=startDepth; i--) lhs = lhs.getParent();
    for ( int i=peerDepth; i>=startDepth; i--) rhs = rhs.getParent();
    
    // search for common ancestor
    for ( int i=startDepth; i>=0; i--)
    {
      if ( lhs == rhs)
      {
        startDepth = i;
        ancestor = lhs;
        break;
      }
      lhs = lhs.getParent();
      rhs = rhs.getParent();
    }
    
    return ancestor;
  }
  
  /**
   * Create a path relative to the startObject argument which navigates to the endObject argument.
   * If a common ancestor cannot be found, then null is returned.  The algorithm returns the shortest
   * possible path.
   * @param startObject The starting object.
   * @param endObject The ending peer object.
   * @return Returns a peer path.
   */
  public static CanonicalPath createRelativePath( IModelObject startObject, IModelObject endObject)
  {
    // if objects are the same then...
    if ( startObject == endObject)
    {
      CanonicalPath peerPath = new CanonicalPath();
      peerPath.addElement( new PathElement( SELF));
      return peerPath;
    }
    
    // if endObject is ancestor of startObject
    if ( isAncestor( startObject, endObject))
    {
      CanonicalPath path = new CanonicalPath();
      while( startObject != endObject)
      {
        path.addElement( 0, new PathElement( PARENT));
        startObject = startObject.getParent();
      }
      return path;
    }
    
    // find common ancestor
    IModelObject ancestor = findCommonAncestor( startObject, endObject);
    
    // create section of path from ancestor to peer
    CanonicalPath path = new CanonicalPath();
    IPath identityPath = createIdentityPath( endObject);
    int pathIndex = identityPath.length();
    for ( IModelObject object = endObject; object != ancestor; object = object.getParent())
      path.addElement( 0, identityPath.getPathElement( --pathIndex));
        
    // if startObject is ancestor of endObject
    if ( isAncestor( endObject, startObject))
    {
      path.removeElement( 0);
    }
    else
    {
      // bail if there is no common ancestor
      if ( ancestor == null) return null;
      
      // create section of path from this object to ancestor
      while( startObject != ancestor)
      {
        path.addElement( 0, new PathElement( PARENT));
        startObject = startObject.getParent();
      }
    }
    
    return path;
  }

  /**
   * Create a deep copy of the subtree rooted at the given object.
   * @param object The root of the subtree to clone.
   * @return Returns a complete clone of the subtree.
   */
  public static IModelObject cloneTree( IModelObject object)
  {
    return cloneTree( object, null, null);
  }
  
  /**
   * Create a deep copy of the subtree rooted at the given object using the specified factory. If
   * the factory is null then a default factory is used.
   * @param object The root of the subtree to clone.
   * @param factory The factory to use when creating objects in the tree.
   * @param exclude Null or a list of descendants of the object to be excluded.
   * @return Returns a complete clone of the subtree.
   */
  public static IModelObject cloneTree( IModelObject object, IModelObjectFactory factory, Set<IModelObject> exclude)
  {
    IModelObject thisDup = (factory == null)? object.cloneObject(): factory.createClone( object);
    Fifo<IModelObject> fifo = new Fifo<IModelObject>();
    fifo.push( object);
    fifo.push( thisDup);
    while( !fifo.empty())
    {
      IModelObject source = (IModelObject)fifo.pop();
      IModelObject sourceDup = (IModelObject)fifo.pop();
      List<IModelObject> children = source.getChildren();
      for ( int i=0; i<children.size(); i++)
      {
        IModelObject child = (IModelObject)children.get( i);
        if ( exclude == null || !exclude.contains( child))
        {
          IModelObject childDup = (factory == null)? child.cloneObject(): factory.createClone( child);
          if ( childDup != null)
          {
            sourceDup.addChild( childDup);
            fifo.push( child);
            fifo.push( childDup);
          }
        }
      }
    }
    return thisDup;
  }
  
//  /**
//   * Create a clone of the specified object. If the object has a caching policy, then the caching policy
//   * is cloned and the new element will be left in the dirty state.
//   * @param object The root of the subtree.
//   * @param factory Null or the factory to use for elements that are not IExternalReference instances.
//   * @return Returns the clone.
//   */
//  public static IModelObject cloneExternalObject( IModelObject object, IModelObjectFactory factory)
//  {
//    ReferenceFactory referenceFactory = new ReferenceFactory();
//    referenceFactory.delegate = factory;
//    IExternalReference clone = (IExternalReference)referenceFactory.createClone( object);
//    clone.setDirty( true);
//    return clone;
//  }
  
  /**
   * Create a deep copy of the specified subtree. External references present in the tree are cloned
   * as ExternalReferences and their caching policies and dirty state are preserved.
   * @param object The root of the subtree.
   * @param factory Null or the factory to use for elements that are not IExternalReference instances.
   * @return Returns the clone.
   */
  public static IModelObject cloneExternalTree( IModelObject object, IModelObjectFactory factory)
  {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    referenceFactory.delegate = factory;
    
    IModelObject thisDup = (referenceFactory == null)? object.cloneObject(): referenceFactory.createClone( object);
    if ( object.isDirty()) return thisDup;
    
    Fifo<IModelObject> fifo = new Fifo<IModelObject>();
    fifo.push( object);
    fifo.push( thisDup);
    while( !fifo.empty())
    {
      IModelObject source = (IModelObject)fifo.pop();
      IModelObject sourceDup = (IModelObject)fifo.pop();
      List<IModelObject> children = source.getChildren();
      for ( int i=0; i<children.size(); i++)
      {
        IModelObject child = (IModelObject)children.get( i);
        IModelObject childDup = (referenceFactory == null)? child.cloneObject(): referenceFactory.createClone( child);
        sourceDup.addChild( childDup);
        if ( !child.isDirty())
        {
          fifo.push( child);
          fifo.push( childDup);
        }
      }
    }
    return thisDup;
  }
  
  /**
   * Create a deep copy of the subtree rooted at the given object including clones of all ancestors.
   * @param object The branch point to be cloned.
   * @return Returns the clone of the specified object.
   */
  public static IModelObject cloneBranch( IModelObject object)
  {
    return cloneBranch( object, null);
  }
  
  /**
   * Create a deep copy of the subtree rooted at the given object including clones of all ancestors.
   * @param object The branch point to be cloned.
   * @param factory The factory to use when creating objects in the tree.
   * @return Returns the clone of the specified object.
   */
  public static IModelObject cloneBranch( IModelObject object, IModelObjectFactory factory)
  {
    // clone tree
    IModelObject clone = cloneTree( object, factory, null);
    
    // clone ancestors
    IModelObject child = clone;
    object = object.getParent();
    while( object != null)
    {
      IModelObject parent = (factory == null)? object.cloneObject(): factory.createClone( object);
      parent.addChild( child);
      child = parent;
      object = object.getParent();
    }
    
    return clone;
  }
  
  /**
   * Copy the attributes of the source object to the destination object.
   * @param source The source object whose attributes will be copied.
   * @param destination The destination object where the attributes will be copied.
   */
  public static void copyAttributes( IModelObject source, IModelObject destination)
  {
    Collection<String> attrNames = source.getAttributeNames();
    for( String attrName: attrNames)
    {
      // prevent unnecessary late id (id created after object is in model)
      Object sourceID = source.getAttribute( "id");
      Object destinationID = destination.getAttribute( "id");
      if ( attrName.equals( "id") && sourceID != null && destinationID != null && destinationID.equals( sourceID)) continue;
      Object attrValue = source.getAttribute( attrName);
      destination.setAttribute( attrName, attrValue);
    }
  }

  /**
   * Copy the children of the source object to the destination object using the specified factory.
   * @param source The source object whose children will be copied.
   * @param destination The destination object where the children will be copied.
   */
  public static void copyChildren( IModelObject source, IModelObject destination, IModelObjectFactory factory)
  {
    if ( factory == null) factory = new ModelObjectFactory();
    for ( IModelObject child: source.getChildren())
    {
      IModelObject clone = cloneTree( child, factory, null);
      destination.addChild( clone);
    }
  }
  
  /**
   * Move the children of the source object to the destination object.
   * @param source The source object whose children will be copied.
   * @param destination The destination object where the children will be copied.
   */
  public static void moveChildren( IModelObject source, IModelObject destination)
  {
    // copy list since children will be removed from original list when moved
    List<IModelObject> children = new ArrayList<IModelObject>( source.getChildren());
    for ( IModelObject child: children) destination.addChild( child);
  }
  
  /**
   * Remove the first argument from its parent and insert the second argument at the same index.
   * This method does nothing if the first argument does not have a parent.
   * @param original The original child.
   * @param replacement The replacement child.
   */
  public static void substitute( IModelObject original, IModelObject replacement)
  {
    IModelObject parent = original.getParent();
    if ( parent != null)
    {
      int index = parent.getChildren().indexOf( original);
      original.removeFromParent();
      parent.addChild( replacement, index);
    }
  }
  
  /**
   * Returns the identity path for the given object.  The identity path of an object is a path
   * which uniquely identifies location in the path leading to the object.  It includes an
   * IPathElement for each ancestor.
   * @return Returns the identity path for the given object.
   */
  public static CanonicalPath createIdentityPath( IModelObject object)
  {
    return createIdentityPath( object, false);
  }
  
  /**
   * Returns the identity path for the given object.  The identity path of an object is a path
   * which uniquely identifies location in the path leading to the object.  It includes an
   * IPathElement for each ancestor.
   * @param object The object.
   * @param useName True if name attribute should be used if present.
   * @return Returns the identity path for the given object.
   */
  public static CanonicalPath createIdentityPath( IModelObject object, boolean useName)
  {
    CanonicalPath path = new CanonicalPath();
    while( object != null)
    {
      IModelObject parent = object.getParent();
      
      int axis = (parent == null)? IAxis.ROOT: IAxis.CHILD;
      if ( object instanceof AttributeNode) axis = IAxis.ATTRIBUTE;
      
      String identAttrName = "id";
      
      if ( useName)
      {
        String name = Xlate.get( object, "name", (String)null);
        if ( name != null) identAttrName = "name";
      }
      
      String identAttrValue = Xlate.get( object, identAttrName, (String)null);
      if ( identAttrValue != null)
      {
        PredicateExpression predicate = new PredicateExpression( path);
        PathExpression identPath = new PathExpression( XPath.createPath( "@"+identAttrName));
        LiteralExpression literal = new LiteralExpression( path);
        literal.setValue( identAttrValue);
        EqualityExpression compare = new EqualityExpression( Operator.EQ);
        compare.addArgument( identPath);
        compare.addArgument( literal);
        predicate.addArgument( compare);
        IPathElement element = new PathElement( axis, object.getType(), predicate);
        path.addElement( 0, element);
      }
      else
      {
        List<IModelObject> children = Collections.emptyList();
        if ( parent != null) children = parent.getChildren( object.getType());
        if ( children.size() > 1 && !(object instanceof AttributeNode))
        {
          PredicateExpression predicate = new PredicateExpression( path);
          LiteralExpression position = new LiteralExpression();
          position.setValue( children.indexOf( object) + 1);
          predicate.addArgument( position);
          IPathElement element = new PathElement( axis, object.getType(), predicate);
          path.addElement( 0, element);
        }
        else
        {
          IPathElement element = new PathElement( axis, object.getType());
          path.addElement( 0, element);
        }
      }
      object = object.getParent();
    }
    return path;
  }
  
  /**
   * Returns an identity path (see above) which begins with a collection() function. 
   * If the root is not a collection, then the path will not return an element.
   * @param object The object whose identity path will be created.
   * @return Returns the identity path.
   */
  public static IExpression createIdentityExpression( IModelObject object)
  {
    IModelObject root = object.getRoot();
    String collection = root.getType();
    
    // create relative path
    IPath path = createRelativePath( root, object);
    
    // create expression
    CollectionFunction function = new CollectionFunction();
    function.addArgument( new LiteralExpression( collection));
    
    FilteredExpression filter = new FilteredExpression();
    filter.addArgument( function);
    filter.addArgument( new PathExpression( path));
    
    // return root expression
    return new RootExpression( filter);
  }
  
  /**
   * Returns the type path for the given object.  The type path of an object is a path
   * which identifies the type of each object in the path leading to the object.  It
   * includes an IPathElement for each ancestor.
   * @return Returns the type path for the given object.
   */
  public static CanonicalPath createTypePath( IModelObject object)
  {
    CanonicalPath path = new CanonicalPath();
    while( object != null)
    {
      int axis = (object.getParent() == null)? IAxis.ROOT: IAxis.CHILD;
      if ( object instanceof AttributeNode) axis = IAxis.ATTRIBUTE;
      IPathElement element = new PathElement( axis, object.getType());
      path.addElement( 0, element);
      object = object.getParent();
    }
    return path;
  }
  
  /**
   * Create a path which consists of elements 0 through pathIndex-1 of the specified path 
   * followed by the element pathIndex without its predicate.  This is useful for finding
   * all of the nodes which are candidates to the predicate at pathIndex.
   * @param path The path.
   * @param pathIndex An index of a path element with a predicate.
   * @return Returns the candidate path.
   */
  public static CanonicalPath createCandidatePath( IPath path, int pathIndex)
  {
    if ( pathIndex > 0)
    {
      CanonicalPath result = new CanonicalPath( path, 0, pathIndex);
      IPathElement element = path.getPathElement( pathIndex);
      result.addElement( new PathElement( element.axis(), element.type()));
      return result;
    }
    else
    {
      CanonicalPath result = new CanonicalPath();
      IPathElement element = path.getPathElement( pathIndex);
      result.addElement( new PathElement( element.axis(), element.type()));
      return result;
    }
  }
  
  /**
   * Create an array containing the index of each element in the path from start to end. 
   * @param start The starting ancestor element.
   * @param end The leaf of the index path.
   * @return Returns null or the index path.
   */
  public static int[] createIndexPath( IModelObject start, IModelObject end)
  {
    // count ancestors
    int count = 0;
    IModelObject ancestor = end;
    while( ancestor != null && ancestor != start)
    {
      count++;
      ancestor = ancestor.getParent();
    }
    
    // create index path
    int[] indices = new int[ count];
    for( int i=indices.length-1; i>=0; i--)
    {
      IModelObject parent = end.getParent();
      indices[ i] = parent.getChildren().indexOf( end);
      end = end.getParent();
    }
    
    return indices;
  }
  
  /**
   * Evaluate an index path created by calling the <code>createIndexPath</code> method.
   * @param root The root of the index path evaluation.
   * @param indices The array of indices.
   * @return Returns null or the leaf of the index path.
   */
  public static IModelObject evaluateIndexPath( IModelObject root, int[] indices)
  {
    if ( indices.length == 0) return root;
    for( int i=0; i<indices.length; i++)
    {
      root = root.getChild( indices[ i]);
      if ( root == null) return null;
    }
    return root;
  }
    
  /**
   * Returns the index path for the given object.  The index path includes positional predicates
   * for the object and each ancestor of the object except the root.  If the object argument is
   * an unresolved reference then null is returned.
   * @param object The object for which the index path will be created.
   * @return Returns the index path for the object or null.
   */
  public static String createIndexPathString( IModelObject object)
  {
    String type = object.getType();
    if ( object instanceof AttributeNode) type = "@"+type;
    
    List<String> steps = new ArrayList<String>();
    while( object != null)
    {
      String step = "/"+type;
      IModelObject parent = object.getParent();
      if ( parent != null)
      {
        int index = parent.getChildren().indexOf( object);
        if ( index < 0) return null;
        step = step+"["+Integer.toString( index)+"]";
      }
      steps.add( 0, step);
      object = parent;
      if ( object != null) type = object.getType();
    }
    
    StringBuilder builder = new StringBuilder();
    for( String step: steps) builder.append( step);
    return builder.toString();
  }
  
  /**
   * Returns a string representation of the given IPath which is similar to an XPath expression.
   * @param path The path to be converted to a string.
   * @return Returns a string representation of the given IPath.
   */
  public static String pathToString( IPath path)
  {
    StringBuffer buffer = new StringBuffer();
    for ( int i=0; i<path.length(); i++)
    {
      IPathElement element = path.getPathElement( i);
      if ( i>0) buffer.append( '/');
      buffer.append( element.toString());
    }
    return buffer.toString();
  }
  
  /**
   * Returns true if the given ancestor is an ancestor of the given object.
   * @param object The object being considered.
   * @param ancestor The ancestor being considered.
   * @return Returns true if the given ancestor is an ancestor of the given object.
   */
  public static boolean isAncestor( IModelObject object, IModelObject ancestor)
  {
    IModelObject parent = object.getParent();
    for ( ; parent != null; parent = parent.getParent())
      if ( parent == ancestor) 
        return true;
    return false;
  }
  
  /**
   * Calculate a tree hash for the subtree rooted at the specified object.  The hash includes the
   * type of each object and all attributes for all objects in the subtree.  It does not include
   * the name of the object since it is an alternative method of establishing identity.  Attribute
   * values are converted to strings before hashing.
   * @param root The root of the subtree to hash.
   * @return Returns a long hash value for the subtree.
   */
  public static long calculateTreeHash( IModelObject root)
  {
    CRC32 crc32 = new CRC32();
    BreadthFirstIterator treeIter = new BreadthFirstIterator( root);
    while( treeIter.hasNext())
    {
      IModelObject object = (IModelObject)treeIter.next();
      crc32.update( object.getType().getBytes());
      Iterator<String> attrIter = object.getAttributeNames().iterator();
      while( attrIter.hasNext())
      {
        String attrName = (String)attrIter.next();
        Object attrValue = object.getAttribute( attrName);
        crc32.update( attrName.getBytes());
        crc32.update( attrValue.toString().getBytes());
      }
    }
    return crc32.getValue();
  }
  
  /**
   * Compare the document order for the specified objects. Return -1 if the left-hand argument 
   * occurs earlier in the document than the right-hand operation. Return 0 if the objects are
   * the same object. If the objects do not share a common ancestor than 0 is returned.
   * @param lhs The left-hand object.
   * @param rhs The right-hand object.
   * @return Returns -1, 0 or 1.
   */
  public static int compareDocumentOrder( IModelObject lhs, IModelObject rhs)
  {
    IModelObject ancestor = findCommonAncestor( lhs, rhs);
    if ( ancestor == null) return 0;
    
    IModelObject leftParent = lhs;
    while( leftParent.getParent() != null && leftParent.getParent() != ancestor)
      leftParent = leftParent.getParent();
    
    IModelObject rightParent = null;
    while( rightParent.getParent() != null && rightParent.getParent() != ancestor)
      rightParent = rightParent.getParent();
    
    List<IModelObject> children = ancestor.getChildren();
    int leftIndex = children.indexOf( leftParent);
    int rightIndex = children.indexOf( rightParent);
    if ( leftIndex < rightIndex) return -1;
    if ( leftIndex > rightIndex) return 1;
    return 0;
  }
  
  /**
   * Returns true if the specified paths are identical.
   * @param path0 The first path.
   * @param path1 The second path.
   * @return Returns true if the specified paths are identical.
   */
  public static boolean comparePaths( IPath path0, IPath path1)
  {
    String string0 = path0.toString();
    String string1 = path1.toString();
    return string0.equals( string1);
  }

  /**
   * Create a subtree of the specified object which satisfies the specified path. The path may specify
   * any number of location steps, however location steps which have predicates must return at least
   * one element and the last location step must return zero elements.
   * @param context The context where the subtree will be created.
   * @param expression A PathExpression, FilterExpression or VariableExpression.
   * @param factory The factory for creating the new objects or null.
   * @param undo A change set where records will be created to undo the changes or null.
   * @param setter Null, the value to assign to the leaf nodes, or a Callable<Object> whose values will be assigned to the leaf nodes.
   */
  public static void createPathSubtree( IContext context, IExpression expression, IModelObjectFactory factory, IChangeSet undo, Object setter)
  {
    expression.createSubtree( context, factory, undo, setter);
  }
  
  /**
   * Create a subtree of the specified object which satisfies the specified path. The path may specify
   * any number of location steps, however location steps which have predicates must return at least
   * one element and the last location step must return zero elements.
   * @param object The object where the subtree will be created.
   * @param path The path defining the nodes to be created.
   * @param factory The factory for creating the new objects or null.
   * @param undo A change set where records will be created to undo the changes or null.
   * @param setter Null, the value to assign to the leaf nodes, or a Callable<Object> whose values will be assigned to the leaf nodes.
   */
  public static void createPathSubtree( IModelObject object, IPath path, IModelObjectFactory factory, IChangeSet undo, Object setter)
  {
    createPathSubtree( new Context( object), path, factory, undo, setter);
  }
  
  /**
   * Create a subtree of the specified object which satisfies the specified path. The path may specify
   * any number of location steps, however location steps which have predicates must return at least
   * one element and the last location step must return zero elements.
   * @param context The context where the subtree will be created.
   * @param path The path defining the nodes to be created.
   * @param factory The factory for creating the new objects or null.
   * @param undo A change set where records will be created to undo the changes or null.
   * @param setter Null, the value to assign to the leaf nodes, or a Callable<Object> whose values will be assigned to the leaf nodes.
   */
  public static void createPathSubtree( IContext context, IPath path, IModelObjectFactory factory, IChangeSet undo, Object setter)
  {
    if ( factory == null) factory = new ModelObjectFactory();
    
    // create result list
    int length = path.length();
    List<IModelObject> result = new ArrayList<IModelObject>();
    
    // configure layers so result is nextLayer at end of query
    boolean even = (length % 2) == 0;
    List<IModelObject> currLayer = even? new ArrayList<IModelObject>(): result;
    List<IModelObject> nextLayer = even? result: new ArrayList<IModelObject>();
    
    // query each location
    nextLayer.add( context.getObject());
    for ( int i=0; i<length; i++)
    {
      IPathElement element = path.getPathElement( i);
      List<IModelObject> swapLayer = currLayer;
      currLayer = nextLayer;
      nextLayer = swapLayer;
      nextLayer.clear();
      int currSize = currLayer.size();
      for ( int j=0; j<currSize; j++)
      {
        IModelObject layerObject = (IModelObject)currLayer.get( j);
        element.query( context, layerObject, nextLayer);
      }
      
      // create object if location step has no predicate and returned zero elements
      IPredicate predicate = element.predicate();
      if ( predicate == null && nextLayer.size() == 0)
      {
        for ( int j=0; j<currSize; j++)
        {
          IModelObject layerObject = currLayer.get( j);
          try
          {
            if ( (element.axis() & IAxis.ATTRIBUTE) != 0)
            {
              layerObject.setAttribute( element.type(), (setter != null)? ((setter instanceof Callable)? ((Callable<?>)setter).call(): setter): "");
              nextLayer.add( layerObject.getAttributeNode( element.type()));
              if ( undo != null) undo.removeAttribute( layerObject, element.type());
            }
            else
            {
              if ( element.type() == null) return;
              IModelObject newObject = factory.createObject( layerObject, element.type());
              if ( setter != null) newObject.setValue( ((setter instanceof Callable)? ((Callable<?>)setter).call(): setter));
              layerObject.addChild( newObject);
              nextLayer.add( newObject);
              if ( undo != null) undo.removeChild( layerObject, newObject);
            }
          }
          catch( Exception e)
          {
            SLog.exception( ModelAlgorithms.class, e);
          }
        }
      }
    }
  }
  
  /**
   * Completely dereference the specified object.
   * @param object The object.
   * @return Returns the dereferenced object.
   */
  public static IModelObject dereference( IModelObject object)
  {
    IModelObject referent = object.getReferent();
    while( referent != object) 
    {
      object = referent;
      referent = referent.getReferent();
    }
    return referent;
  }
  
  /**
   * Returns a unique session ID for the XModel. Each object ID is prefixed with the session ID.
   * @return Returns a unique session ID for the XModel.
   */
  public static String getUniqueSession()
  {
    if ( rstamp == null) 
    {
      // time-stamp
      long time = System.currentTimeMillis();
      String tstamp = Integer.toString( (int)(time>>>20), 36);
      tstamp = tstamp.substring( 2);
      
      // random-stamp
      SecureRandom random = new SecureRandom();
      int value = random.nextInt();
      if ( value < 0) value = -value;
      
      synchronized( ModelAlgorithms.class)
      {
        rstamp = Integer.toString( value, 36);
        rstamp = rstamp.substring( 2);
        rstamp = tstamp + rstamp;
      }
    }
    return rstamp;
  }
  
  /**
   * Create an object ID which includes a time-stamp, a random number and the specified integer.
   * @param id The integer which will become the string suffix.
   * @return Returns a unique object ID.
   */
  public static String createUniqueID( int id)
  {
    String session = getUniqueSession();
    
    // generate name
    StringBuilder name = new StringBuilder();
    name.append( session);
    name.append( Integer.toString( id, 36));
    return name.toString();
  }
  
  /**
   * Create a master clone of the specified slave object.  The master clone can be used in another thread,
   * and the slave will be kept synchronized via its thread dispatcher.
   * @see org.xmodel.concurrent.MasterSlaveListener
   * @param slave The slave.
   * @param executor The executor for handling updates in the correct thread.
   * @return Returns the master clone, or null if the argument is null.
   */
  public static IModelObject createMasterClone( IModelObject slave, Executor executor)
  {
    if ( slave == null) return null;
    
    IModelObject master = slave.cloneTree();
    MasterSlaveListener masterListener = new MasterSlaveListener( master, slave, executor);
    masterListener.install( master);
    return master;
  }

  /**
   * Create a slave clone of the specified master object.  The slave clone can be used in another thread
   * and it will be kept synchronized with the master via its thread dispatcher.
   * @see org.xmodel.concurrent.MasterSlaveListener
   * @param master The master.
   * @param executor The executor for handling updates in the correct thread.
   * @return Returns the slave clone, or null if the argument is null.
   */
  public static IModelObject createSlaveClone( IModelObject master, Executor executor)
  {
    if ( master == null) return null;
    
    IModelObject slave = master.cloneTree();
    MasterSlaveListener masterListener = new MasterSlaveListener( master, slave, executor);
    masterListener.install( master);
    return slave;
  }

  /**
   * An IModelObjectFactory which clones ExternalReferences or delegates to another factory.
   */
  private static class ReferenceFactory extends ModelObjectFactory
  {
    public IModelObject createClone( IModelObject object)
    {
      if ( object instanceof IExternalReference)
      {
        IExternalReference reference = (IExternalReference)object;
        IExternalReference clone = (IExternalReference)reference.createObject( reference.getType());
        if ( reference.isDirty())
        {
          ICachingPolicy cachingPolicy = reference.getCachingPolicy();
          if ( cachingPolicy != null)
          {
            for( String attrName: cachingPolicy.getStaticAttributes())
            {
              if ( attrName.equals( "*")) copyAttributes( reference, clone);
              else clone.setAttribute( attrName, reference.getAttribute( attrName));
            }
          }
        }
        else
        {
          ModelAlgorithms.copyAttributes( reference, clone);
        }
        
        try
        {
          ICachingPolicy cachingPolicy = reference.getCachingPolicy();
          if ( cachingPolicy != null)
          {
            clone.setCachingPolicy( (ICachingPolicy)cachingPolicy.clone());
            clone.setDirty( reference.isDirty());
          }
        }
        catch( CloneNotSupportedException e)
        {
          SLog.errorf( this, "%s of element, %s, is not cloneable!", 
              reference.getCachingPolicy().getClass().getSimpleName(),
              reference.getType());
        }
        
        return clone;
      }
      
      if ( delegate != null) return delegate.createClone( object);
      return super.createClone( object);
    }
    
    public IModelObjectFactory delegate;
  }
  
  private static String rstamp;
  
  public static void main( String[] args) throws Exception
  {
    String xml = 
      "<a>" +
      "  <b id='1' name='B1'>" +
      "    <c>1</c>" +
      "    <c>2</c>" +
      "    <c>3</c>" +
      "  </b>" +
      "  <b id=\'2\'>" +
      "    <c>4</c>" +
      "    <c>5</c>" +
      "    <c>6</c>" +
      "  </b>" +
      "  <b id=\'3\'>" +
      "    <c>7</c>" +
      "    <c>8</c>" +
      "    <c>9</c>" +
      "  </b>" +
      "</a>";
    
    XmlIO xmlIO = new XmlIO();
    IModelObject root = xmlIO.read( xml);
    IModelObject leaf = root.getChild( 1).getChild( 0);
    IPath path = ModelAlgorithms.createIdentityPath( leaf, true);
    System.out.println( path.toString());
  }
}
