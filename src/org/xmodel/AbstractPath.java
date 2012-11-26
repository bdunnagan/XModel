/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractPath.java
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

import java.util.ArrayList;
import java.util.List;
import org.xmodel.path.IListenerChain;
import org.xmodel.path.ListenerChain;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableSource;
import org.xmodel.xpath.variable.Precedences;
import org.xmodel.xpath.variable.VariableScope;
import org.xmodel.xpath.variable.VariableSource;


/**
 * A partial implementation of IPath which provides all of the interface semantics except that
 * of constructing the list of IPathElement instances that comprise the path.
 * <p>
 * Subclasses of AbstractPath are not thread-safe. Furthermore, paths which are indexed cannot
 * be used in more than one thread even if the two threads don't access the path concurrently.
 * This is due to the fact that indexing changes the internal structure of the path and indexes
 * are associated with a particular IModel which, in turn, is associated with a particular thread.
 */
public abstract class AbstractPath implements IPath, IAxis
{
  public AbstractPath()
  {
    elements = new IPathElement[ 0];
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#setParent(org.xmodel.xpath.expression.IExpression)
   */
  public void setParent( IExpression parent)
  {
    this.parent = parent;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#getParent()
   */
  public IExpression getParent()
  {
    return parent;
  }

  /**
   * Add an element to the path.
   * @param element The element to be added.
   */
  public void addElement( IPathElement element)
  {
    inverse = null;
    isAbsolute = null;
    if ( chainProto != null) throw new IllegalStateException( "Unable to add elements to bound path.");
    List<IPathElement> list = new ArrayList<IPathElement>( elements.length+1);
    for( int i=0; i<elements.length; i++) list.add( elements[ i]);
    list.add( element);
    elements = list.toArray( new IPathElement[ 0]);
  }
  
  /**
   * Add an element to the path at the specified position.
   * @param index The index of the position where the element should be inserted.
   * @param element The element to be added.
   */
  public void addElement( int index, IPathElement element)
  {
    inverse = null;
    isAbsolute = null;
    if ( chainProto != null) throw new IllegalStateException( "Unable to add elements to bound path.");
    List<IPathElement> list = new ArrayList<IPathElement>( elements.length+1);
    for( int i=0; i<elements.length; i++) list.add( elements[ i]);
    list.add( index, element);
    elements = list.toArray( new IPathElement[ 0]);
  }
  
  /**
   * Remove an element from the path.
   * @param index The index of the element to be removed.
   * @return Returns the element that was removed.
   */
  public IPathElement removeElement( int index)
  {
    inverse = null;
    isAbsolute = null;
    if ( chainProto != null) throw new IllegalStateException( "Unable to remove elements from bound path.");
    List<IPathElement> list = new ArrayList<IPathElement>( elements.length+1);
    for( int i=0; i<elements.length; i++) list.add( elements[ i]);
    IPathElement element = list.remove( index);
    elements = list.toArray( new IPathElement[ 0]);
    return element;
  }

  /**
   * Append the specified path to this path.
   * @param path The path to be appended.
   */
  public void appendPath( IPath path)
  {
    inverse = null;
    isAbsolute = null;
    if ( chainProto != null) throw new IllegalStateException( "Unable to add elements to bound path.");
    for( int i=0; i<path.length(); i++)
    {
      IPathElement element = path.getPathElement( i);
      addElement( element);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#isAbsolute(org.xmodel.xpath.expression.IContext)
   */
  public boolean isAbsolute( IContext context)
  {
    if ( isAbsolute != null) return isAbsolute;
    if ( elements.length == 0) return (isAbsolute = false);

    // check if relative path
    IPathElement element = elements[ 0];
    if ((element.axis() & ROOT) == 0) return (isAbsolute = false);
    
    // check if any predicate is absolute
    for( int i=0; i<elements.length; i++)
    {
      IPredicate predicate = elements[ i].predicate();
      if ( predicate != null && !predicate.isAbsolute( context)) return (isAbsolute = false);
    }
    
    return (isAbsolute = true);
  }
  
  /*
   * (non-Javadoc)
   * @see org.xmodel.IPath#evaluate(org.xmodel.IModelObject)
   */
  public boolean isLeaf( INode object)
  {
    IPath inversePath = invertPath();
    return inversePath.queryFirst( object) != null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#isNode(org.xmodel.IModelObject)
   */
  public boolean isNode( INode object)
  {
    IPath inversePath = invertPath();
    CanonicalPath pathSegment = new CanonicalPath();
    for ( int i=0; i<inversePath.length(); i++)
    {
      pathSegment.addElement( i, inversePath.getPathElement( i));
      if ( pathSegment.queryFirst( object) != null) return true;
    }
    return false;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#invertPath()
   */
  public IPath invertPath()
  {
    if ( inverse != null) return inverse;
    
    CanonicalPath inverse = new CanonicalPath();
    for ( int i=0; i<length(); i++)
    {
      IPathElement thisElement = getPathElement( i);
      if ( i < length()-1)
      {
        IPathElement nextElement = getPathElement( i+1);
        int newAxis = invertAxis( nextElement.axis());
        if ( newAxis < 0) return null;
        inverse.addElement( 0, thisElement.clone( newAxis));
      }
      else
      {
        inverse.addElement( 0, thisElement.clone( SELF));
      }
    }
    
    this.inverse = inverse;
    return inverse;
  }
  
  /**
   * Returns the inverse of the specified axis.  Note that for paths which conform
   * to the X-Path specification, an element with the ROOT designation will be the
   * first element in the path and the first element axis does not need to be 
   * inverted.  If the axis cannot be inverted as with ATTRIBUTE, then -1 is returned.
   * @param axis The axis to invert.
   * @return Returns the inverse of the specified axis or -1 if there is not one.
   */
  private final int invertAxis( int axis)
  {
    if ( (axis & ATTRIBUTE) != 0) return -1;
    if ( (axis & ANCESTOR) != 0) return axis & ~ANCESTOR | DESCENDANT;
    if ( (axis & DESCENDANT) != 0) return axis & ~DESCENDANT | ANCESTOR;
    if ( (axis & PARENT) != 0) return axis & ~PARENT | CHILD;
    if ( (axis & CHILD) != 0) return axis & ~CHILD | PARENT;
    return axis;
  }
  
  /*
   * (non-Javadoc)
   * @see org.xmodel.IPath#getPathElement(int)
   */
  public IPathElement getPathElement( int index)
  {
    return elements[ index];
  }

  /*
   * (non-Javadoc)
   * @see org.xmodel.IPath#length()
   */
  public int length()
  {
    return elements.length;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#createSubtree(org.xmodel.IModelObject)
   */
  public INode createSubtree( INode root)
  {
    ModelAlgorithms.createPathSubtree( root, this, null, null);
    return queryFirst( root);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#getVariableSource()
   */
  public IVariableSource getVariableSource()
  {
    // use variable source of expression tree
    if ( parent != null) return parent.getVariableSource();
    
    // use path variable source
    if ( source == null) 
    {
      source = new VariableSource();
      source.addScope( new VariableScope( "local", Precedences.localScope));
    }
    
    return source;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#setVariable(java.lang.String, java.lang.Boolean)
   */
  public void setVariable( String name, Boolean value)
  {
    getVariableSource().getScope( "local").set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#setVariable(java.lang.String, java.lang.Number)
   */
  public void setVariable( String name, Number value)
  {
    getVariableSource().getScope( "local").set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#setVariable(java.lang.String, java.lang.String)
   */
  public void setVariable( String name, String value)
  {
    getVariableSource().getScope( "local").set( name, value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#setVariable(java.lang.String, org.xmodel.IModelObject)
   */
  public void setVariable( String name, INode node)
  {
    getVariableSource().getScope( "local").set( name, node);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#setVariable(java.lang.String, java.util.List)
   */
  public void setVariable( String name, List<INode> nodes)
  {
    getVariableSource().getScope( "local").set( name, nodes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#setVariable(java.lang.String, 
   * org.xmodel.xpath.expression.IExpression)
   */
  public void setVariable( String name, IExpression expression)
  {
    getVariableSource().getScope( "local").define( name, expression);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#query(org.xmodel.IModelObject, java.util.List)
   */
  public List<INode> query( INode object, List<INode> result)
  {
    return query( null, object, length(), result);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#query(org.xmodel.IModelObject, int, java.util.List)
   */
  public List<INode> query( INode object, int length, List<INode> result)
  {
    return query( null, object, length, result);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#query(org.xmodel.xpath.expression.IContext,
   * org.xmodel.IModelObject, java.util.List)
   */
  public List<INode> query( IContext context, List<INode> result)
  {
    return query( context, context.getObject(), length(), result);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IPath#query(org.xmodel.xpath.expression.IContext, int, 
   * java.util.List)
   */
  public List<INode> query( IContext context, int length, List<INode> result)
  {
    return query( context, context.getObject(), length, result);
  }

  /**
   * Perform the path query using the object argument as the context. The parent context is provided
   * so that it can be associated to sub-contexts created during path processing. Usually, the parent
   * context object will be the same as the object argument. The parent context may be null.
   * @param parent The parent context or null.
   * @param object The context object.
   * @param length The length of the path segment to query.
   * @param result The list where the result nodes will be stored or null.
   * @return Returns the result argument or a new list if the result argument is null.
   */
  protected List<INode> query( IContext parent, INode object, int length, List<INode> result)
  {
    if ( result == null) result = new ArrayList<INode>();
    List<INode> layer = new ArrayList<INode>();
    
    // configure layers so result is nextLayer at end of query
    List<INode> currLayer = layer;
    List<INode> nextLayer = result;
    if ( (length & 0x1) != 0)
    {
      currLayer = result;
      nextLayer = layer;
    }
      
    // query each location
    nextLayer.clear();
    nextLayer.add( object);
    for ( int i=0; i<length; i++)
    {
      IPathElement element = elements[ i];
      
      List<INode> swapLayer = currLayer;
      currLayer = nextLayer;
      nextLayer = swapLayer;
      
      nextLayer.clear();
      int currSize = currLayer.size();
      for ( int j=0; j<currSize; j++)
      {
        element.query( parent, currLayer.get( j), nextLayer);
      }
    }

    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#queryFirst(org.xmodel.IModelObject)
   */
  public INode queryFirst( INode object)
  {
    List<INode> list = query( object, null);
    if ( list.size() == 0) return null;
    return (INode)list.get( 0);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#addPathListener(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IPathListener)
   */
  public void addPathListener( IContext context, IPathListener listener)
  {
    if ( context == null)
      throw new IllegalArgumentException( 
        "Attempt to add listener to null context: path:"+toString());
    
    // lazily build proto
    if ( chainProto == null) chainProto = new ListenerChain( this, null, null);

    // evaluate path once to ensure that external references are synced
    // this prevents duplicate notification caused by syncing in FanoutListener
    query( context, null);
    
    // install listener chain
    PathListenerList list = context.getObject().getPathListeners();
    IListenerChain chain = list.findListenerChain( context, listener);
    if ( chain == null)
    {
      // create chain and install on context
      chain = new ListenerChain( this, chainProto, context, listener);
      chain.install( context.getObject());
      list.addListenerChain( chain);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#removePathListener(org.xmodel.IModelObject, 
   * org.xmodel.IPathListener)
   */
  public void removePathListener( IContext context, IPathListener listener)
  {
    if ( context == null)
      throw new IllegalArgumentException( 
        "Attempt to remove listener from null context: path:"+toString());
    
    PathListenerList list = context.getObject().getPathListeners();
    IListenerChain chain = list.removeListenerChain( context, listener);
    if ( chain == null) return;
    
    // uninstall
    chain.uninstall( context.getObject());
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  public IPath clone()
  {
    AbstractPath path = new CanonicalPath( this);
    path.parent = parent;
    path.chainProto = chainProto;
    return path;
  }
  
  private IExpression parent;
  private IPathElement[] elements;
  private IPath inverse;
  protected IListenerChain chainProto;
  private Boolean isAbsolute;
  private IVariableSource source;
}