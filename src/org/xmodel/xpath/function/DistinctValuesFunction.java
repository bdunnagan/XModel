/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DistinctValuesFunction.java
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmodel.INode;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of IExpression which represents an X-Path 2.0 fn:distinct function. The
 * name of the function does not include the "fn" namespace, however.
 */
public class DistinctValuesFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "distinct-values";
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
  public List<INode> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    assertType( context, ResultType.NODES);
    
    List<INode> nodes = getArgument( 0).evaluateNodes( context);
    List<INode> result = new ArrayList<INode>( nodes.size());
    Map<Object, INode> map = new HashMap<Object, INode>( nodes.size());
    for( INode node: nodes)
    {
      Object value = node.getValue();
      INode match = map.get( value);
      if ( match == null)
      {
        map.put( value, node);
        result.add( node);
      }
    }
    return result;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<INode> nodes)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<INode> nodes)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyChange( this, context);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    IExpression parent = getParent();
    if ( parent == null) return false;
    return parent.requiresValueNotification( this);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(java.util.Collection, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, INode object, Object newValue, Object oldValue)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyValue( this, contexts, object, newValue, oldValue);
  }
}
