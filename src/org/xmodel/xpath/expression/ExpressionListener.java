/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.xmodel.IModel;
import org.xmodel.IModelObject;


/**
 * An implementation of IExpressionListener with empty method stubs except for the indeterminate
 * callback method <code>notifyChange( IExpression, IContext)</code> which reevaluates the 
 * expression once with the last update reverted and once with the update restored so that it
 * can call one of the other notification methods.
 */
public class ExpressionListener implements IExpressionListener
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  public void notifyChange( IExpression expression, IContext context)
  {
    try
    {
      IModel model = context.getModel();
      switch( expression.getType( context))
      {
        case NODES:
        {
          // revert and reevaluate
          model.revert();
          Collection<IModelObject> oldNodes = expression.evaluateNodes( context);
          if ( oldNodes.size() > 3) oldNodes = new LinkedHashSet<IModelObject>( oldNodes);
  
          // restore and reevaluate
          model.restore();
          Collection<IModelObject> newNodes = expression.evaluateNodes( context);
          if ( newNodes.size() > 3) newNodes = new LinkedHashSet<IModelObject>( newNodes);
  
          // notify nodes removed
          List<IModelObject> removedSet = new ArrayList<IModelObject>( newNodes.size());
          for( IModelObject node: oldNodes) if ( !newNodes.contains( node)) removedSet.add( node);
          if ( removedSet.size() > 0) notifyRemove( expression, context, removedSet);
          
          // notify nodes added
          List<IModelObject> addedSet = new ArrayList<IModelObject>( newNodes.size());
          for( IModelObject node: newNodes) if ( !oldNodes.contains( node)) addedSet.add( node);
          if ( addedSet.size() > 0) notifyAdd( expression, context, addedSet);
        }
        break;
        
        case NUMBER:
        {
          // revert and reevaluate
          model.revert();
          double oldValue = expression.evaluateNumber( context);
  
          // restore and reevaluate
          model.restore();
          double newValue = expression.evaluateNumber( context);
          
          if ( newValue != oldValue) notifyChange( expression, context, newValue, oldValue);
        }
        break;
        
        case BOOLEAN:
        {
          // revert and reevaluate
          model.revert();
          boolean oldValue = expression.evaluateBoolean( context);
  
          // restore and reevaluate
          model.restore();
          boolean newValue = expression.evaluateBoolean( context);
          
          if ( newValue != oldValue) notifyChange( expression, context, newValue);
        }
        break;
        
        case STRING:
        {
          // revert and reevaluate
          model.revert();
          String oldValue = expression.evaluateString( context);
  
          // restore and reevaluate
          model.restore();
          String newValue = expression.evaluateString( context);
          
          if ( !newValue.equals( oldValue)) notifyChange( expression, context, newValue, oldValue);
        }
        break;
      }      
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#requiresValueNotification()
   */
  public boolean requiresValueNotification()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyValue(
   * org.xmodel.xpath.expression.IExpression, java.util.Collection, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    // legacy semantic: convert value notification to string notification.
    // this may be okay since an implicit cast for the purpose of notification is not ambiguous
    for( IContext context: contexts)
    {
      String newResult = (newValue != null)? newValue.toString(): "";
      String oldResult = (oldValue != null)? oldValue.toString(): "";
      notifyChange( expression, context, newResult, oldResult);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#handleException(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.Exception)
   */
  public void handleException( IExpression expression, IContext context, Exception e)
  {
    System.err.println( "Expression Error: "+expression+", "+context);
    e.printStackTrace( System.err);
  }
}
