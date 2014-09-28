package org.xmodel.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;

/**
 * A JSON parser that generates a corresponding XPath data-type.
 */
public class JsonParser
{
  public final static String invalidEscapeCharacter = "Invalid escape character.";
  public final static String unterminatedString = "Unterminated string.";
  public final static String illegalNumberFormat = "Illegal number format.";
  public final static String expectedBooleanKeyword = "Expected boolean keyword.";
  public final static String expectedNullKeyword = "Expected null keyword.";
  public final static String expectedValue = "Expected value.";
  public final static String expectedColon = "Expected colon character.";
  public final static String expectedComma = "Expected comma character.";
  public final static String unexpectedCharacter = "Unexpected character.";
  public final static String illegalObjectKey = "Illegal object key.";
  
  /**
   * Parse the specified JSON text.
   * @param json The JSON text.
   * @return Returns one of: String, Number, Boolean, or IModelObject.
   */
  public Object parse( String json) throws ParseException
  {
    index = 0;
    return parseValue( json, null);
  }
  
  /**
   * Parse the next value in the specified JSON text.
   * @param json The JSON text.
   * @param key Null or the name of the key associated with this value.
   * @return Returns the next value.
   */
  private Object parseValue( String json, String key) throws ParseException
  {
    while( index < json.length())
    {
      char c = json.charAt( index++);

      if ( c == '\"' || c == '\'')
      {
        --index;
        return parseString( json);
      }
      else if ( c == '{')
      {
        return parseObject( json, key);
      }
      else if ( c == '}')
      {
        return objectEndToken;
      }
      else if ( c == '[')
      {
        return parseArray( json, key);
      }
      else if ( c == ']')
      {
        return arrayEndToken;
      }
      else if ( c == ',')
      {
        return commaToken;
      }
      else if ( c == 't')
      {
        if ( json.indexOf( "rue", index) != index)
          throw new ParseException( expectedBooleanKeyword, index);
        
        index += 3;
        return true;
      }
      else if ( c == 'f')
      {
        if ( json.indexOf( "alse", index) != index)
          throw new ParseException( expectedBooleanKeyword, index);
        
        index += 4;
        return false;
      }
      else if ( c == 'n')
      {
        if ( json.indexOf( "ull", index) != index)
          throw new ParseException( expectedNullKeyword, index);
        
        index += 3;
        return null;
      }
      else if ( c == '+' || c == '-' || (c >= '0' && c <= '9'))
      {
        --index;
        return parseNumber( json);
      }
      else if ( c == '\t' || c == ' ' || c == '\n' || c == '\r')
      {
        continue;
      }
      else
      {
        throw new ParseException( unexpectedCharacter, index);
      }
    }
   
    throw new ParseException( expectedValue, index);
  }
  
  /**
   * Parse a string from the specified JSON text.
   * @param json The JSON.
   * @return Returns the string that was parsed.
   */
  private String parseString( String json) throws ParseException
  {
    char quote = json.charAt( index++);

    StringBuilder sb = new StringBuilder();
    while( index < json.length())
    {
      char c = json.charAt( index++);
      if ( c == '\\')
      {
        c = json.charAt( index++);
        switch( c)
        {
          case '\"': 
          case '\'':
          case '\\':
          case '/':
            break;
            
          case 'b': c = '\b'; break;
          case 'f': c = '\f'; break;
          case 'n': c = '\n'; break;
          case 'r': c = '\r'; break;
          case 't': c = '\t'; break;
          
          case 'u':
          {
            int code = Integer.parseInt( json.substring( index, index+4), 16);
            c = (char)code;
            index += 4;
            break;
          }
          
          default:  
            throw new ParseException( invalidEscapeCharacter, index);
        }
      }
      else if ( c == quote)
      {
        return sb.toString();
      }
      
      sb.append( c);
    }
    
    throw new ParseException( unterminatedString, index);
  }
  
  /**
   * Parse a number from the specified JSON text.
   * @param json The JSON.
   * @return Returns the number that was parsed.
   */
  private Number parseNumber( String json) throws ParseException
  {
    Matcher matcher = numberRegex.matcher( json);
    if ( !matcher.find( index)) throw new ParseException( illegalNumberFormat, index);
    index += matcher.end() - matcher.start();
    return Double.parseDouble( matcher.group());
  }
  
