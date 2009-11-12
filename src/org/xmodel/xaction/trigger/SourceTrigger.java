/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SourceTrigger.java
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

import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.ExpressionListener;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpressionListener;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A trigger which fires whenever an expression changes. One or more variables are provided to the
 * trigger script depending on the type of the expression. If the expression returns a node-set then
 * the $added and $removed variables contain the nodes which were added or removed from the node-set.
 * If the expression returns another value, then the $update variable contains the new value.
 */
public class SourceTrigger extends AbstractTrigger
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // get trigger expression from source attribute or element
    triggerExpr = document.getExpression( "source", true);
    
    // get actions
    script = document.createScript( "initialize", "finalize", "source");
    
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
      triggerExpr.addNotifyListener( context, conditionListener);
    }
    else
    {
      triggerExpr.addListener( context, conditionListener);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#deactivate(org.xmodel.xpath.expression.IContext)
   */
  public void deactivate( IContext context)
  {
    if ( finalize)
    {
      triggerExpr.removeNotifyListener( context, conditionListener);
    }
    else
    {
      triggerExpr.removeListener( context, conditionListener);
    }
  }
  
  /**
   * Set the $trigger variable.
   * @param context The context.
   * @param variable The variable name.
   * @param node The triggering node.
   */
  private void setTriggerVariable( IContext context, String variable, List<IModelObject> nodes)
  {
    IVariableScope scope = context.getScope();
    if ( scope != null) 
    {
      scope.set( "added", Collections.<IModelObject>emptyList()); 
      scope.set( "removed", Collections.<IModelObject>emptyList()); 
      scope.set( "updated", ""); 
      scope.set( variable, nodes);
    }
  }

  final IExpressionListener conditionListener = new ExpressionListener() {
    public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      if ( updating) return;
      updating = true;
      
      try
      {
        NotifyAdd runnable = new NotifyAdd();
        runnable.context = context;
        runnable.nodes = nodes;
        context.getModel().dispatch( runnable);
      }
      finally
      {
        updating = false;
      }
    }
    public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
    {
      if ( updating) return;
      updating = true;

      try
      {
        NotifyRemove runnable = new NotifyRemove();
        runnable.context = context;
        runnable.nodes = nodes;
        context.getModel().dispatch( runnable);
      }
      finally
      {
        updating = false;
      }
    }
    public void notifyChange( IExpression expression, IContext context, boolean newValue)
    {
      if ( updating) return;
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
    public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
    {
      if ( updating) return;
      updating = true;
      
      try
      {
        NotifyNumberChange runnable = new NotifyNumberChange();
        runnable.context = context;
        runnable.newValue = newValue;
        runnable.oldValue = oldValue;
        context.getModel().dispatch( runnable);
      }
      finally
      {
        updating = false;
      }
    }
    public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
    {
      if ( updating) return;
      updating = true;

      try
      {
        NotifyStringChange runnable = new NotifyStringChange();
        runnable.context = context;
        runnable.newValue = newValue;
        runnable.oldValue = oldValue;
        context.getModel().dispatch( runnable);
      }
      finally
      {
        updating = false;
      }
    }
    public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
    {
      if ( updating) return;
      updating = true;

      try
      {
        NotifyValueChange runnable = new NotifyValueChange();
        runnable.context = contexts[ 0];
        runnable.node = object;
        runnable.newValue = (newValue != null)? newValue.toString(): "";
        runnable.oldValue = (oldValue != null)? oldValue.toString(): "";
        contexts[ 0].getModel().dispatch( runnable);
      }
      finally
      {
        updating = false;
      }
    }
    public boolean requiresValueNotification()
    {
      return true;
    }
  };
  
  // classes for asynchronous notification
  private class NotifyAdd implements Runnable
  {
    public void run()
    {
      System.out.println( "Trigger notifyAdd( "+nodes.size()+" nodes): "+SourceTrigger.this.toString());
      setTriggerVariable( context, "added", nodes); 
      script.run( context);
    }
    
    IContext context;
    List<IModelObject> nodes;
  }

  private class NotifyRemove implements Runnable
  {
    public void run()
    {
      System.out.println( "Trigger notifyRemove( "+nodes.size()+" nodes): "+SourceTrigger.this.toString());
      setTriggerVariable( context, "removed", nodes); 
      script.run( context);
    }
    
    IContext context;
    List<IModelObject> nodes;
  }
  
  private class NotifyStringChange implements Runnable
  {
    public void run()
    {
      System.out.println( "Trigger notifyChange( "+newValue+", "+oldValue+"): "+SourceTrigger.this.toString());
      script.run( context);
    }

    IContext context;
    String oldValue;
    String newValue;
  }
  
  private class NotifyValueChange implements Runnable
  {
    public void run()
    {
      System.out.println( "Trigger notifyChange( "+newValue+", "+oldValue+"): "+SourceTrigger.this.toString());
      setTriggerVariable( context, "updated", Collections.singletonList( node));
      script.run( context);
    }

    IContext context;
    IModelObject node;
    String oldValue;
    String newValue;
  }
  
  private class NotifyNumberChange implements Runnable
  {
    public void run()
    {
      System.out.println( "Trigger notifyChange( "+newValue+", "+oldValue+"): "+SourceTrigger.this.toString());
      script.run( context);
    }

    IContext context;
    double oldValue;
    double newValue;
  }
  
  private class NotifyBooleanChange implements Runnable
  {
    public void run()
    {
      System.out.println( "Trigger notifyChange( "+newValue+"): "+SourceTrigger.this.toString());
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
}
