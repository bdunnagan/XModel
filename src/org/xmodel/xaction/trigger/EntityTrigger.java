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
import org.xmodel.external.NonSyncingListener;
import org.xmodel.log.SLog;
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
    script = document.createScript( "entity");
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#activate(org.xmodel.xpath.expression.IContext)
   */
  public void activate( IContext context)
  {
    this.context = (StatefulContext)context;
    entityExpr.addNotifyListener( context, entityListener);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#deactivate(org.xmodel.xpath.expression.IContext)
   */
  public void deactivate( IContext context)
  {
    entityExpr.removeListener( context, entityListener);
  }

  private void dispatch()
  {
    context.getModel().getDispatcher().execute( dispatch1);
  }
  
  final IExpressionListener entityListener = new ExpressionListener() {
    public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      if ( touched.size() == 0) dispatch();
      for( IModelObject node: nodes) listener.install( node);
    }
    public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      if ( touched.size() == 0) dispatch();
      for( IModelObject node: nodes) listener.uninstall( node);
    }
  };
  
  private NonSyncingListener listener = new NonSyncingListener() {
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyAddChild( parent, child, index);
      if ( touched.size() == 0) dispatch();
      touched.add( parent);
    }
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      if ( touched.size() == 0) dispatch();
      touched.add( parent);
    }
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      if ( touched.size() == 0) dispatch();
      touched.add( object);
    }
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      if ( touched.size() == 0) dispatch();
      touched.add( object);
    }
  };
  
  private final Runnable dispatch1 = new Runnable() {
    public void run()
    {
      context.getModel().getDispatcher().execute( dispatch2);
    }
  };
  
  private final Runnable dispatch2 = new Runnable() {
    public void run()
    {
      SLog.debugf( EntityTrigger.this, "Trigger notifyUpdate(): %s", EntityTrigger.this.toString());
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
  private ScriptAction script;
  private StatefulContext context;
  private Set<IModelObject> touched;
}