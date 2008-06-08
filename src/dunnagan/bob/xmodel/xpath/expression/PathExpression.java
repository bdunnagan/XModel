/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.List;

import dunnagan.bob.xmodel.*;

/**
 * An implementation of IExpression which represents an X-Path 1.0 path expression. When this expression is
 * bound, it adds a LeafValueListener to each node in the node-set. This listener detects changes to the 
 * value of the node and performs notification.
 */
public class PathExpression extends Expression implements IPathListener
{
  /**
   * Create a PathExpression from the specified path.
   * @param path The path.
   */
  public PathExpression( IPath path)
  {
    this.path = path;
    this.path.setParent( this);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "path";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNodes(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    return path.query( context, null);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#isAbsolute()
   */
  public boolean isAbsolute( IContext context)
  {
    return path.isAbsolute( context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#createSubtree(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IModelObjectFactory, dunnagan.bob.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    ModelAlgorithms.createPathSubtree( context, path, factory, undo);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#bind(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
    try
    {
      // ignore initial notification when binding path because evaluating the expression from
      // the bottom up is more expensive (and difficult) then evaluating the path twice
      binding = true;
      path.addPathListener( context, this);
    }
    finally
    {
      binding = false;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#unbind(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
    try
    {
      // ignore final notification when unbinding path because evaluating the expression from
      // the bottom up is more expensive (and difficult) then evaluating the path twice
      binding = true;
      path.removePathListener( context, this);
    }
    finally
    {
      binding = false;
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IPathListener#notifyAdd(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IPath, int, java.util.List)
   */
  public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex < path.length() || parent == null) return;
    if ( !binding) parent.notifyAdd( this, context, nodes);
    
    // install value listener if required
    if ( parent.requiresValueNotification( this))
    {
      LeafValueListener listener = new LeafValueListener( this, context);
      for( IModelObject node: nodes) node.addModelListener( listener);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IPathListener#notifyRemove(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IPath, int, java.util.List)
   */
  public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
  {
    if ( pathIndex < path.length() || parent == null) return;
    if ( !binding) parent.notifyRemove( this, context, nodes);
    
    // uninstall value listener if required
    if ( parent.requiresValueNotification( this))
    {
      for( IModelObject node: nodes) 
      {
        LeafValueListener listener = LeafValueListener.findListener( node, this, context);
        if ( listener != null) 
          node.removeModelListener( listener);
        else 
          System.err.println( "Warning: LeafValueListener not found: "+this+", "+context+", "+node);
      }
    }
  }

  /**
   * Returns the path for this PathExpression.
   * @return Returns the path for this PathExpression.
   */
  public IPath getPath()
  {
    return path;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new PathExpression( path.clone());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return ModelAlgorithms.pathToString( path);
  }
  
  private IPath path;
  private boolean binding;
}
