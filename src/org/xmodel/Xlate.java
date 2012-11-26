/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Xlate.java
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
package org.xmodel;

import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;

/**
 * A utility class for transforming the strings returned by an IModelObject.  Attribute values
 * are stored internally as Java Object instances.  Methods are provided to transform attribute
 * values to various Java primitives including String.  Each <i>get</i> method takes an 
 * argument which is returned when the transform is not valid.  The transform will not be valid
 * if the attribute value is null, if the input object is null or if the transform cannot be
 * performed for some other reason such as NumberFormatException.
 * <p>
 * In addition to transforming attribute values, this class also provides methods for transforming
 * between namespaces.  
 */
@SuppressWarnings("unchecked")
public class Xlate
{
  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static boolean get( INode object, boolean defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Boolean) return (Boolean)attrValue;
    return Boolean.toString( true).equals( attrValue.toString());
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static short get( INode object, short defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).shortValue();
    try
    {
      return Short.parseShort( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static int get( INode object, int defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).intValue();
    try
    {
      return Integer.parseInt( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static long get( INode object, long defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).longValue();
    try
    {
      return Long.parseLong( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static float get( INode object, float defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).floatValue();
    try
    {
      return Float.parseFloat( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static double get( INode object, double defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).doubleValue();
    try
    {
      return Double.parseDouble( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static byte get( INode object, byte defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).byteValue();
    try
    {
      return Byte.parseByte( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static byte[] get( INode object, byte[] defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    char[] digits = attrValue.toString().toCharArray();
    byte[] result = new byte[digits.length / 2];
    for ( int i = 0; i < digits.length; i += 2)
    {
      int nib1 = Character.digit( digits[ i + 1], 16);
      int nib2 = Character.digit( digits[ i], 16);
      result[ i] = (byte)(nib2 << 4 & nib1);
    }
    return result;
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static char get( INode object, char defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Character) return (Character)attrValue;
    return attrValue.toString().charAt( 0);
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static char[] get( INode object, char[] defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    return attrValue.toString().toCharArray();
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static String get( INode object, String defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    return attrValue.toString();
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static IExpression get( INode object, IExpression defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    IExpression expression = XPath.createExpression( attrValue.toString());
    return (expression != null)? expression: defaultValue;
  }

  /**
   * Convert the value to the specified return value. If the value is not defined or cannot be
   * converted then return the specified default value.
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value or the default value.
   */
  public static <T extends Object> T get( INode object, T defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    return (T)attrValue;
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static boolean get( INode object, String attrName, boolean defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Boolean) return (Boolean)attrValue;
    return Boolean.toString( true).equals( attrValue.toString());
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static short get( INode object, String attrName, short defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).shortValue();
    try
    {
      return Short.parseShort( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static int get( INode object, String attrName, int defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).intValue();
    try
    {
      return Integer.parseInt( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static long get( INode object, String attrName, long defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).longValue();
    try
    {
      return Long.parseLong( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static float get( INode object, String attrName, float defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).floatValue();
    try
    {
      return Float.parseFloat( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static double get( INode object, String attrName, double defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).doubleValue();
    try
    {
      return Double.parseDouble( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static byte get( INode object, String attrName, byte defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number) return ((Number)attrValue).byteValue();
    try
    {
      return Byte.parseByte( attrValue.toString());
    }
    catch ( NumberFormatException e)
    {
      return defaultValue;
    }
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static byte[] get( INode object, String attrName, byte[] defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    char[] digits = attrValue.toString().toCharArray();
    byte[] result = new byte[digits.length / 2];
    for ( int i = 0; i < digits.length; i += 2)
    {
      int nib1 = Character.digit( digits[ i + 1], 16);
      int nib2 = Character.digit( digits[ i], 16);
      result[ i] = (byte)(nib2 << 4 & nib1);
    }
    return result;
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static char get( INode object, String attrName, char defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Character) return (Character)attrValue;
    return attrValue.toString().charAt( 0);
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static char[] get( INode object, String attrName, char[] defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    return attrValue.toString().toCharArray();
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static String get( INode object, String attrName, String defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    return attrValue.toString();
  }
  
  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static <T extends Object> T get( INode object, String attrName, T defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    return (T)attrValue;
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param attrName The name of the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static IExpression get( INode object, String attrName, IExpression defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    IExpression expression = XPath.createExpression( attrValue.toString());
    return (expression != null)? expression: defaultValue;
  }

  /**
   * Convert the specified value to a format that can be saved as the value of an attribute on an IModelObject.
   * The method attempts to return the previous value.  If the previous value was not of the same type as the
   * new value, then the result is undefined. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   * @return Returns the old value.
   */
  public static boolean set( INode object, boolean value)
  {
    boolean old = get( object, false);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static short set( INode object, short value)
  {
    short old = get( object, (short)0);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static int set( INode object, int value)
  {
    int old = get( object, 0);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static long set( INode object, long value)
  {
    long old = get( object, (long)0);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static float set( INode object, float value)
  {
    float old = get( object, (float)0);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static double set( INode object, double value)
  {
    double old = get( object, (double)0);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static byte set( INode object, byte value)
  {
    byte old = get( object, value);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static byte[] set( INode object, byte[] value)
  {
    byte[] old = get( object, (byte[])null);

    StringBuilder buffer = new StringBuilder();
    for ( int i = 0; i < value.length; i++)
    {
      buffer.append( Character.forDigit( value[ i] >> 4 & 0x0F, 16));
      buffer.append( Character.forDigit( value[ i] & 0x0F, 16));
    }

    object.setValue( buffer.toString());
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static char set( INode object, char value)
  {
    char old = get( object, '\0');
    object.setValue( Character.toString( value));
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static char[] set( INode object, char[] value)
  {
    char[] old = get( object, (char[])null);
    String attrValue = new String( value);
    object.setValue( attrValue);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static String set( INode object, String value)
  {
    String old = get( object, (String)null);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value of an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param value The value of the attribute.
   */
  public static <T extends Object> T set( INode object, T value)
  {
    T old = get( object, (T)null);
    object.setValue( value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static boolean set( INode object, String attrName, boolean value)
  {
    boolean old = get( object, attrName, false);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static short set( INode object, String attrName, short value)
  {
    short old = get( object, attrName, (short)0);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static int set( INode object, String attrName, int value)
  {
    int old = get( object, attrName, (int)0);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static long set( INode object, String attrName, long value)
  {
    long old = get( object, attrName, (long)0);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static float set( INode object, String attrName, float value)
  {
    float old = get( object, attrName, (float)0);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static double set( INode object, String attrName, double value)
  {
    double old = get( object, attrName, (double)0);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static byte set( INode object, String attrName, byte value)
  {
    byte old = get( object, attrName, (byte)0);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static byte[] set( INode object, String attrName, byte[] value)
  {
    byte[] old = get( object, attrName, (byte[])null);

    StringBuffer buffer = new StringBuffer();
    for ( int i = 0; i < value.length; i++)
    {
      buffer.append( Character.forDigit( value[ i] >> 4 & 0x0F, 16));
      buffer.append( Character.forDigit( value[ i] & 0x0F, 16));
    }
    object.setAttribute( attrName, buffer.toString());

    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static char set( INode object, String attrName, char value)
  {
    char old = get( object, attrName, (char)0);
    object.setAttribute( attrName, Character.toString( value));
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static char[] set( INode object, String attrName, char[] value)
  {
    char[] old = get( object, attrName, (char[])null);
    String attrValue = new String( value);
    object.setAttribute( attrName, attrValue);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static String set( INode object, String attrName, String value)
  {
    String old = get( object, attrName, (String)null);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the specified value to a format that can be saved as the value
   * of an attribute on an IModelObject. 
   * @param object The object on which to set the attribute.
   * @param attrName The name of the attribute.
   * @param value The value of the attribute.
   */
  public static <T extends Object> T set( INode object, String attrName, T value)
  {
    T old = get( object, attrName, (T)null);
    object.setAttribute( attrName, value);
    return old;
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static boolean childGet( INode parent, String childType, boolean defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static short childGet( INode parent, String childType, short defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static int childGet( INode parent, String childType, int defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static long childGet( INode parent, String childType, long defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static float childGet( INode parent, String childType, float defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static double childGet( INode parent, String childType, double defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static byte childGet( INode parent, String childType, byte defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static byte[] childGet( INode parent, String childType, byte[] defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static char childGet( INode parent, String childType, char defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static char[] childGet( INode parent, String childType, char[] defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static String childGet( INode parent, String childType, String defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static IExpression childGet( INode parent, String childType, IExpression defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Convert the value of the specified child of the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the child does not exist.
   * @param object The parent object.
   * @param childType The type of child.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static <T extends Object> T childGet( INode parent, String childType, T defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( parent.getFirstChild( childType), defaultValue);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static boolean childSet( INode parent, String childType, boolean value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static short childSet( INode parent, String childType, short value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static int childSet( INode parent, String childType, int value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static long childSet( INode parent, String childType, long value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static float childSet( INode parent, String childType, float value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static double childSet( INode parent, String childType, double value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static byte childSet( INode parent, String childType, byte value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static byte[] childSet( INode parent, String childType, byte[] value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static char childSet( INode parent, String childType, char value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static char[] childSet( INode parent, String childType, char[] value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static String childSet( INode parent, String childType, String value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static IExpression childSet( INode parent, String childType, IExpression value)
  {
    return set( parent.getCreateChild( childType), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param childType The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static <T extends Object> T childSet( INode parent, String childType, T value)
  {
    return set( parent.getCreateChild( childType), value);
  }  
  
  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static boolean pathGet( INode parent, String path, boolean defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static short pathGet( INode parent, String path, short defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static int pathGet( INode parent, String path, int defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static long pathGet( INode parent, String path, long defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static float pathGet( INode parent, String path, float defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static double pathGet( INode parent, String path, double defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static byte pathGet( INode parent, String path, byte defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static byte[] pathGet( INode parent, String path, byte[] defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static char pathGet( INode parent, String path, char defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static char[] pathGet( INode parent, String path, char[] defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static String pathGet( INode parent, String path, String defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static IExpression pathGet( INode parent, String path, IExpression defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Convert the value of the element on the specified path relative to the specified parent to the specified return value.
   * The default value is returned if the parent is null or if the element does not exist.
   * @param object The parent object.
   * @param path The relative path of the element.
   * @param defaultValue The default value if the value does not exist.
   * @return Returns the value.
   */
  public static <T extends Object> T pathGet( INode parent, String path, T defaultValue)
  {
    if ( parent == null) return defaultValue;
    return get( evaluate( parent, path), defaultValue);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static boolean pathSet( INode parent, String path, boolean value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static short pathSet( INode parent, String path, short value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static int pathSet( INode parent, String path, int value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static long pathSet( INode parent, String path, long value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static float pathSet( INode parent, String path, float value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static double pathSet( INode parent, String path, double value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static byte pathSet( INode parent, String path, byte value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static byte[] pathSet( INode parent, String path, byte[] value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static char pathSet( INode parent, String path, char value)
  {
    IPath xpath = XPath.createPath( path);
    xpath.createSubtree( parent);
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static char[] pathSet( INode parent, String path, char[] value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static String pathSet( INode parent, String path, String value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static IExpression pathSet( INode parent, String path, IExpression value)
  {
    return set( evaluate( parent, path), value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static <T extends Object> T pathSet( INode parent, String path, T value)
  {
    return set( evaluate( parent, path), value);
  }
  
  /**
   * Evaluate the specified simple, relative xpath, creating elements as necessary to complete it.
   * @param parent The parent element.
   * @param path A simple, relative path (only child axes, no predicates).
   * @return Returns the first node on the path.
   */
  private static INode evaluate( INode parent, String path)
  {
    IPath xpath = XPath.createPath( path);
    xpath.createSubtree( parent);
    return xpath.queryFirst( parent);
  }
}
