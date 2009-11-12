/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * RootExpression.java
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
package org.xmodel.xpath.expression;

import java.util.List;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.variable.*;


/**
 * An expression which is placed at the root of every expression tree and forwards notifications
 * from its child to registered IExpressionListener instances. This class exists so that the
 * parent/child notification mechanism can be converted into the context-aware notification 
 * provided by the <code>ExpressionListenerList</code> class.
 */
public class RootExpression extends Expression
{
  public RootExpression()
  {
  }
  
  /**
   * Create a root expression for the specified expression tree.
   * @param tree The expression tree.
   */
  public RootExpression( IExpression tree)
  {
    addArgument( tree);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "root";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    IExpression arg0 = getArgument( 0);
    if ( arg0 == null) throw new IllegalStateException( "RootExpression is missing child.");
    return arg0.getType();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getType(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    if ( arg0 == null) throw new IllegalStateException( "RootExpression is missing child.");
    return arg0.getType( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getRoot()
   */
  public IExpression getRoot()
  {
    return this;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#internal_setParent(org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public void internal_setParent( IExpression parent)
  {
    super.internal_setParent( parent);
    System.err.println( "warning: RootExpression was parented.");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    IExpression arg0 = getArgument( 0);
    if ( arg0 == null) throw new ExpressionException( this, "RootExpression has no arguments.");
    return arg0.evaluateBoolean( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    IExpression arg0 = getArgument( 0);
    if ( arg0 == null) throw new ExpressionException( this, "RootExpression has no arguments.");
    return arg0.evaluateNodes( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    IExpression arg0 = getArgument( 0);
    if ( arg0 == null) throw new ExpressionException( this, "RootExpression has no arguments.");
    return arg0.evaluateNumber( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#evaluateString(
   * org.xmodel.xpath.expression.IContext)
   */
  public String evaluateString( IContext context) throws ExpressionException
  {
    IExpression arg0 = getArgument( 0);
    if ( arg0 == null) throw new ExpressionException( this, "RootExpression has no arguments.");
    return arg0.evaluateString( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#createSubtree(org.xmodel.xpath.expression.IContext, 
   * org.xmodel.IModelObjectFactory, org.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    getArgument( 0).createSubtree( context, factory, undo);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getLocalScope()
   */
  @Override
  public IVariableScope getLocalScope()
  {
    return getVariableSource().getScope( "local");
  }

  /**
   * (internal use only) Set the IVariableSource used by this expression.
   * @param variableSource The variable source.
   */
  public void setVariableSource( IVariableSource variableSource)
  {
    this.variableSource = variableSource;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getVariableSource()
   */
  @Override
  public IVariableSource getVariableSource()
  {
    if ( variableSource == null) 
    {
      variableSource = new VariableSource();
      if ( variableSource.getScope( "local") == null)
        variableSource.addScope( new VariableScope( "local", Precedences.localScope));
    }
    return variableSource;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#addListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  public void addListener( IContext context, IExpressionListener listener)
  {
    if ( context == null)
      throw new IllegalArgumentException( 
        "Attempt to add listener on null context: path:"+toString());
    
    if ( listeners == null) listeners = new ExpressionListenerList();
    listeners.addListener( context, listener);
    
    try
    {
      getVariableSource().addScope( context.getScope());
      bind( context);
    }
    finally
    {
      getVariableSource().removeScope( context.getScope());
    }
    
    context.notifyBind( this);
    context.notifyUpdate( this);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#removeListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  public void removeListener( IContext context, IExpressionListener listener)
  {
    if ( context == null)
      throw new IllegalArgumentException( 
        "Attempt to remove listener from null context: path:"+toString());
    
    if ( listeners != null) 
    {
      listeners.removeListener( context, listener);

      try
      {
        getVariableSource().addScope( context.getScope());
        unbind( context);
      }
      finally
      {
        getVariableSource().removeScope( context.getScope());
      }
      
      context.notifyUnbind( this);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#addNotifyListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  public void addNotifyListener( IContext context, IExpressionListener listener)
  {
    if ( context == null)
      throw new IllegalArgumentException( 
        "Attempt to add listener on null context: path:"+toString());
    
    if ( listeners == null) listeners = new ExpressionListenerList();
    listeners.addListener( context, listener);

    try
    {
      getVariableSource().addScope( context.getScope());
      bind( context);
    }
    finally
    {
      getVariableSource().removeScope( context.getScope());
    }
    
    // notify context and listener
    try
    {
      context.notifyBind( this);
      performInitialNotification( context, listener);
      context.notifyUpdate( this);
    }
    catch( ExpressionException e)
    {
      listener.handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#removeNotifyListener(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpressionListener)
   */
  public void removeNotifyListener( IContext context, IExpressionListener listener)
  {
    if ( context == null)
      throw new IllegalArgumentException( 
        "Attempt to remove listener from null context: path:"+toString());
    
    if ( listeners != null && listeners.removeListener( context, listener))
    {
      try
      {
        getVariableSource().addScope( context.getScope());
        unbind( context); 
      }
      finally
      {
        getVariableSource().removeScope( context.getScope());
      }
      
      // notify context and listener
      try
      {
        context.notifyUnbind( this);
        performFinalNotification( context, listener);
      }
      catch( ExpressionException e)
      {
        listener.handleException( this, context, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getListeners()
   */
  public ExpressionListenerList getListeners()
  {
    if ( listeners == null) listeners = new ExpressionListenerList();
    return listeners;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    RootExpression clone = new RootExpression();

    // clone local scope
    IVariableScope scope = getVariableSource().getScope( "local");
    if ( scope != null) clone.getVariableSource().addScope( scope.cloneOne());
    
    // clone let scope
    scope = getVariableSource().getScope( "let");
    if ( scope != null) clone.getVariableSource().addScope( scope.cloneOne());
    
    return clone;
  }

  /**
   * Perform the initial notification for the listeners of this expression.
   * @param context The context.
   * @param listener The listener.
   */
  protected void performInitialNotification( IContext context, IExpressionListener listener) throws ExpressionException
  {
    // check if should update
    boolean shouldUpdate = context.shouldUpdate( this);
    
    try
    {
      // perform notification
      switch( getType( context))
      {
        case NODES:
          List<IModelObject> nodes = evaluateNodes( context);
          if ( nodes.size() > 0) listener.notifyAdd( this, context, nodes);
          break;
        
        case STRING:
          listener.notifyChange( this, context, evaluateString( context), "");
          break;
       
        case NUMBER:
          listener.notifyChange( this, context, evaluateNumber( context), 0);
          break;
          
        case BOOLEAN:
          listener.notifyChange( this, context, evaluateBoolean( context));
          break;
      }
    }
    catch( ExpressionException e)
    {
      listener.handleException( this, context, e);
    }
    
    // make context updateable again if necessary
    if ( shouldUpdate) context.markUpdate( this);
  }
  
  /**
   * Perform the final notification for the listeners of this expression.
   * @param context The context.
   * @param listener The listener.
   */
  protected void performFinalNotification( IContext context, IExpressionListener listener) 
  throws ExpressionException
  {
    // check if should update
    boolean shouldUpdate = context.shouldUpdate( this);
    
    try
    {
      // perform notification
      switch( getType( context ))
      {
        case NODES:
          List<IModelObject> nodes = evaluateNodes( context);
          if ( nodes.size() > 0) listener.notifyRemove( this, context, nodes);
          break;
        
        case STRING:
          listener.notifyChange( this, context, "", evaluateString( context));
          break;
       
        case NUMBER:
          listener.notifyChange( this, context, 0, evaluateNumber( context));
          break;
          
        case BOOLEAN:
          listener.notifyChange( this, context, !evaluateBoolean( context));
          break;
      }
    }
    catch( ExpressionException e)
    {
      listener.handleException( this, context, e);
    }

    // make context updateable again if necessary
    if ( shouldUpdate) context.markUpdate( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    updating = true;
    try
    {
      if ( listeners != null) listeners.notifyAdd( this, context, nodes);
    }
    finally
    {
      updating = false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    updating = true;
    try
    {
      if ( listeners != null) listeners.notifyRemove( this, context, nodes);
    }
    finally
    {
      updating = false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    updating = true;
    try
    {
      if ( listeners != null) listeners.notifyChange( this, context, newValue);
    }
    finally
    {
      updating = false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    updating = true;
    try
    {
      if ( listeners != null) listeners.notifyChange( this, context, newValue, oldValue);
    }
    finally
    {
      updating = false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    updating = true;
    try
    {
      if ( listeners != null) listeners.notifyChange( this, context, newValue, oldValue);
    }
    finally
    {
      updating = false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    updating = true;
    try
    {
      if ( listeners != null) 
      {
        if ( context.shouldUpdate( this))
        {
          context.notifyUpdate( this);
          listeners.notifyChange( this, context);
        }
      }
    }
    finally
    {
      updating = false;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#handleException(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.Exception)
   */
  public void handleException( IExpression expression, IContext context, Exception e)
  {
    if ( listeners != null) listeners.handleException( this, context, e);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return (listeners != null && listeners.requiresValueNotification());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, 
   * java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    if ( listeners != null) listeners.notifyValue( this, contexts, object, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    IExpression tree = getArgument( 0);
    return (tree != null)? tree.toString(): "(?)";
  }
  
  IVariableSource variableSource;
  ExpressionListenerList listeners;
  boolean updating;
  
  public static void main( String[] args) throws Exception
  {
    String xml = 
      "<office>" +
      "  <employees>" +
      "    <employee id='1'>" +
      "      <name>Bob</name>" +
      "      <area>gui</area>" +
      "    </employee>" +
      "    <employee id='2'>" +
      "      <name>Cam</name>" +
      "      <area>gui</area>" +
      "      <area>ps</area>" +
      "    </employee>" +
      "    <employee id='3'>" +
      "      <name>Jeff</name>" +
      "      <area>ps</area>" +
      "      <area>dm</area>" +
      "    </employee>" +
      "  </employees>" +
      "</office>";
    
    XmlIO xmlIO = new XmlIO();
    IModelObject root = xmlIO.read( xml);
    StatefulContext c1 = new StatefulContext( root.getFirstChild( "employees").getChild( 1));
    StatefulContext c2 = new StatefulContext( root.getFirstChild( "employees").getChild( 1));
    
    IExpression expr = XPath.createExpression( 
      "let $employees := /office/employees/employee[ @id = $id];" +
      "let $ids := /office/employees/employee[ @id = $employees/@id]/@id;" +
      "string( $employees[ @id = $ids]/@id)");

//    IExpression expr = XPath.createExpression( 
//      "string( /office/employees/employee[ @id = $id]/name)");

    c1.set( "id", "1");
    c2.set( "id", "1");
    expr.addNotifyListener( c1, new ExpressionListener() {
      public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
      {
        System.out.println( "c1="+newValue);
      }
    });
    expr.addNotifyListener( c2, new ExpressionListener() {
      public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
      {
        System.out.println( "c2="+newValue);
      }
    });

    c1.set( "id", "2");
    
    System.out.println( expr);
  }
}

