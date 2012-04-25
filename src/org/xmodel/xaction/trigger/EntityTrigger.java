/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * EntityTrigger.java
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
package org.xmodel.xaction.trigger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.log.Log;
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
  public EntityTrigger()
  {
    touched = new HashSet<IModelObject>();
  }
  
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
    aggregator = new Aggregator( Xlate.get( document.getRoot(), "delay", 50));
    
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
      touched.add( parent);
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      touched.add( parent);
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      touched.add( object);
      aggregator.dispatch( dispatchIndex);
    }
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      touched.add( object);
      aggregator.dispatch( dispatchIndex);
    }
  };
  
  // classes for asynchronous notification
  private final Runnable updateRunnable = new Runnable() {
    public void run()
    {
      log.debugf( "Trigger notifyUpdate(): %s", EntityTrigger.this.toString());
      context.set( "changes", new ArrayList<IModelObject>( touched));
      script.run( context);
      touched.clear();
    }
  };
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.AbstractTrigger#toString()
   */
  @Override
  public String toString()
  {
    return String.format( "EntityTrigger: %s", entityExpr);
  }

  private IExpression entityExpr;
  private Aggregator aggregator;
  private int dispatchIndex;
  private ScriptAction script;
  private StatefulContext context;
  private Set<IModelObject> touched;
  
  private static Log log = Log.getLog( "org.xmodel.xaction.trigger");
}