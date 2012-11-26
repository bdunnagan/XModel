/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PathElement.java
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
package org.xmodel.xpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.xmodel.BreadthFirstIterator;
import org.xmodel.FollowingIterator;
import org.xmodel.IAxis;
import org.xmodel.INode;
import org.xmodel.IPath;
import org.xmodel.IPathElement;
import org.xmodel.IPredicate;
import org.xmodel.PrecedingIterator;
import org.xmodel.log.Log;
import org.xmodel.util.Fifo;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.expression.SubContext;


/**
 * An implementation of IModelQuery which can be used to perform any type of query 
 * that lies on one of the axes: ANCESTOR, PARENT, SELF, CHILD or DECENDENT.  This
 * class also implements the IPathElement interface making it suitable for use as
 * an element of an IPath.
 */
public class PathElement implements IPathElement, IAxis
{
  /**
   * Create an axis query which will match all objects on the specified axis.
   * @param axis One of ROOT, ANCESTOR, PARENT, SELF, CHILD or DECENDENT on IAxis.
   */
  public PathElement( int axis)
  {
    this.axis = axis;
  }
  
  /**
   * Create an axis query which will match all objects on the specified axis.
   * @param axis One of ROOT, ANCESTOR, PARENT, SELF, CHILD or DECENDENT on IAxis.
   * @param predicate A filter for refining the node list.
   */
  public PathElement( int axis, IPredicate predicate)
  {
    this.axis = axis;
    this.predicate = predicate;
  }
  
  /**
   * Create an axis query which will match objects on the specified axis which have
   * one of the specified types.
   * @param axis One of ROOT, ANCESTOR, PARENT, SELF, CHILD or DECENDENT on IAxis.
   * @param type An object type.
   */
  public PathElement( int axis, String type)
  {
    this.axis = axis;
    this.type = type;
  }
  
