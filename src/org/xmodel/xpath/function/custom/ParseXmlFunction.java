/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ParseXmlFunction.java
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

import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;


/**
 * A custom xpath function which takes a single string argument, parses it as xml and returns
 * the root of the xml fragment.  If an error occurs during parsing then an empty set is returned.
 */
public class ParseXmlFunction extends Function
{
  public final static String name = "parse-xml";
  
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
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    assertType( context, ResultType.STRING);

    try
    {
      String string = getArgument( 0).evaluateString( context);
      IModelObject root = xmlIO.read( string);
      return Collections.singletonList( root);
    }
    catch( XmlException e)
    {
      throw new ExpressionException( this, "XML parsing error.", e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    try
    {
      IModelObject oldRoot = xmlIO.read( oldValue);
      IModelObject newRoot = xmlIO.read( newValue);
      if ( differ.diff( oldRoot, newRoot, null))
      {
        getParent().notifyRemove( this, context, Collections.singletonList( oldRoot));
        getParent().notifyAdd( this, context, Collections.singletonList( newRoot));
      }
    }
    catch( XmlException e)
    {
      getParent().handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    try
    {
      context.getModel().revert();
      String oldValue = expression.evaluateString( context);
      context.getModel().restore();
      String newValue = expression.evaluateString( context);
      notifyChange( expression, context, newValue, oldValue);
    }
    catch( ExpressionException e)
    {
      getParent().handleException( this, context, e);
    }
  }
  
  XmlIO xmlIO = new XmlIO();
  XmlDiffer differ = new XmlDiffer();
}
