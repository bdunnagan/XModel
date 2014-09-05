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
import org.xmodel.xpath.function.StringFunction;

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
  public static boolean get( IModelObject object, boolean defaultValue)
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
  public static short get( IModelObject object, short defaultValue)
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
  public static int get( IModelObject object, int defaultValue)
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
  public static long get( IModelObject object, long defaultValue)
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
  public static float get( IModelObject object, float defaultValue)
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
  public static double get( IModelObject object, double defaultValue)
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
  public static byte get( IModelObject object, byte defaultValue)
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
  public static byte[] get( IModelObject object, byte[] defaultValue)
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
  public static char get( IModelObject object, char defaultValue)
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
  public static char[] get( IModelObject object, char[] defaultValue)
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
  public static String get( IModelObject object, String defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getValue();
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number)
      return StringFunction.stringValue( (Number)attrValue);
    return attrValue.toString();
  }

  /**
   * Convert the attribute to the specified return value.  If the attribute is
   * not defined or cannot be converted then return the specified default value. 
   * @param object The object from which to retrieve the attribute.
   * @param defaultValue The default value returned if the attribute doesn't exist.
   * @return Returns the converted value of the attribute or the default value.
   */
  public static IExpression get( IModelObject object, IExpression defaultValue)
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
  public static <T extends Object> T get( IModelObject object, T defaultValue)
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
  public static boolean get( IModelObject object, String attrName, boolean defaultValue)
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
  public static short get( IModelObject object, String attrName, short defaultValue)
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
  public static int get( IModelObject object, String attrName, int defaultValue)
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
  public static long get( IModelObject object, String attrName, long defaultValue)
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
  public static float get( IModelObject object, String attrName, float defaultValue)
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
  public static double get( IModelObject object, String attrName, double defaultValue)
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
  public static byte get( IModelObject object, String attrName, byte defaultValue)
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
  public static byte[] get( IModelObject object, String attrName, byte[] defaultValue)
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
  public static char get( IModelObject object, String attrName, char defaultValue)
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
  public static char[] get( IModelObject object, String attrName, char[] defaultValue)
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
  public static String get( IModelObject object, String attrName, String defaultValue)
  {
    if ( object == null) return defaultValue;
    Object attrValue = object.getAttribute( attrName);
    if ( attrValue == null) return defaultValue;
    if ( attrValue instanceof Number)
      return StringFunction.stringValue( (Number)attrValue);
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
  public static <T extends Object> T get( IModelObject object, String attrName, T defaultValue)
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
  public static IExpression get( IModelObject object, String attrName, IExpression defaultValue)
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
  public static boolean set( IModelObject object, boolean value)
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
  public static short set( IModelObject object, short value)
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
  public static int set( IModelObject object, int value)
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
  public static long set( IModelObject object, long value)
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
  public static float set( IModelObject object, float value)
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
  public static double set( IModelObject object, double value)
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
  public static byte set( IModelObject object, byte value)
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
  public static byte[] set( IModelObject object, byte[] value)
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
  public static char set( IModelObject object, char value)
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
  public static char[] set( IModelObject object, char[] value)
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
  public static String set( IModelObject object, String value)
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
  public static <T extends Object> T set( IModelObject object, T value)
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
  public static boolean set( IModelObject object, String attrName, boolean value)
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
  public static short set( IModelObject object, String attrName, short value)
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
  public static int set( IModelObject object, String attrName, int value)
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
  public static long set( IModelObject object, String attrName, long value)
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
  public static float set( IModelObject object, String attrName, float value)
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
  public static double set( IModelObject object, String attrName, double value)
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
  public static byte set( IModelObject object, String attrName, byte value)
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
  public static byte[] set( IModelObject object, String attrName, byte[] value)
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
  public static char set( IModelObject object, String attrName, char value)
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
  public static char[] set( IModelObject object, String attrName, char[] value)
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
  public static String set( IModelObject object, String attrName, String value)
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
  public static <T extends Object> T set( IModelObject object, String attrName, T value)
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
  public static boolean childGet( IModelObject parent, String childType, boolean defaultValue)
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
  public static short childGet( IModelObject parent, String childType, short defaultValue)
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
  public static int childGet( IModelObject parent, String childType, int defaultValue)
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
  public static long childGet( IModelObject parent, String childType, long defaultValue)
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
  public static float childGet( IModelObject parent, String childType, float defaultValue)
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
  public static double childGet( IModelObject parent, String childType, double defaultValue)
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
  public static byte childGet( IModelObject parent, String childType, byte defaultValue)
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
  public static byte[] childGet( IModelObject parent, String childType, byte[] defaultValue)
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
  public static char childGet( IModelObject parent, String childType, char defaultValue)
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
  public static char[] childGet( IModelObject parent, String childType, char[] defaultValue)
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
  public static String childGet( IModelObject parent, String childType, String defaultValue)
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
  public static IExpression childGet( IModelObject parent, String childType, IExpression defaultValue)
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
  public static <T extends Object> T childGet( IModelObject parent, String childType, T defaultValue)
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
  public static boolean childSet( IModelObject parent, String childType, boolean value)
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
  public static short childSet( IModelObject parent, String childType, short value)
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
  public static int childSet( IModelObject parent, String childType, int value)
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
  public static long childSet( IModelObject parent, String childType, long value)
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
  public static float childSet( IModelObject parent, String childType, float value)
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
  public static double childSet( IModelObject parent, String childType, double value)
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
  public static byte childSet( IModelObject parent, String childType, byte value)
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
  public static byte[] childSet( IModelObject parent, String childType, byte[] value)
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
  public static char childSet( IModelObject parent, String childType, char value)
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
  public static char[] childSet( IModelObject parent, String childType, char[] value)
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
  public static String childSet( IModelObject parent, String childType, String value)
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
  public static IExpression childSet( IModelObject parent, String childType, IExpression value)
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
  public static <T extends Object> T childSet( IModelObject parent, String childType, T value)
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
  public static boolean pathGet( IModelObject parent, String path, boolean defaultValue)
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
  public static short pathGet( IModelObject parent, String path, short defaultValue)
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
  public static int pathGet( IModelObject parent, String path, int defaultValue)
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
  public static long pathGet( IModelObject parent, String path, long defaultValue)
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
  public static float pathGet( IModelObject parent, String path, float defaultValue)
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
  public static double pathGet( IModelObject parent, String path, double defaultValue)
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
  public static byte pathGet( IModelObject parent, String path, byte defaultValue)
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
  public static byte[] pathGet( IModelObject parent, String path, byte[] defaultValue)
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
  public static char pathGet( IModelObject parent, String path, char defaultValue)
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
  public static char[] pathGet( IModelObject parent, String path, char[] defaultValue)
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
  public static String pathGet( IModelObject parent, String path, String defaultValue)
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
  public static IExpression pathGet( IModelObject parent, String path, IExpression defaultValue)
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
  public static <T extends Object> T pathGet( IModelObject parent, String path, T defaultValue)
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
  public static boolean pathSet( IModelObject parent, String path, boolean value)
  {
    return (Boolean)pathSetImpl( parent, path, value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static short pathSet( IModelObject parent, String path, short value)
  {
    Number old = (Number)pathSetImpl( parent, path, value);
    return (old != null)? old.shortValue(): 0;
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static int pathSet( IModelObject parent, String path, int value)
  {
    Number old = (Number)pathSetImpl( parent, path, value);
    return (old != null)? old.intValue(): 0;
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static long pathSet( IModelObject parent, String path, long value)
  {
    Number old = (Number)pathSetImpl( parent, path, value);
    return (old != null)? old.longValue(): 0;
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static float pathSet( IModelObject parent, String path, float value)
  {
    Number old = (Number)pathSetImpl( parent, path, value);
    return (old != null)? old.floatValue(): 0;
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static double pathSet( IModelObject parent, String path, double value)
  {
    Number old = (Number)pathSetImpl( parent, path, value);
    return (old != null)? old.doubleValue(): 0;
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static byte pathSet( IModelObject parent, String path, byte value)
  {
    return (Byte)pathSetImpl( parent, path, value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static byte[] pathSet( IModelObject parent, String path, byte[] value)
  {
    return (byte[])pathSetImpl( parent, path, value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static char pathSet( IModelObject parent, String path, char value)
  {
    return (Character)pathSetImpl( parent, path, value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static char[] pathSet( IModelObject parent, String path, char[] value)
  {
    return (char[])pathSetImpl( parent, path, value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static String pathSet( IModelObject parent, String path, String value)
  {
    Object old = pathSetImpl( parent, path, value);
    return (old != null)? old.toString(): null;
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static IExpression pathSet( IModelObject parent, String path, IExpression value)
  {
    return (IExpression)pathSetImpl( parent, path, value);
  }

  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent.
   * @param path The type of the child.
   * @param value The value.
   * @return Returns the previous value.
   */
  public static <T extends Object> T pathSet( IModelObject parent, String path, T value)
  {
    return (T)pathSetImpl( parent, path, value);
  }
  
  /**
   * Set the value of the specified child of the specified parent. The previous value is returned.
   * If the child does not exist, then it is created.
   * @param parent The parent element.
   * @param path A simple, relative path (only child axes, no predicates).
   * @param value The value to set on the path once it's created.
   * @return Returns null or the previous value.
   */
  private static Object pathSetImpl( IModelObject parent, String path, Object value)
  {
    IPath xpath = XPath.createPath( path);
    IModelObject node = xpath.queryFirst( parent);
    Object old = (node != null)? node.getValue(): null;
    xpath.createSubtree( parent, value);
    return old;
  }
  
  /**
   * Evaluate the specified simple, relative xpath, creating elements as necessary to complete it.
   * @param parent The parent element.
   * @param path A simple, relative path (only child axes, no predicates).
   * @return Returns the first node on the path.
   */
  private static IModelObject evaluate( IModelObject parent, String path)
  {
    IPath xpath = XPath.createPath( path);
    xpath.createSubtree( parent, null);
    return xpath.queryFirst( parent);
  }  
}
