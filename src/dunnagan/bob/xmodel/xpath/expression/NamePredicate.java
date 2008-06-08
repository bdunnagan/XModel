/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.HashMap;
import java.util.Map;

import dunnagan.bob.xmodel.IModelListener;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.ModelListener;

/**
 * An implementation of IExpression which evaluates boolean and returns true if the context object has the
 * name with which the NamePredicate was constructed.
 */
public class NamePredicate extends PredicateExpression
{
  public NamePredicate( IPath path, String objectName)
  {
    super( path);
    this.objectName = objectName;
    this.contexts = new HashMap<IModelObject, IContext>();
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "name-predicate";
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    return context.getObject().getID().equals( objectName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#bind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    context.getObject().addModelListener( nameListener);
    contexts.put( context.getObject(), context);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#unbind(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    context.getObject().removeModelListener( nameListener);
    contexts.remove( context.getObject());
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new NamePredicate( path, objectName);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.PredicateExpression#toString()
   */
  @Override
  public String toString()
  {
    return "[@id='"+objectName+"']";
  }

  final IModelListener nameListener = new ModelListener() {
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      if ( listeners == null) return;
      if ( attrName.equals( "id"))
      {
        if ( oldValue != null && oldValue.equals( objectName)) 
        {
          IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
          for( IExpressionListener listener: array) 
            listener.notifyChange( NamePredicate.this, contexts.get( object), false);
        }
        if ( newValue != null && newValue.equals( objectName))
        {
          IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
          for( IExpressionListener listener: array) 
            listener.notifyChange( NamePredicate.this, contexts.get( object), true);
        }
      }
    }
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      if ( listeners == null) return;
      if ( attrName.equals( "id"))
      {
        if ( oldValue != null && oldValue.equals( objectName)) 
        {
          IExpressionListener[] array = listeners.toArray( new IExpressionListener[ 0]);
          for( IExpressionListener listener: array) 
            listener.notifyChange( NamePredicate.this, contexts.get( object), false);
        }
      }
    }
  };
  
  String objectName;
  Map<IModelObject, IContext> contexts;
}
