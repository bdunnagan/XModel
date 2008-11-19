/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction.trigger;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.util.Aggregator;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpressionListener;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * A trigger which fires whenever anything changes in the subtree of an entity. Since changes to an entity
 * subtree can occur at high-frequency (such as during a diff), this class uses a thread-local aggregator.
 */
public class EntityTrigger extends AbstractTrigger
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.AbstractTrigger#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    entityExpr = document.getExpression( "entity", true);
    
    // get actions
    script = document.createScript( "entity");
    
    // get aggregator
    aggregator = new Aggregator( 50);
    
    // get index
    dispatchIndex = aggregator.add( document.getRoot().getModel().getDispatcher(), updateRunnable);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#activate(org.xmodel.xpath.expression.IContext)
   */
  public void activate( IContext context)
  {
    if ( context instanceof StatefulContext) 
      this.context = (StatefulContext)context;
    
    aggregator.start();
    entityExpr.addNotifyListener( context, entityListener);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#deactivate(org.xmodel.xpath.expression.IContext)
   */
  public void deactivate( IContext context)
  {
    entityExpr.removeListener( context, entityListener);
    aggregator.stop();
  }

  final IExpressionListener entityListener = new ExpressionListener() {
    public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      for( IModelObject node: nodes) listener.install( node);
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      for( IModelObject node: nodes) listener.uninstall( node);
      aggregator.dispatch( dispatchIndex);
    }
  };
  
  private NonSyncingListener listener = new NonSyncingListener() {
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyAddChild( parent, child, index);
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      aggregator.dispatch( dispatchIndex);
    }
  };
  
  // classes for asynchronous notification
  private final Runnable updateRunnable = new Runnable() {
    public void run()
    {
      log.info( "Trigger notifyUpdate(): "+EntityTrigger.this.toString());
      script.run( context);
    }
  };
  
  private IExpression entityExpr;
  private Aggregator aggregator;
  private int dispatchIndex;
  private ScriptAction script;
  private StatefulContext context;
}