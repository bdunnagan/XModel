/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function.custom;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.IPathListener;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.xpath.expression.Context;
import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.function.Function;

/**
 * A custom xpath function which constructs a path specification. The function has two forms. The
 * single argument version returns the identity path of the argument. The two argument version
 * returns the relative path from the first argument to the second argument. 
 * TODO: This function does not install listeners to know when the position of an ancestor changes.
 */
public class CreatePathFunction extends Function
{
  public final static String name = "create-path";
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateString(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 1, 2);
    assertType( context, ResultType.NODES);

    List<IModelObject> nodes = getArgument( 0).evaluateNodes( context);
    if ( nodes.size() == 0) return "";
    IPath path = createPath( context, nodes.get( 0));
    return (path != null)? path.toString(): "";
  }
  
  /**
   * Evaluates the arguments of the function and returns either the relative path from the
   * first node of the first argument to the first node of the second argument, or the 
   * identity path of the first node of the first argument.
   * @param context The context of the argument evaluations.
   * @param start The start node.
   * @return Returns either the relative path or the identity path for the arguments.
   */
  private IPath createPath( IContext context, IModelObject start) throws ExpressionException
  {
    IExpression arg1 = getArgument( 1);
    if ( arg1 != null)
    {
      List<IModelObject> nodes = arg1.evaluateNodes( context);
      IModelObject end = (nodes.size() > 0)? nodes.get( 0): null;
      if ( end != null) return ModelAlgorithms.createRelativePath( start, end);
    }
    return ModelAlgorithms.createIdentityPath( start);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#bind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    super.bind( context);
    try
    {
      List<IModelObject> nodes = getArgument( 0).evaluateNodes( context);
      if ( nodes.size() > 0)
      {
        IModelObject start = nodes.get( 0);
        IPath path = createPath( context, start);
        path.addPathListener( new Context( start), listener);
      }
    }
    catch( ExpressionException e)
    {
      handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#unbind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    super.unbind( context);
    try
    {
      List<IModelObject> nodes = getArgument( 0).evaluateNodes( context);
      if ( nodes.size() > 0)
      {
        IModelObject start = nodes.get( 0);
        IPath path = createPath( context, start);
        path.removePathListener( new Context( start), listener);
      }
    }
    catch( ExpressionException e)
    {
      handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyAdd(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyRemove(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }
  
  final IPathListener listener = new IPathListener() {
    public void notifyAdd( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
    {
      getParent().notifyChange( CreatePathFunction.this, context);
    }
    public void notifyRemove( IContext context, IPath path, int pathIndex, List<IModelObject> nodes)
    {
      getParent().notifyChange( CreatePathFunction.this, context);
    }
  };
}
