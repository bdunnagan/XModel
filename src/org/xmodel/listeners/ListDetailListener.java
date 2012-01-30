package org.xmodel.listeners;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExactExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpressionListener;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An ExactExpressionListener that tracks changes to a node-set.  One or more listener/expression
 * pairs are installed or removed from nodes in the node-set when they are added or removed, respectively.
 */
public class ListDetailListener extends ExactExpressionListener
{
  public ListDetailListener()
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
   * @param index The index of the point in the list.
   */
  protected void notifyDetailsBound( IContext context, int index)
  {
  }
  
  /**
   * Called after all of the details listeners have been unbound from a node that has been removed.
   * @param context The node context.
   * @param index The index of the point in the list.
   */
  protected void notifyDetailsUnbound( IContext context, int index)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExactExpressionListener#notifyInsert(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List, int, int)
   */
  @Override
  public void notifyInsert( IExpression expression, IContext context, List<IModelObject> nodes, int start, int count)
  {
    for( int i=0; i<count; i++)
    {
      StatefulContext nodeContext = new StatefulContext( context, nodes.get( start + i));
      for( Detail detail: details) detail.install( nodeContext);
      notifyDetailsBound( nodeContext, start + i);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExactExpressionListener#notifyRemove(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List, int, int)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes, int start, int count)
  {
    for( int i=count-1; i>=0; i--)
    {
      StatefulContext nodeContext = new StatefulContext( context, nodes.get( start + i));
      for( Detail detail: details) detail.remove( nodeContext);
      notifyDetailsUnbound( nodeContext, start + i);
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
      this.expression = expression;
      this.listener = listener;
    }
    
    /**
     * Install detail listener.
     * @param context The node context.
     */
    public void install( StatefulContext context)
    {
      expression.addNotifyListener( context, listener);
    }
    
    /**
     * Remove detail listener.
     * @param context The node context.
     */
    public void remove( StatefulContext context)
    {
      expression.removeListener( context, listener);
    }
    
    public IExpression expression;
    public IExpressionListener listener;
  }
  
  private List<Detail> details;
}
