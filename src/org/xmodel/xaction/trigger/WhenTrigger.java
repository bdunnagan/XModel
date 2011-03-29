/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * WhenTrigger.java
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

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpressionListener;

/**
 * A trigger which fires when an expression transitions from false to true.
 */
public class WhenTrigger extends AbstractTrigger
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.AbstractTrigger#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    triggerExpr = document.getExpression( "when", true);
        
    // get actions
    script = document.createScript( "initialize", "finalize", "source", "when");
    
    // get flags
    initialize = Xlate.get( document.getRoot(), "initialize", false);
    finalize = Xlate.get( document.getRoot(), "finalize", false);
    
    // backwards compatability
    initialize = Xlate.get( document.getRoot().getFirstChild( "initialize"), initialize);
    finalize = Xlate.get( document.getRoot().getFirstChild( "finalize"), finalize);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#activate(org.xmodel.xpath.expression.IContext)
   */
  public void activate( IContext context)
  {
    if ( initialize)
    {
      triggerExpr.addNotifyListener( context, listener);
    }
    else
    {
      triggerExpr.addListener( context, listener);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#deactivate(org.xmodel.xpath.expression.IContext)
   */
  public void deactivate( IContext context)
  {
    if ( finalize)
    {
      triggerExpr.removeNotifyListener( context, listener);
    }
    else
    {
      triggerExpr.removeListener( context, listener);
    }
  }

  final IExpressionListener listener = new ExpressionListener() {
    public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
    {
    }
    public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
    {
    }
    public void notifyChange( IExpression expression, IContext context, boolean newValue)
    {
      if ( updating) return;
      if ( newValue)
      {
        updating = true;
        try
        {
          NotifyBooleanChange runnable = new NotifyBooleanChange();
          runnable.context = context;
          runnable.newValue = newValue;
          context.getModel().dispatch( runnable);
        }
        finally
        {
          updating = false;
        }
      }
    }
    public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
    {
    }
    public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
    {
    }
    public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
    {
    }
    public boolean requiresValueNotification()
    {
      return false;
    }
  };
  
  private class NotifyBooleanChange implements Runnable
  {
    public void run()
    {
      log.debugf( "Trigger notifyChange( %s): %s", Boolean.toString( newValue), WhenTrigger.this.toString());
      script.run( context);
    }

    IContext context;
    boolean newValue;
  }
    
  private IExpression triggerExpr;
  private ScriptAction script;
  private boolean initialize;
  private boolean finalize;
  private boolean updating;
  
  private static Log log = Log.getLog( "org.xmodel.xaction.trigger");
}
