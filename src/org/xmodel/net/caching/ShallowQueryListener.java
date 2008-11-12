package org.xmodel.net.caching;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.net.caching.QueryProtocol.ServerQuery;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An expression listener which reports to an instance of QueryProtocol whenever an event is received.
 */
public class ShallowQueryListener extends ExpressionListener
{
  public ShallowQueryListener( QueryProtocol protocol, ServerQuery query)
  {
    this.protocol = protocol;
    this.query = query;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    protocol.sendAddUpdate( query, nodes); 
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    protocol.sendRemoveUpdate( query, nodes); 
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    protocol.sendChangeUpdate( query, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    protocol.sendChangeUpdate( query, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    protocol.sendChangeUpdate( query, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    protocol.sendValueUpdate( query, (newValue == null)? null: newValue.toString());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#requiresValueNotification()
   */
  @Override
  public boolean requiresValueNotification()
  {
    // TODO Auto-generated method stub
    return super.requiresValueNotification();
  }
  private QueryProtocol protocol;
  private ServerQuery query;
}
