/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
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
public class QueryListener extends ExpressionListener
{
  public QueryListener( QueryProtocol protocol, ServerQuery query, boolean deep)
  {
    this.protocol = protocol;
    this.query = query;
    if ( deep) deepListener = new DeepListener( protocol, query);
  }
  
  /**
   * Specify whether the listener should generate messages.
   * @param silent True if listener should not generate messages.
   */
  public void setSilent( boolean silent)
  {
    this.silent = silent;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( !silent) protocol.sendAddUpdate( query, nodes);
    
    if ( deepListener != null)
    {
      for( IModelObject node: nodes)
        deepListener.install( node);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( !silent) protocol.sendRemoveUpdate( query, nodes); 

    if ( deepListener != null)
    {
      for( IModelObject node: nodes)
        deepListener.uninstall( node);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    if ( !silent) protocol.sendChangeUpdate( query, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    if ( !silent) protocol.sendChangeUpdate( query, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    if ( !silent) protocol.sendChangeUpdate( query, newValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( !silent) protocol.sendValueUpdate( query, (newValue == null)? null: newValue.toString());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.ExpressionListener#requiresValueNotification()
   */
  @Override
  public boolean requiresValueNotification()
  {
    return true;
  }
  
  private QueryProtocol protocol;
  private ServerQuery query;
  private DeepListener deepListener;
  private boolean silent;
}
