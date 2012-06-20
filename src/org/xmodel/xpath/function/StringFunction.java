/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * StringFunction.java
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
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * An implementation of the X-Path string() function.
 */
public class StringFunction extends Function
{
  public StringFunction()
  {
  }
  
  /**
   * Create a StringFunction with the given argument.
   * @param argument The argument expression.
   */
  public StringFunction( IExpression argument)
  {
    addArgument( argument);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "string";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.STRING;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(
   * org.xmodel.xpath.expression.IContext)
   */
  public String evaluateString( IContext context) throws ExpressionException
  {
    assertArgs( 0, 1);

    IExpression arg0 = getArgument( 0);
    if ( arg0 != null)
    {
      switch( arg0.getType( context))
      {
        case NODES:   return stringValue( arg0.evaluateNodes( context));
        case BOOLEAN: return stringValue( arg0.evaluateBoolean( context));
        case NUMBER:  return stringValue( arg0.evaluateNumber( context));
        case STRING:  return arg0.evaluateString( context);
      }
    }
    else
    {
      return stringValue( context.getObject());
    }
    
    return "";
  }

  /**
   * <b>NOTE:</b>This method has been redesigned to optimize performance. Now, it only returns
   * the string value of the object stored in the empty attribute.
   * <p>
   * Return the string-value of the given node.  The string-value is defined in X-Path 1.0
   * to be the concatenation of string-value of all text node descendants of the node. 
   * Since there are no text nodes in X-Model, the first attribute of all descendants are 
   * concatenated.  This works for both AttributeNode and TextNode.
   * @param object The object to be converted.
   * @return Returns the string-value of the node.
   */
  public static String stringValue( IModelObject object)
  {
    Object value = object.getValue();
    if ( value instanceof Number) return stringValue( ((Number)value).doubleValue());
    if ( value instanceof Boolean) return stringValue( (Boolean)value);
    if ( value != null) return value.toString();
    return "";
//    This is too slow.    
//    StringBuffer result = new StringBuffer();
//    BreadthFirstIterator treeIter = new BreadthFirstIterator( object);
//    while( treeIter.hasNext())
//    {
//      object = (IModelObject)treeIter.next();
//      Object value = object.getValue();
//      if ( value != null) result.append( value.toString());
//    }
//    return result.toString();
  }
  
  /**
   * Returns the string-value of the first node in the node-set.
   * @param nodes The node-set.
   * @return Returns the string-value of the first node in the node-set.
   */
  public static String stringValue( List<IModelObject> nodes)
  {
    if ( nodes.size() == 0) return "";
    return stringValue( nodes.get( 0));
  }

  /**
   * Returns the string-value of the specified number.
   * @param value The number to be converted.
   * @return Returns the string-value of the specified number.
   */
  public static String stringValue( double value)
  {
    String result = Double.toString( value);
    if ( result.endsWith( ".0")) result = result.substring( 0, result.length()-2);
    return result;
  }
  
  /**
   * Returns the string-value of the specified boolean.
   * @param value The boolean to be converted.
   * @return Returns the string-value of the specified boolean.
   */
  public static String stringValue( boolean value)
  {
    return Boolean.toString( value);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( getParent() != null) notifyChange( this, context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyChange( this, context, stringValue( newValue), stringValue( !newValue));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyChange( this, context, stringValue( newValue), stringValue( oldValue));
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    IExpression parent = getParent();
    if ( parent != null) parent.notifyChange( this, context, newValue, oldValue);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(java.util.Collection, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    getParent().notifyChange( this, contexts[ 0]);
  }
}