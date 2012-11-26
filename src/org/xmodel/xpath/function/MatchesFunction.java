/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * MatchesFunction.java
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the XPath 2.0 fn:matches function.  Regular expressions are implemented
 * using the Java Pattern class and may not conform to the XPath 2.0 specification.  The flags
 * parameter does roughly correspond to the XPath 2.0 specification.  Another deviation from the
 * XPath 2.0 specification is that if the first argument is a node-set then the function is true
 * if the value of any node matches.
 */
public class MatchesFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "matches";
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
    assertArgs( 2, 3);
    assertType( context, 1, ResultType.STRING);
    assertType( context, 2, ResultType.STRING);
    
    Pattern pattern = getPattern( context);
    if ( pattern == null) return false;
    
    IExpression arg0 = getArgument( 0);
    if ( arg0.getType( context) == ResultType.STRING)
    {
      String input = arg0.evaluateString( context);
      Matcher matcher = pattern.matcher( input);
      return matcher.find();
    }
    else if ( arg0.getType( context) == ResultType.NODES)
    {
      List<INode> nodes = arg0.evaluateNodes( context);
      for( INode node: nodes)
      {
        Matcher matcher = pattern.matcher( Xlate.get( node, ""));
        if ( matcher.find()) return true;
      }
    }
    
    return false;
  }

  /**
   * Returns a Pattern object for this expression.
   * @param context The context of the evaluation.
   * @return Returns the Pattern object.
   */
  private Pattern getPattern( IContext context) throws ExpressionException
  {
    try
    {
      String regex = getArgument( 1).evaluateString( context);
      return Pattern.compile( regex, getFlags( context));
    }
    catch( PatternSyntaxException e)
    {
      throw new ExpressionException( this, "Invalid regular expression.", e);
    }
  }
  
  /**
   * Returns the flags of the pattern.
   * @param context The context of the evaluation.
   */
  private int getFlags( IContext context) throws ExpressionException
  {
    IExpression arg2 = getArgument( 2);
    if ( arg2 == null) return 0;
    
    int result = 0;
    String flags = arg2.evaluateString( context);
    if ( flags.contains( "s")) result |= Pattern.DOTALL;
    if ( flags.contains( "m")) result |= Pattern.MULTILINE;
    if ( flags.contains( "i")) result |= Pattern.CASE_INSENSITIVE;
    if ( flags.contains( "x")) result |= Pattern.COMMENTS;
    
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<INode> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<INode> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyValue(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext[], org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  @Override
  public void notifyValue( IExpression expression, IContext[] contexts, INode object, Object newValue, Object oldValue)
  {
    for( IContext context: contexts) getParent().notifyChange( this, context);
  }
}
