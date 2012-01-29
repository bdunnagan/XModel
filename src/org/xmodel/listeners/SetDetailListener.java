package org.xmodel.listeners;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpressionListener;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An implementation of IExpressionListener that tracks changes to a node-set.  One or more listener/expression
 * pairs are installed or removed from nodes in the node-set when they are added or removed, respectively.
 */
public class SetDetailListener extends ExpressionListener
{
  public SetDetailListener()
  {
    details = new ArrayList<Detail>( 3);
  }
  
  /**
   * Add a detail listener with initial notification only.
   * @param expression The detail expression.
   * @param listener The listener.
   */
  public void addDetail( IExpression expression, IExpressionListener listener)
  {
    details.add( new Detail( expression, listener));
  }
  
  /**
   * Add a detail listener.
   * @param expression The detail expression.
   * @param listener The listener.
   * @param initialNotify True if initial notification should be performed.
   * @param finalNotify True if final notification should be performed.
   */
  public void addDetail( IExpression expression, IExpressionListener listener, boolean initialNotify, boolean finalNotify)
  {
    details.add( new Detail( expression, listener, initialNotify, finalNotify));
  }
  
  /**
   * Remove a detail listener.
   * @param expression The detail expression.
   */
  public void removeDetail( IExpression expression)
  {
    for( int i=0; i<details.size(); i++)
    {
      if ( details.get( i).expression == expression)
      {
        details.remove( i);
        return;
      }
    }
  }
  
  /**
   * Called after all of the detail listeners have been bound to a node that has been added.
   * @param context The node context.
   */
  protected void notifyDetailsBound( IContext context)
  {
  }
  
  /**
   * Called after all of the details listeners have been unbound from a node that has been removed.
   * @param context
   */
  protected void notifyDetailsUnbound( IContext context)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    for( IModelObject node: nodes)
    {
      StatefulContext nodeContext = new StatefulContext( context, node);
      for( Detail detail: details) detail.install( nodeContext);
      notifyDetailsBound( nodeContext);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    for( IModelObject node: nodes)
    {
      StatefulContext nodeContext = new StatefulContext( context, node);
      for( Detail detail: details) detail.remove( nodeContext);
      notifyDetailsUnbound( nodeContext);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#requiresValueNotification()
   */
  @Override
  public boolean requiresValueNotification()
  {
    return false;
  }

  private static class Detail
  {
    public Detail( IExpression expression, IExpressionListener listener)
    {
      this( expression, listener, true, false);
    }
    
    public Detail( IExpression expression, IExpressionListener listener, boolean initialNotify, boolean finalNotify)
    {
      this.expression = expression;
      this.listener = listener;
      this.initialNotify = initialNotify;
      this.finalNotify = finalNotify;
    }
    
    /**
     * Install detail listener.
     * @param context The node context.
     */
    public void install( StatefulContext context)
    {
      if ( initialNotify)
      {
        expression.addNotifyListener( context, listener);
      }
      else
      {
        expression.addListener( context, listener);
      }
    }
    
    /**
     * Remove detail listener.
     * @param context The node context.
     */
    public void remove( StatefulContext context)
    {
      if ( finalNotify)
      {
        expression.removeNotifyListener( context, listener);
      }
      else
      {
        expression.removeListener( context, listener);
      }
    }
    
    public IExpression expression;
    public IExpressionListener listener;
    public boolean initialNotify;
    public boolean finalNotify;
  }
  
  private List<Detail> details;
}
