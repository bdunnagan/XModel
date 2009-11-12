/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IExpression.java
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
package org.xmodel.xpath.expression.nu;

import java.util.List;
import org.xmodel.xpath.expression.IContext;

/**
 * A new expression interface which aims to support completely support XQuery 1.0 and XPath 2.0.
 * This interface differs from the previous interface in two important areas: 1) the way that
 * sub-notifications are gathered and turned into expression notifications, and 2) support for
 * arbitrary sequences. In addition, a mechanism will be provided for managing the complexity
 * of incremental notification given the multiplicity of return types. All return types are 
 * now represented as Object instances, so in general, an expression which can receive 
 * child notifications of more than one possible return type will emit a blind notification.  
 */
public interface IExpression
{
  /**
   * Evaluate this expression in the specified context with the specified expression variables.
   * The expression variables are key/value pairs.
   * @param context The context.
   * @param vars The key/value pairs.
   * @return Returns the result of the expression. 
   */
  public List<Object> evaluate( IContext context, String... vars);
  
  /**
   * Add a listener to this expression. A listener is always associated with an instance
   * of a context. The same context instance must be used when removing the listener via
   * the <code>removeListener</code> method.
   * @param context The context.
   * @param listener The listener.
   */
  public void addListener( IContext context, IExpressionListener listener);
  
  /**
   * Remove a listener from this expression. The context instance must be the same instance
   * with which the listener was added via the <code>addListener</code> method.
   * @param context The context.
   * @param listener The listener.
   */
  public void removeListener( IContext context, IExpressionListener listener);
}
