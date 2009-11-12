/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ReverseFunction.java
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
package org.xmodel.xpath.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An X-Path function which reverses the order of the nodes in a node-set.
 */
public class ReverseFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "reverse";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    assertType( context, 0, ResultType.NODES);
    
    IExpression arg0 = getArgument( 0);
    List<IModelObject> nodes = arg0.evaluateNodes( context);
    Collections.reverse( nodes);
    
    return nodes;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression parent = getParent();
    if ( parent != null) 
    {
      // obviously, this only reverses the order of the nodes which were added, but
      // this allows the listener to find the position of the first node in the node-set
      // and calculate the other positions from it.
      nodes = new ArrayList<IModelObject>( nodes);
      Collections.reverse( nodes);
      parent.notifyAdd( this, context, nodes);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    IExpression parent = getParent();
    if ( parent != null) 
    {
      // obviously, this only reverses the order of the nodes which were removed, but
      // this allows the listener to find the position of the first node in the node-set
      // and calculate the other positions from it.
      nodes = new ArrayList<IModelObject>( nodes);
      Collections.reverse( nodes);
      parent.notifyRemove( this, context, nodes);
    }
  }
}
