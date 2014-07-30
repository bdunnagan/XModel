/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PrintfFunction.java
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

import java.util.Formatter;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.AttributeNode;
import org.xmodel.xpath.TextNode;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

/**
 * A custom XPath function which provides formatted text output similar to String.format(). The
 * function performs Java-like escape expansions on the format string returned by the
 * first argument. Currently, unicode and octal expansions are not supported and will
 * result in an exception if present.
 * FIXME: floating point numbers are not converted for %d, etc...
 */
public class FormatFunction extends Function
{
  public final static String name = "format";
  
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
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 1, -1);
    assertType( context, 0, ResultType.STRING);

    // get format string
    String format = getArgument( 0).evaluateString( context);
    
    // perform escape sequence substitutions
    format = processEscapeCharacters( format);
    
    // get parameters
    Object[] params = new Object[ getArguments().size() - 1];
    for( int i=0; i<params.length; i++)
    {
      IExpression arg = getArgument( i+1);
      switch( arg.getType( context))
      {
        case STRING:
          params[ i] = arg.evaluateString( context);
          break;
        
        case NUMBER:
          params[ i] = arg.evaluateNumber( context);
          break;
          
        case BOOLEAN:
          params[ i] = arg.evaluateBoolean( context);
          break;
          
        case NODES:
        {
          StringBuilder sb = new StringBuilder();
          XmlIO xmlIO = new XmlIO();
          for( IModelObject node: arg.evaluateNodes( context))
          {
            if ( node instanceof AttributeNode || node instanceof TextNode)
              sb.append( node.getValue());
            else
              sb.append( xmlIO.write( node));
          }
          params[ i] = sb.toString();
        }
        break;
        
        default: break;
      }
    }
    
    StringBuilder builder = new StringBuilder();
    Formatter formatter = new Formatter( builder);
    formatter.format( format, params);
    formatter.close();
    
    return builder.toString();
  }
  
  /**
   * Process the escaped characters in the specified string.
   * @param string The string.
   * @return Returns the escape processed string.
   */
  private String processEscapeCharacters( String string)
  {
    boolean escape = false;
    boolean unicode = false;
    boolean octal = false;
    StringBuilder builder = new StringBuilder();
    for( int i=0; i<string.length(); i++)
    {
      char c = string.charAt( i);
      if ( escape)
      {
        if ( c == '\\') { escape = false; builder.append( '\\');}
        else if ( c == 'n') { escape = false; builder.append( '\n');}
        else if ( c == 'f') { escape = false; builder.append( '\f');}
        else if ( c == 'r') { escape = false; builder.append( '\r');}
        else if ( c == 't') { escape = false; builder.append( '\t');}
        else if ( c == 'b') { escape = false; builder.append( '\b');}
        else if ( c == 'u') { escape = false; unicode = true; }
        else if ( Character.isDigit( c)) { escape = false; octal = true;}
        else 
        {
          throw new ExpressionException( this, "Unrecognized escape character.");
        }
      }
      else if ( unicode)
      {
        throw new ExpressionException( this, "Unicode character expansion not supported.");
      }
      else if ( octal)
      {
        throw new ExpressionException( this, "Octal character expansion not supported.");
      }
      else
      {
        if ( c == '\\') escape = true; else builder.append( c);
      }
    }
    
    return builder.toString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, boolean)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    getParent().notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IContext, double, double)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
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
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    getParent().notifyChange( this, contexts[ 0]);
  }
}
