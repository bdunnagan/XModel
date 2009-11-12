/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Function.java
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
import org.xmodel.xpath.expression.Expression;
import org.xmodel.xpath.expression.IExpression;


/**
 * Base class of all functions.  This class implements toString() to correctly print function.
 */
public abstract class Function extends Expression
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#toString()
   */
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( getName());
    builder.append( '(');
    List<IExpression> arguments = getArguments();
    boolean useComma = arguments.size() > 1;
    for ( int i=0; i<arguments.size(); i++)
    {
      if ( i > 0 && useComma) builder.append( ", ");
      builder.append( getArgument( i).toString());
    }
    builder.append( ')');
    return builder.toString();
  }
}