  /**
   * Parse an object from the specified JSON text.
   * @param json The JSON.
   * @param name Null or the name of the key associated with this object.
   * @return Returns the object that was parsed.
   */
  private IModelObject parseObject( String json, String name) throws ParseException
  {
    IModelObject element = new ModelObject( (name != null)? name: "object");
    int count = 0;
    while( index < json.length())
    {
      if ( count++ > 0)
      {
        Object token = parseValue( json, null);
        if ( token == objectEndToken) break;
        if ( token != commaToken) throw new ParseException( expectedComma, index);
        if ( token == arrayEndToken) throw new ParseException( unexpectedCharacter, index);
      }
      
      Object key = parseValue( json, null);
      if ( key == objectEndToken) break;
      if ( !(key instanceof String)) throw new ParseException( illegalObjectKey, index);
      if ( key == arrayEndToken) throw new ParseException( unexpectedCharacter, index);
      if ( key == commaToken) throw new ParseException( unexpectedCharacter, index);
      
      int j = json.indexOf( ':', index);
      if ( j < 0) throw new ParseException( expectedColon, index);
      index = j + 1;
      
      Object value = parseValue( json, (String)key);
      if ( value == arrayEndToken) throw new ParseException( unexpectedCharacter, index);
      if ( value == objectEndToken) throw new ParseException( unexpectedCharacter, index);
      if ( value == commaToken) throw new ParseException( unexpectedCharacter, index);
      
      if ( value != null)
      {
        if ( value instanceof IModelObject)
        {
          element.addChild( (IModelObject)value);
        }
        else
        {
          element.setAttribute( (String)key, value);
        }
      }
    }
    
    return element;
  }
  
  /**
   * Parse an array from the specified JSON text.
   * @param json The JSON.
   * @param name Null or the name of the key associated with this array.
   * @return Returns the array that was parsed.
   */
  private IModelObject parseArray( String json, String name) throws ParseException
  {
    IModelObject array = new ModelObject( (name != null)? name: "array");
    
    int count = 0;
    while( index < json.length())
    {
      if ( count++ > 0)
      {
        Object token = parseValue( json, null);
        if ( token == arrayEndToken) break;
        if ( token != commaToken) throw new ParseException( expectedComma, index);
        if ( token == objectEndToken) throw new ParseException( unexpectedCharacter, index);
      }
      
      Object value = parseValue( json, null);
      if ( value == arrayEndToken) break;
      if ( value == commaToken) throw new ParseException( unexpectedCharacter, index);
      if ( value == objectEndToken) throw new ParseException( unexpectedCharacter, index);
     
      if ( value instanceof IModelObject)
      {
        array.addChild( (IModelObject)value);
      }
      else
      {
        IModelObject item = new ModelObject( "value");
        item.setValue( value);
        array.addChild( item);
      }
    }

    return array;
  }
  
  private final static Pattern numberRegex = Pattern.compile( "[+-]?(0|([1-9]\\d*+))([.]\\d++)?([eE][+-]\\d++)?");
  
  private final static Object objectEndToken = new Object();
  private final static Object arrayEndToken = new Object();
  private final static Object commaToken = new Object();
  
  private int index;
  
  public static void main( String[] args) throws Exception
  {
    JsonParser json = new JsonParser();
    Object object = null;
    
    object = json.parse( "true"); System.out.println( object);
    object = json.parse( "false"); System.out.println( object);
    object = json.parse( "null"); System.out.println( object);
    object = json.parse( " true "); System.out.println( object);
    
    object = json.parse( "1"); System.out.println( object);
    object = json.parse( "-1"); System.out.println( object);
    object = json.parse( "1.35"); System.out.println( object);
    object = json.parse( "1.35e-10"); System.out.println( object);
    object = json.parse( "-1.35e-10"); System.out.println( object);
    object = json.parse( " -1.35e-10"); System.out.println( object);
    
    object = json.parse( " \"\\u0065\""); System.out.println( object);
    
    object = json.parse( "\"Bob\""); System.out.println( object);
    object = json.parse( " \"Bob\" "); System.out.println( object);
    object = json.parse( "\"\\\"Quoted\\\"\""); System.out.println( object);
    
    object = json.parse( "{ \"name\": \"Bob\"}"); System.out.println( object);
    object = json.parse( "{ \"name\": \"Bob\", \"state\": \"being\"}"); System.out.println( object);
    
    object = json.parse( "[ \"Bob\", \"Melissa\"]"); System.out.println( object);
    object = json.parse( "[1,2,3]"); System.out.println( object);
    
    object = json.parse( "{\"object\":{\"name\":\"Bob\",\"state\": \"being\"}}"); System.out.println( object);
  }
}