  /**
   * Create an axis query which will match objects on the specified axis which have
   * one of the specified types and satisfies the specified filter.  Either the type
   * list or the filter may be null.  If the type list is null, then all types are
   * considered.  If the filter is null, then it is ignored.
   * @param axis One of ROOT, ANCESTOR, PARENT, SELF, CHILD or DECENDENT on IAxis.
   * @param type An object type.
   * @param predicate A filter for refining the node list.
   */
  public PathElement( int axis, String type, IPredicate predicate)
  {
    this.axis = axis;
    this.type = type;
    this.predicate = predicate;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#setParent(org.xmodel.IPath)
   */
  public void setParent( IPath path)
  {
    if ( predicate != null) predicate.setParentPath( path);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#axis()
   */
  public int axis()
  {
    return axis;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#setAxis(int)
   */
  public void setAxis( int axis)
  {
    this.axis = axis;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#hasAxis(int)
   */
  public boolean hasAxis( int axis)
  {
    return (this.axis & axis) != 0;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#type()
   */
  public String type()
  {
    return type;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#predicate()
   */
  public IPredicate predicate()
  {
    return predicate;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#query(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IModelObject, java.util.List)
   */
  public List<INode> query( IContext parent, INode object, List<INode> result)
  {
    int start = 0;
    if ( result == null) result = new ArrayList<INode>(); else start = result.size();
    if ( (axis & ROOT) != 0) 
    {
      int oldSize = result.size();
      findMatchingNodes( object.getRoot(), type, result);
      int newSize = result.size();
      if ( newSize == oldSize) return result;
      object = (INode)result.get( newSize-1);
    }
    if ( (axis & SELF) != 0) findMatchingSelf( object, type, result);
    if ( (axis & ANCESTOR) != 0) findMatchingAncestors( object, type, result);
    if ( (axis & DESCENDANT) != 0) findMatchingDescendants( object, type, result);
    if ( (axis & FOLLOWING) != 0) findMatchingFollowing( object, type, result);
    if ( (axis & FOLLOWING_SIBLING) != 0) findMatchingFollowingSiblings( object, type, result);
    if ( (axis & PRECEDING) != 0) findMatchingPreceding( object, type, result);
    if ( (axis & PRECEDING_SIBLING) != 0) findMatchingPrecedingSiblings( object, type, result);
    if ( (axis & NESTED) != 0) findMatchingNested( object, type, result);
    if ( (axis & PARENT) != 0) findMatchingParent( object, type, result);
    if ( (axis & CHILD) != 0) findMatchingChildren( object, type, result);
    if ( (axis & ATTRIBUTE) != 0) findMatchingAttributes( object, type, result);
    
    // apply predicate
    filterNodeSet( parent, result, start);
    
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#query(org.xmodel.xpath.expression.IContext, 
   * java.util.List, java.util.List)
   */
  public List<INode> query( IContext context, List<INode> list, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>();
    int size = list.size();
    for ( int i=0; i<size; i++)
    {
      INode object = (INode)list.get( i);
      query( context, object, result);
    }
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#evaluate(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IPath, org.xmodel.IModelObject)
   */
  public boolean evaluate( IContext context, IPath candidatePath, INode candidate)
  {
    if ( !performNodeTest( candidate, type)) return false;
    if ( predicate != null && !predicate.evaluate( context, candidatePath, candidate)) return false;
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    if ( type != null) return axis + predicate.hashCode() + type.hashCode();
    return axis + predicate.hashCode();
  }
  
  /**
   * Adds the specified object to the result list if it matches the specified type. If the result
   * parameter is not null, then the matching objects are stored in that list, otherwise a new list
   * is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the result should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingSelf( INode object, String type, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>( 2);
    if ( performNodeTest( object, type)) result.add( object);
    return result;
  }
  
  /**
   * Find the ancestors of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in
   * that list, otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingAncestors( INode object, String type, List<INode> result)
  {
    INode ancestor = object.getParent();
    while( ancestor != null)
    {
      result = findMatchingNodes( ancestor, type, result);
      ancestor = ancestor.getParent();
    }
    return result;
  }
  
  /**
   * Find the parent of the specified object which has the specified type and add it to the result
   * list. If the result list parameter is not null, then the matching object is stored in that
   * list, otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingParent( INode object, String type, List<INode> result)
  {
    INode parent = object.getParent();
    if ( parent != null) result = findMatchingNodes( parent, type, result);
    return result;
  }
  
  /**
   * Find the children of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in
   * that list, otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingChildren( INode object, String type, List<INode> result)
  {
    if ( type != null)
    {
      if ( type.equals( "text()"))
      {
        result = findMatchingText( object, result);
        return result;
      }
      else if ( type.equals( "node()"))
      {
        result = findMatchingAttributes( object, null, result);
        result = findMatchingText( object, result);
        result = findMatchingProcessingInstructions( object, null, result);
        for ( Object child: object.getChildren())
          result = findMatchingNodes( (INode)child, null, result);
        return result;
      }
      else if ( type.startsWith( "processing-instruction"))
      {
        return findMatchingProcessingInstructions( object, type, result);
      }
      else if ( type.indexOf( "*") < 0)
      {
        // make specific getChildren call
        if ( result == null) result = new ArrayList<INode>();
        List<INode> children = object.getChildren( type);
        for( INode child: children)
          if ( child.getType().charAt( 0) != '?' && performNodeTest( child, type))
            result.add( child);
        return result;
      }
      else
      {
        // make generic getChildren call
        if ( result == null) result = new ArrayList<INode>();
        List<INode> children = (List<INode>)object.getChildren();
        for( INode child: children)
          if ( child.getType().charAt( 0) != '?' && performNodeTest( child, type))
            result.add( child);
      }
    }
    else
    {
      // make generic getChildren call
      if ( result == null) result = new ArrayList<INode>();
      List<INode> children = (List<INode>)object.getChildren();
      for( INode child: children)
      {
        String childType = child.getType();
        if ( (childType.length() == 0 || childType.charAt( 0) != '?') && performNodeTest( child, type))
          result.add( child);
      }
    }
      
    return result;
  }
  
  /**
   * Find the descendants of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in
   * that list, otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingDescendants( INode object, String type, List<INode> result)
  {
    BreadthFirstIterator iter = new BreadthFirstIterator( object);
    while( iter.hasNext())
    {
      INode descendant = (INode)iter.next();
      if ( descendant != object) result = findMatchingNodes( descendant, type, result);
    }
    return result;
  }

  /**
   * Find all of the following elements of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in that list,
   * otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingFollowing( INode object, String type, List<INode> result)
  {
    FollowingIterator iter = new FollowingIterator( object);
    while( iter.hasNext())
    {
      object = (INode)iter.next();
      result = findMatchingNodes( object, type, result);
    }
    return result;
  }
    
  /**
   * Find all of the following siblings of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in that list,
   * otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingFollowingSiblings( INode object, String type, List<INode> result)
  {
    if ( object instanceof AttributeNode)
    {
      if ( result == null) result = Collections.emptyList();
      return result;
    }
    else
    {
      if ( result == null) result = new ArrayList<INode>( 5);

      INode parent = object.getParent();
      List<INode> children = parent.getChildren();
      int start = children.indexOf( object);
      for( int i = start+1; i < children.size(); i++)
        result.add( children.get( i));
    }
    
    return result;
  }
    
  /**
   * Find all of the preceding elements of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in that list,
   * otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingPreceding( INode object, String type, List<INode> result)
  {
    PrecedingIterator iter = new PrecedingIterator( object);
    while( iter.hasNext())
    {
      object = (INode)iter.next();
      result = findMatchingNodes( object, type, result);
    }
    return result;
  }
    
  /**
   * Find all of the preceding siblings of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in that list,
   * otherwise a new list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingPrecedingSiblings( INode object, String type, List<INode> result)
  {
    INode parent = object.getParent();
    if ( parent == null || object instanceof AttributeNode)
    {
      if ( result == null) result = Collections.emptyList();
      return result;
    }
    else
    {
      if ( result == null) result = new ArrayList<INode>( 5);

      List<INode> children = parent.getChildren();
      int start = children.indexOf( object);
      for( int i = start-1; i >= 0; i--)
      {
        INode child = children.get( i);
        if ( performNodeTest( child, type))
          result.add( child);
      }
    }
    
    return result;
  }
    
  /**
   * Find all descendants which form an unbroken ancestry of objects of the specified type and add
   * them to the result list. This type of query is similar to the descendant query with the
   * exception that it will not traverse nodes of any type except the specified type. If the result
   * list parameter is not null, then the matching objects are stored in that list, otherwise a new
   * list is created.
   * @param object The target object.
   * @param type The object type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingNested( INode object, String type, List<INode> result)
  {
    int start = (result != null)? result.size(): 0;
    Fifo<INode> stack = new Fifo<INode>();
    stack.push( object);
    while( !stack.empty())
    {
      object = stack.pop();
      result = findMatchingChildren( object, type, result);
      for( int i=start; i<result.size(); i++)
      {
        object = result.get( i);
        stack.push( object);
      }
      start = result.size();
    }
    return result;
  }
  
  /**
   * Find the attributes of the specified object which have the specified type and add them to the
   * result list.  If the result list parameter is not null, then the matching objects are stored in
   * that list, otherwise a new list is created.
   * @param object The target object.
   * @param name The name of the attribute.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching objects.
   */
  private final List<INode> findMatchingAttributes( INode object, String name, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>( 5);
    if ( name != null)
    {
      if ( name.endsWith( ")") && name.equals( "text()"))
        name = "";
        
      if ( object.getAttribute( name) != null)
        result.add( object.getAttributeNode( name));
    }
    else
    {
      Iterator<String> iter = object.getAttributeNames().iterator();
      while( iter.hasNext())
      {
        String attrName = iter.next();
        if ( attrName.length() > 0)
          result.add( object.getAttributeNode( attrName));
      }
    }
    return result;
  }
  
  /**
   * Find the test node of the specified object add it to the result list. If the result list
   * parameter is not null, then the matching objects are stored in that list, otherwise a new list
   * is created.
   * @param object The target object.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching text nodes.
   */
  private final List<INode> findMatchingText( INode object, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>( 2);
    if ( object.getValue() != null)
      result.add( object.getAttributeNode( ""));
    return result;
  }
  
  /**
   * Find the matching processing instruction children of the specified object. If the result list
   * parameter is not null, then the matching objects are stored in that list, otherwise a new list
   * is created.
   * @param object The target object.
   * @param piTest The entire processing instruction node-test.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching processing instructions.
   */
  private final List<INode> findMatchingProcessingInstructions( INode object, String piTest, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>( 2);
    
    int start = (piTest != null)? piTest.indexOf( "'", 22): -1;
    if ( start < 0)
    {
      List<INode> children = object.getChildren();
      for( INode child: children)
        if ( child.getType().charAt( 0) == '?')
          result.add( child);
    }
    else
    {
      start++;
      int end = piTest.indexOf( "'", start);
      String literal = "?"+piTest.substring( start, end);
      List<INode> children = object.getChildren();
      for( INode child: children)
        if ( child.getType().equals( literal))
          result.add( child);
    }
    
    return result;
  }
  
  /**
   * Find the child nodes of the specified object which have the specified type and add them to the
   * result list. If the result list parameter is not null, then the matching objects are stored in
   * that list, otherwise a new list is created. Light-weight IModelObjects are created to wrapper
   * the text information.
   * @param object The target object.
   * @param type The node-test type.
   * @param result An optional list where the results should be stored.
   * @return Returns the matching text nodes.
   */
  private final List<INode> findMatchingNodes( INode object, String type, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>( 5);
    
    if ( type != null)
    {
      if ( type.equals( "text()"))
      {
        result = findMatchingText( object, result);
      }
      else if ( type.equals( "node()"))
      {
        result = findMatchingAttributes( object, null, result);
        result = findMatchingText( object, result);
        result = findMatchingProcessingInstructions( object, null, result);
        result.add( object);
      }
      else if ( type.startsWith( "processing-instruction"))
      {
        return findMatchingProcessingInstructions( object, type, result);
      }
      else if ( performNodeTest( object, type))
      {
        result.add( object);        
      }
    }
    else 
    {
      result.add( object);
    }
    
    return result;
  }
  
  /**
   * Perform the node-test for the specified object.  The node-test is defined in the type
   * argument and may be null, which means all element children.  It may also include a 
   * namespace followed by a wildcard.  In some cases, text() or node() might be present.
   * @param object The object whose type is being tested.
   * @param test The node-test.
   * @return Returns true if the object conforms to the node-test.
   */
  private final boolean performNodeTest( INode object, String test)
  {
    if ( test == null) return true;
    
    int testLength = test.length();
    if ( test.charAt( testLength - 1) == ')')
    {
      // text()
      if ( test.charAt( 0) == 't')
      {
        if ( !(object instanceof AttributeNode)) return false;
        AttributeNode node = (AttributeNode)object;
        return node.attrName.length() == 0;
      }
      
      // node()
      else
      {
        return true;
      }
    }
    else
    {
      String type = object.getType();
      boolean prefix = true;
      int i=0, j=0;
      for( ; i<type.length() && j<testLength; i++)
      {
        char lc = type.charAt( i);
        char rc = test.charAt( j);
        if ( lc == ':') prefix = false;
        if ( !prefix && rc == '*') return true;
        if ( !prefix || rc != '*') j++;
        if ( rc != '*' && lc != rc) return false;
      }
      return (i == type.length() && j == testLength);
    }
  }
  
  /**
   * Filter the specified node-set using this PathElement's predicate.
   * @param parent The parent context or null.
   * @param nodeSet The node-set to be filtered.
   * @param start The first element to be filtered.
   */
  private final void filterNodeSet( IContext parent, List<INode> nodeSet, int start)
  {
    if ( predicate == null) return;

    // evaluate predicates
    IExpression expression = (IExpression)predicate;
    List<IExpression> arguments = expression.getArguments();
    for ( IExpression argument: arguments)
    {
      // FIXME: need a method on IPredicate for filtering entire node-sets so that PredicateExpression
      //        can contain this logic.
      if ( argument.getType( parent) == ResultType.NUMBER)
      {
        try
        {
          int position = (int)argument.evaluateNumber( parent) - 1 + start;
          if ( position < start) throw new ExpressionException( argument, "position values begin with 1");
          if ( position < nodeSet.size())
          {
            INode node = nodeSet.get( position);
            nodeSet.clear(); nodeSet.add( node);
          }
          else
          {
            nodeSet.clear();
          }
        }
        catch( ExpressionException e) 
        {
          log.exception( e);
          nodeSet.clear();
        }
      }
      else
      {
        int size = nodeSet.size();
        for ( int i=start; i<size; i++)
        {
          try
          {
            IContext context = null;
            if ( parent == null)
              context = new Context( nodeSet.get( i), i+1, size);
            else
              context = new SubContext( parent, nodeSet.get( i), i+1, size);
            if ( !argument.evaluateBoolean( context)) nodeSet.set( i, null);
          }
          catch( ExpressionException e)
          {
            log.exception( e);
            nodeSet.set( i, null);
          }
        }
        for ( int i=start; i<size; i++)
        {
          INode resultNode = (INode)nodeSet.get( i);
          if ( resultNode == null)
          {
            nodeSet.remove( i--);
            size--;
          }
        }
      }
    }
  }
    
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public IPathElement clone()
  {
    return clone( axis);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPathElement#clone(int)
   */
  public IPathElement clone( int axis)
  {
    IPredicate clone = (predicate != null)? (IPredicate)predicate.clone(): null;
    return new PathElement( axis, type, clone);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( object instanceof IPathElement)
    {
      IPathElement element = (IPathElement)object;
      if ( element.axis() != axis) return false;
      if ( (element.type() != null && type != null) && !element.type().equals( type)) return false;
      if ( (element.type() == null || type == null) && element.type() != type) return false;
      return element.predicate() == predicate;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    switch( axis)
    {
      case IAxis.ATTRIBUTE:  
      {
        buffer.append( "@"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case IAxis.CHILD:      
      {
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case IAxis.NESTED:     
      {
        buffer.append( "nested::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case IAxis.DESCENDANT: 
      {
        buffer.append( "//"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case IAxis.PARENT:     
      {
        if ( type != null) 
        {
          buffer.append( "parent::");
          buffer.append( type);
        }
        else
        {
          buffer.append( "..");
        }
      }
      break;
      
      case IAxis.ROOT:       
      {
        buffer.append( '/'); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case IAxis.SELF:       
      {
        if ( type != null)
        {
          buffer.append( "self::"); 
          buffer.append( type);
        }
        else
        {
          buffer.append( ".");
        }
      }
      break;
      
      case IAxis.ANCESTOR:   
      {
        buffer.append( "ancestor::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case (IAxis.NESTED | IAxis.SELF): 
      {
        buffer.append( "nested-or-self::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case (IAxis.DESCENDANT | IAxis.SELF): 
      {
        buffer.append( "descendant-or-self::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
      
      case (IAxis.ANCESTOR | IAxis.SELF):   
      {
        buffer.append( "ancestor-or-self::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;      

      case (IAxis.FOLLOWING):   
      {
        buffer.append( "following::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;

      case (IAxis.FOLLOWING_SIBLING):   
      {
        buffer.append( "following-sibling::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;

      case (IAxis.PRECEDING):   
      {
        buffer.append( "preceding::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;

      case (IAxis.PRECEDING_SIBLING):   
      {
        buffer.append( "preceding-sibling::"); 
        buffer.append( (type == null)? "*": type);
      }
      break;
    }
    
    if ( predicate != null) buffer.append( predicate.toString());
    return buffer.toString();
  }

  int axis;
  String type;
  IPredicate predicate;
  
  private static Log log = Log.getLog( "org.xmodel.xml");
}
