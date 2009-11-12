/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * EvaluateFunction.java
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
package org.xmodel.xpath.function.custom;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.*;
import org.xmodel.xpath.function.Function;

/**
 * An XPath function that compiles and evaluates the expression returned by its second argument. The first
 * argument gives the context in which the expression will be evaluated. The third argument defines the 
 * return type of the function and the default value if the expression cannot be compiled.
 */
public class EvaluateFunction extends Function
{
  public final static String name = "evaluate";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.UNDEFINED;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getType(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    return getArgument( 2).getType( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertArgs( 3, 3);
    assertType( context, 0, ResultType.NODES);

    String spec = getArgument( 1).evaluateString( context);
    IExpression expr = XPath.createExpression( spec, false);
    if ( expr == null) return getArgument( 2).evaluateBoolean( context);
    
    for( IModelObject object: getArgument( 0).evaluateNodes( context))
    {
      if ( !expr.evaluateBoolean( new Context( context, object))) 
        return false;
    }
    
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 3, 3);
    assertType( context, 0, ResultType.NODES);

    String spec = getArgument( 1).evaluateString( context);
    IExpression expr = XPath.createExpression( spec, false);
    if ( expr == null) return getArgument( 2).evaluateNodes( context);
    
    List<IModelObject> nodes = getArgument( 0).evaluateNodes( context);
    List<IModelObject> result = new ArrayList<IModelObject>();
    for( IModelObject object: nodes)
      result.addAll( expr.evaluateNodes( new Context( context, object))); 
    
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    assertArgs( 3, 3);
    assertType( context, 0, ResultType.NODES);

    String spec = getArgument( 1).evaluateString( context);
    IExpression expr = XPath.createExpression( spec, false);
    if ( expr == null) return getArgument( 2).evaluateNumber( context);
    
    IModelObject object = getArgument( 0).queryFirst( context);
    return expr.evaluateNumber( new Context( context, object)); 
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 3, 3);
    assertType( context, 0, ResultType.NODES);

    String spec = getArgument( 1).evaluateString( context);
    IExpression expr = XPath.createExpression( spec, false);
    if ( expr == null) return getArgument( 2).evaluateString( context);
    
    IModelObject object = getArgument( 0).queryFirst( context);
    return expr.evaluateString( new Context( context, object)); 
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext[], 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    getParent().notifyChange( this, contexts[ 0]);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return argument == getArgument( 1);
  }
}
