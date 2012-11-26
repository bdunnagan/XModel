/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DereferenceFunction.java
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
import org.xmodel.INode;
import org.xmodel.ModelAlgorithms;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;


/**
 * A custom xpath function which returns the referent(s) of its argument node-set.
 */
public class DereferenceFunction extends Function
{
  public static final String name = "dereference";
  
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
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<INode> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    assertType( context, ResultType.NODES);
    List<INode> nodes = new ArrayList<INode>( getArgument( 0).evaluateNodes( context));
    for( int i=0; i<nodes.size(); i++) nodes.set( i, ModelAlgorithms.dereference( nodes.get( i)));
    return nodes;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<INode> nodes)
  {
    nodes = new ArrayList<INode>( nodes);
    for( int i=0; i<nodes.size(); i++) nodes.set( i, ModelAlgorithms.dereference( nodes.get( i)));
    getParent().notifyAdd( this, context, nodes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<INode> nodes)
  {
    nodes = new ArrayList<INode>( nodes);
    for( int i=0; i<nodes.size(); i++) nodes.set( i, ModelAlgorithms.dereference( nodes.get( i)));
    getParent().notifyRemove( this, context, nodes);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, INode object, Object newValue, Object oldValue)
  {
    getParent().notifyValue( this, contexts, ModelAlgorithms.dereference( object), newValue, oldValue);
  }
}
