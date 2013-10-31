/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DeepEqualFunction.java
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

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of the XPath 2.0 <code>fn:deep-equal</code> function (without the fn: prefix).
 * TODO: verify semantics against XPath 2.0 function.
 */
public class DeepEqualFunction extends Function
{
  public DeepEqualFunction()
  {
    differ = new XmlDiffer( new DefaultXmlMatcher( true));
  }
  
  public final static String name = "deep-equal";
  
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
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    assertType( context, ResultType.NODES);
    
    List<IModelObject> nodes1 = getArgument( 0).evaluateNodes( context);
    List<IModelObject> nodes2 = getArgument( 1).evaluateNodes( context);
    
    // rule: non-equal list
    if ( nodes1.size() != nodes2.size()) return false;
    
    // rule: empty lists
    if ( nodes1.size() == 0) return true;
    
    // unsure if these semantics exactly match
    for( IModelObject node1: nodes1)
      for( IModelObject node2: nodes2)
        if ( differ.diff( node1, node2, null)) return true;
    
    return false;
  }
  
  
  private XmlDiffer differ;
}
