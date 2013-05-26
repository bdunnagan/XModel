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
      if ( value == arrayEndToken) throw new ParseException( unexpectedCharacter, index);
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
    
    String large = 
        "[\n" + 
        "[\"\", \"unknown country\",0.05,0.0,14.067278,0.0,0.0],\n" + 
        "[\"GD\", \"Grenada\",0.004908836,0.5,0.0,0.0,7.0E-4],\n" + 
        "[\"GE\", \"Georgia\",0.24745117,0.42857143,0.41967392,0.4235399,0.0],\n" + 
        "[\"GF\", \"French Guiana\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"GA\", \"Gabon\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"GB\", \"United Kingdom\",0.33812153,0.41687658,0.73147964,0.4417777,0.0021],\n" + 
        "[\"FJ\", \"Fiji\",0.0014025245,0.33333334,0.0,0.0,2.0E-4],\n" + 
        "[\"FM\", \"Micronesia\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"FI\", \"Finland\",0.39561448,0.54330707,0.8674627,0.4974564,0.0029],\n" + 
        "[\"FR\", \"France\",0.69653684,0.46608314,0.6858054,0.47565424,0.0525],\n" + 
        "[\"FO\", \"Faroe Islands\",0.002805049,0.5,0.0,0.0,4.0E-4],\n" + 
        "[\"GY\", \"Guyana (Republic of)\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"GW\", \"Guinea-Bissau\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"WS\", \"Samoa\",0.002805049,0.5,0.0,0.0,4.0E-4],\n" + 
        "[\"GN\", \"Guinea\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"GM\", \"Gambia\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"GL\", \"Greenland\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"GI\", \"Gibraltar\",0.0,0.25,0.0,0.0,0.0],\n" + 
        "[\"GH\", \"Ghana\",0.19238552,0.0625,0.0,0.4652187,0.0],\n" + 
        "[\"GG\", \"Guernsey\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"GU\", \"Guam\",0.0,0.4,0.0,0.0,0.0],\n" + 
        "[\"GT\", \"Guatemala\",0.26224023,0.071428575,0.28785512,0.5018615,1.0E-4],\n" + 
        "[\"GR\", \"Greece\",0.29301772,0.39130434,0.44075337,0.4964705,0.0018000001],\n" + 
        "[\"GQ\", \"Equatorial Guinea\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"GP\", \"Guadeloupe\",0.20453937,1.0,0.0,0.49460864,0.0],\n" + 
        "[\"VI\", \"U.S. Virgin Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"DZ\", \"Algeria\",0.20815691,0.33333334,0.0,0.5033564,0.0],\n" + 
        "[\"VU\", \"Vanuatu\",0.28681627,0.0,0.0,0.0,0.040900003],\n" + 
        "[\"VN\", \"Vietnam\",0.22125447,0.15789473,0.25927186,0.42785606,0.0],\n" + 
        "[\"EC\", \"Ecuador\",0.3731356,0.47368422,0.8748981,0.47423205,0.001],\n" + 
        "[\"DE\", \"Germany\",0.502642,0.5662514,0.79481995,0.44512537,0.0234],\n" + 
        "[\"UZ\", \"Uzbekistan\",0.14369869,0.16666667,0.0,0.34579045,1.0E-4],\n" + 
        "[\"UY\", \"Uruguay\",0.35495773,0.4,0.7758742,0.4800772,1.0E-4],\n" + 
        "[\"DK\", \"Denmark\",0.34744194,0.5548387,0.76242584,0.45818272,0.0013],\n" + 
        "[\"DJ\", \"Djibouti\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"VE\", \"Venezuela\",0.313544,0.51428574,0.6593863,0.44795144,1.0E-4],\n" + 
        "[\"DM\", \"Dominica\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"VC\", \"Saint Vincent and the Grenadines\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"DO\", \"Dominican Republic\",0.26721677,0.22222222,0.340782,0.48077375,0.0],\n" + 
        "[\"VA\", \"Vatican City\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"UK\", \"United Kingdom\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"EU\", \"European Union\",0.11987949,0.3432836,0.6063887,0.0,0.0],\n" + 
        "[\"UG\", \"Uganda\",0.20247072,0.0,0.0,0.48960632,0.0],\n" + 
        "[\"US\", \"United States of America\",0.48481637,0.41787207,0.58550906,0.4729614,0.0252],\n" + 
        "[\"W\", \"world\",0.23026761,0.0,0.0,0.5568236,0.0],\n" + 
        "[\"EH\", \"Western Sahara\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"EG\", \"Egypt\",0.27514422,0.33333334,0.40120086,0.5003597,0.0],\n" + 
        "[\"TZ\", \"Tanzania\",0.29036158,0.47368422,0.37772956,0.5093058,5.9999997E-4],\n" + 
        "[\"EE\", \"Estonia\",0.38445544,0.5263158,0.9174635,0.46633112,0.0011999999],\n" + 
        "[\"TT\", \"Trinidad and Tobago\",0.1988103,0.6666667,0.0,0.48075482,0.0],\n" + 
        "[\"TW\", \"Taiwan\",0.3517035,0.36363637,0.577685,0.47127417,0.0063],\n" + 
        "[\"TV\", \"Tuvalu\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"UA\", \"Ukraine\",0.25125983,0.51282054,0.28796706,0.46535984,9.0000004E-4],\n" + 
        "[\"ET\", \"Ethiopia\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"ES\", \"Spain\",0.33020535,0.3828829,0.6187674,0.50729346,5.9999997E-4],\n" + 
        "[\"ER\", \"Eritrea\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"TO\", \"Tonga\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"TN\", \"Tunisia\",0.21189047,0.33333334,0.0,0.5123848,0.0],\n" + 
        "[\"TM\", \"Turkmenistan\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"TL\", \"Timor-Leste\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"CA\", \"Canada\",0.3498386,0.46060607,0.7222475,0.47282365,0.0019],\n" + 
        "[\"TR\", \"Turkey\",0.32314113,0.24271844,0.74347395,0.42761445,1.0E-4],\n" + 
        "[\"BZ\", \"Belize\",7.0126227E-4,0.11111111,0.0,0.0,1.0E-4],\n" + 
        "[\"TG\", \"Togo\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BW\", \"Botswana\",0.13363747,0.0,0.66582084,0.0,0.0],\n" + 
        "[\"BY\", \"Belarus\",0.2825288,0.4,0.5264572,0.43987182,0.0],\n" + 
        "[\"TD\", \"Chad\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"TK\", \"Tokelau\",0.0,0.8,0.0,0.0,0.0],\n" + 
        "[\"BS\", \"Bahamas\",0.19333799,0.0,0.0,0.4675219,0.0],\n" + 
        "[\"TJ\", \"Tajikistan\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BR\", \"Brazil\",0.28626716,0.18977384,0.35003605,0.52682686,4.0E-4],\n" + 
        "[\"TH\", \"Thailand\",0.36577788,0.5,0.79717547,0.50118464,2.0E-4],\n" + 
        "[\"BT\", \"Bhutan\",0.15638149,0.33333334,0.0,0.0,0.0223],\n" + 
        "[\"BN\", \"Brunei\",0.0,0.2,0.0,0.0,0.0],\n" + 
        "[\"BO\", \"Bolivia\",0.20197134,0.1,0.0,0.4883987,0.0],\n" + 
        "[\"BJ\", \"Benin\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"TC\", \"Turks and Caicos Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BM\", \"Bermuda\",0.0,0.33333334,0.0,0.0,0.0],\n" + 
        "[\"BF\", \"Burkina Faso\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"SV\", \"El Salvador\",0.20834103,0.14285715,0.0,0.50380164,0.0],\n" + 
        "[\"BG\", \"Bulgaria\",0.26622617,0.68421054,0.51249313,0.4058274,2.0E-4],\n" + 
        "[\"SS\", \"South Sudan\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BH\", \"Bahrain\",0.19951412,0.06666667,0.0,0.48245677,0.0],\n" + 
        "[\"ST\", \"Sao Tome and Principe\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BI\", \"Burundi\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"SY\", \"Syria\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BB\", \"Barbados\",0.17416435,0.0,0.0,0.421157,0.0],\n" + 
        "[\"SZ\", \"Swaziland\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"BD\", \"Bangladesh\",0.26968387,0.28070176,0.31137034,0.5166603,1.0E-4],\n" + 
        "[\"BE\", \"Belgium\",0.41228312,0.44036698,0.7260643,0.47192934,0.0104],\n" + 
        "[\"SL\", \"Sierra Leone\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"SK\", \"Slovakia\",0.27948138,0.56,0.39502198,0.43477702,0.0038],\n" + 
        "[\"SN\", \"Senegal\",0.20015374,1.0,0.0,0.48400345,0.0],\n" + 
        "[\"SM\", \"San Marino\",0.0,0.6666667,0.0,0.0,0.0],\n" + 
        "[\"SO\", \"Somalia\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"SR\", \"Suriname\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"CZ\", \"Czech Republic\",0.48958477,0.6322314,0.6579108,0.60453963,0.0159],\n" + 
        "[\"SD\", \"Sudan\",0.18367046,0.4,0.0,0.44414428,0.0],\n" + 
        "[\"CY\", \"Cyprus\",0.30111507,0.375,0.5301004,0.4811394,1.0E-4],\n" + 
        "[\"SC\", \"Seychelles\",0.0,0.25,0.0,0.0,0.0],\n" + 
        "[\"CW\", \"curacao\",0.0014025245,0.6,0.0,0.0,2.0E-4],\n" + 
        "[\"SE\", \"Sweden\",0.38025287,0.53276354,0.8862866,0.45309058,0.0023999999],\n" + 
        "[\"CV\", \"Cape Verde\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"CU\", \"Cuba\",0.21964495,0.5,1.0,0.0,0.0027],\n" + 
        "[\"SG\", \"Singapore\",0.3954889,0.4121212,0.8232555,0.52667767,0.0023999999],\n" + 
        "[\"SJ\", \"Svalbard and Jan Mayen Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"SI\", \"Slovenia\",0.444197,0.60215056,0.762751,0.5371583,0.0101],\n" + 
        "[\"CR\", \"Costa Rica\",0.32170781,0.39473686,0.6088109,0.48147407,2.0E-4],\n" + 
        "[\"CO\", \"Colombia\",0.28479478,0.3448276,0.5119318,0.4895329,1.0E-4],\n" + 
        "[\"CM\", \"Cameroon\",0.2067027,0.0,0.0,0.49983984,0.0],\n" + 
        "[\"CN\", \"China\",0.15209185,0.14403293,0.2259758,0.16275807,0.0073],\n" + 
        "[\"CK\", \"Cook Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"SA\", \"Saudi Arabia\",0.31907728,0.3846154,0.65143937,0.46652815,0.0],\n" + 
        "[\"CL\", \"Chile\",0.33123618,0.26923078,0.6679519,0.47616062,2.0E-4],\n" + 
        "[\"SB\", \"Solomon Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"CI\", \"Cote d'Ivoire\",0.20382692,1.0,0.0,0.49288583,0.0],\n" + 
        "[\"RS\", \"Serbia\",0.28225487,0.3783784,0.44107378,0.4661069,9.0000004E-4],\n" + 
        "[\"CG\", \"Congo (Brazzaville)\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"CH\", \"Switzerland\",0.5618721,0.5753425,0.8088245,0.48803324,0.028900001],\n" + 
        "[\"RU\", \"Russia\",0.21086793,0.40532082,0.40270543,0.2948302,0.0018000001],\n" + 
        "[\"CF\", \"Central African Republic\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"RW\", \"Rwanda\",0.19426714,0.16666667,0.0,0.4663772,2.0E-4],\n" + 
        "[\"CD\", \"Congo (Democratic Republic)\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"RO\", \"Romania\",0.7788945,0.5609756,0.46692926,0.4599751,0.0713],\n" + 
        "[\"RE\", \"Reunion\",0.19228758,0.0,0.0,0.46498185,0.0],\n" + 
        "[\"AZ\", \"Azerbaijan\",0.36714035,0.45454547,0.8509622,0.4747884,0.0],\n" + 
        "[\"BA\", \"Bosnia and Herzegovina\",0.2350401,0.4347826,0.29312444,0.4417826,0.0028],\n" + 
        "[\"AT\", \"Austria\",0.35085377,0.60169494,0.7793996,0.45568383,0.0016],\n" + 
        "[\"AS\", \"American Samoa\",0.20071086,1.0,1.0,0.0,0.0],\n" + 
        "[\"AR\", \"Argentina\",0.26948506,0.1627907,0.4142148,0.4577476,0.0],\n" + 
        "[\"AX\", \"Aland Islands\",0.002805049,0.0,0.0,0.0,4.0E-4],\n" + 
        "[\"AW\", \"Aruba\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"QA\", \"Qatar\",0.3817278,0.5,0.88301224,0.49999872,0.0],\n" + 
        "[\"AU\", \"Australia\",0.33985338,0.26369864,0.6330507,0.4685174,0.0034],\n" + 
        "[\"AL\", \"Albania\",0.19649343,0.0,0.0,0.47515228,0.0],\n" + 
        "[\"AI\", \"Anguilla\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"AO\", \"Angola\",0.0,0.05882353,0.0,0.0,0.0],\n" + 
        "[\"AP\", \"Asia/Pacific Region\",0.0,0.5,0.0,0.0,0.0],\n" + 
        "[\"PY\", \"Paraguay\",0.19740589,0.07692308,0.0,0.47735873,0.0],\n" + 
        "[\"AM\", \"Armenia\",0.3379047,0.6315789,0.747966,0.47013867,1.0E-4],\n" + 
        "[\"AN\", \"Netherlands Antilles\",0.06709272,0.18181819,0.43415543,0.0,0.0],\n" + 
        "[\"PT\", \"Portugal\",0.3524859,0.4054054,0.5613894,0.49052578,0.0073],\n" + 
        "[\"AD\", \"Andorra\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"PW\", \"Palau\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"AG\", \"Antigua and Barbuda\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"AE\", \"United Arab Emirates\",0.30217722,0.16666667,0.5647635,0.47853547,0.0],\n" + 
        "[\"PR\", \"Puerto Rico\",0.27325517,0.36363637,0.38737488,0.4782244,0.0],\n" + 
        "[\"AF\", \"Afghanistan\",0.19792135,0.0,0.0,0.4786052,0.0],\n" + 
        "[\"PS\", \"Palestinian Teritorry\",0.14963226,0.23076923,0.0,0.36183453,0.0],\n" + 
        "[\"NU\", \"Niue\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"NR\", \"Nauru\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"NP\", \"Nepal\",0.25625855,0.33333334,0.2943714,0.48558873,0.0],\n" + 
        "[\"NO\", \"Norway\",0.44926858,0.5786802,0.86645204,0.49140453,0.0109],\n" + 
        "[\"NZ\", \"New Zealand\",0.36274683,0.35632184,0.75352114,0.46607673,0.0032],\n" + 
        "[\"OM\", \"Oman\",0.38560936,0.33333334,0.9116292,0.49300244,0.0],\n" + 
        "[\"PE\", \"Peru\",0.39822936,0.35,0.531434,0.4873486,0.013200001],\n" + 
        "[\"PF\", \"French Polynesia\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"PG\", \"Papua New Guinea\",0.0014025245,0.16666667,0.0,0.0,2.0E-4],\n" + 
        "[\"PA\", \"Panama\",0.21402971,0.18181819,0.092169344,0.47112754,1.0E-4],\n" + 
        "[\"PL\", \"Poland\",0.29029155,0.52797204,0.5099132,0.46080157,5.9999997E-4],\n" + 
        "[\"PH\", \"Philippines\",0.29137367,0.25,0.45773658,0.5076684,1.0E-4],\n" + 
        "[\"PK\", \"Pakistan\",0.29814178,0.3529412,0.4763982,0.49104744,2.0E-4],\n" + 
        "[\"LS\", \"Lesotho\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"LR\", \"Liberia\",0.0,1.0,0.0,0.0,0.0],\n" + 
        "[\"LV\", \"Latvia\",0.2890322,0.45714286,0.554571,0.42374912,9.0000004E-4],\n" + 
        "[\"LU\", \"Luxembourg\",0.6286035,0.525,0.53702664,0.5356552,0.044299997],\n" + 
        "[\"LT\", \"Lithuania\",0.3186523,0.36363637,0.5735836,0.42933223,0.0041],\n" + 
        "[\"LY\", \"Libya\",0.20227121,0.0,0.0,0.48912388,0.0],\n" + 
        "[\"MC\", \"Monaco\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"MD\", \"Moldova\",0.30053195,0.71428573,0.6005118,0.4722923,1.0E-4],\n" + 
        "[\"MA\", \"Morocco\",0.20630594,0.4,0.0,0.49888045,0.0],\n" + 
        "[\"MG\", \"Madagascar\",0.22052363,0.0,0.0,0.53326106,0.0],\n" + 
        "[\"MH\", \"Marshall Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"ME\", \"Montenegro\",0.17880623,0.6666667,0.0,0.43238184,0.0],\n" + 
        "[\"MK\", \"Macedonia\",0.26054657,0.54545456,0.5248564,0.38764694,1.0E-4],\n" + 
        "[\"ML\", \"Mali\",0.0,0.5,0.0,0.0,0.0],\n" + 
        "[\"MN\", \"Mongolia\",0.18337564,0.25,0.0,0.44343135,0.0],\n" + 
        "[\"MM\", \"Myanmar (Burma)\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"MP\", \"Northern Mariana Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"MO\", \"Macau\",0.40300366,0.33333334,1.0,0.48408872,2.9999999E-4],\n" + 
        "[\"MR\", \"Mauritania\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"MQ\", \"Martinique\",0.19329931,0.0,0.0,0.46742836,0.0],\n" + 
        "[\"MT\", \"Malta\",0.26416835,0.4,0.44074455,0.4681742,2.0E-4],\n" + 
        "[\"MS\", \"Montserrat\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"MV\", \"Maldives\",0.18480927,0.5,0.0,0.4418108,2.9999999E-4],\n" + 
        "[\"MU\", \"Mauritius\",0.37082946,0.3888889,0.88599485,0.47859862,1.0E-4],\n" + 
        "[\"MX\", \"Mexico\",0.2544019,0.3392857,0.35640755,0.46169007,0.0],\n" + 
        "[\"MW\", \"Malawi\",0.0,0.125,0.0,0.0,0.0],\n" + 
        "[\"MZ\", \"Mozambique\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"MY\", \"Malaysia\",0.36707294,0.36082473,0.73595375,0.5345105,8.0E-4],\n" + 
        "[\"NA\", \"Namibia\",0.0,0.5,0.0,0.0,0.0],\n" + 
        "[\"NC\", \"New Caledonia\",0.1948392,0.625,0.9737083,0.0,1.0E-4],\n" + 
        "[\"NE\", \"Niger\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"NF\", \"Norfolk Island\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"NG\", \"Nigeria\",0.20874727,0.1,0.0,0.4996967,2.9999999E-4],\n" + 
        "[\"NI\", \"Nicaragua\",0.19665857,0.0,0.0,0.4755516,0.0],\n" + 
        "[\"NL\", \"Netherlands\",0.41042024,0.5954198,0.84233016,0.4634269,0.0076],\n" + 
        "[\"JP\", \"Japan\",0.4795984,0.4638009,0.7849239,0.2563003,0.031600002],\n" + 
        "[\"JO\", \"Jordan\",0.28597268,0.1875,0.47859234,0.4643774,0.0],\n" + 
        "[\"JM\", \"Jamaica\",0.19483227,0.0,0.0,0.47113532,0.0],\n" + 
        "[\"KI\", \"Kiribati\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"KH\", \"Cambodia\",0.28090006,0.34615386,0.42895576,0.48282203,4.0E-4],\n" + 
        "[\"KG\", \"Kyrgyzstan\",0.1506725,0.16666667,0.0,0.36434996,0.0],\n" + 
        "[\"KE\", \"Kenya\",0.23931563,0.32258064,0.22274849,0.49704546,4.0E-4],\n" + 
        "[\"KW\", \"Kuwait\",0.25875792,0.30769232,0.38062894,0.48038828,0.0],\n" + 
        "[\"KY\", \"Cayman Islands\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"KZ\", \"Kazakhstan\",0.2136779,0.5882353,0.44134727,0.33027655,0.0],\n" + 
        "[\"KP\", \"Korea (Democratic People's Repub\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"KR\", \"South Korea\",0.1651623,0.20634921,0.16437265,0.35783818,1.0E-4],\n" + 
        "[\"KM\", \"Comoros\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"KN\", \"Saint Kitts and Nevis\",0.16148108,0.0,0.80454576,0.0,0.0],\n" + 
        "[\"LI\", \"Liechtenstein\",0.19582503,0.8,0.91626114,0.0,0.0017],\n" + 
        "[\"LK\", \"Sri Lanka\",0.35010934,0.6875,0.64156115,0.5711766,4.0E-4],\n" + 
        "[\"LA\", \"Laos\",0.0,0.5,0.0,0.0,0.0],\n" + 
        "[\"LC\", \"Saint Lucia\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"LB\", \"Lebanon\",0.19079672,0.47368422,0.0,0.46137673,0.0],\n" + 
        "[\"HR\", \"Croatia\",0.32737315,0.5,0.6977277,0.4482712,2.9999999E-4],\n" + 
        "[\"HT\", \"Haiti\",0.0,0.2,0.0,0.0,0.0],\n" + 
        "[\"HU\", \"Hungary\",0.3332797,0.37313432,0.6249329,0.478753,0.0025],\n" + 
        "[\"HK\", \"Hong Kong\",0.36150178,0.43356642,0.75546294,0.4791311,0.0023999999],\n" + 
        "[\"ZA\", \"South Africa\",0.33401567,0.45054945,0.5911373,0.47100952,0.005],\n" + 
        "[\"HN\", \"Honduras\",0.20397831,0.30769232,0.0,0.48477307,5.0E-4],\n" + 
        "[\"ZW\", \"Zimbabwe\",0.0035063114,0.0,0.0,0.0,5.0E-4],\n" + 
        "[\"ID\", \"Indonesia\",0.3032629,0.288,0.47549266,0.49127078,0.001],\n" + 
        "[\"IE\", \"Ireland\",0.3789118,0.46666667,0.5462616,0.48136982,0.0114],\n" + 
        "[\"ZM\", \"Zambia\",0.0035063114,0.2,0.0,0.0,5.0E-4],\n" + 
        "[\"IQ\", \"Iraq\",0.1937994,0.071428575,0.0,0.46863765,0.0],\n" + 
        "[\"IR\", \"Iran\",0.14465651,0.20731707,0.1069532,0.2876356,0.0013],\n" + 
        "[\"YE\", \"Yemen\",0.1876474,0.0,0.0,0.45376113,0.0],\n" + 
        "[\"IS\", \"Iceland\",0.2867125,0.4848485,0.52350146,0.43578765,0.0019],\n" + 
        "[\"IT\", \"Italy\",0.30096492,0.38732395,0.6104546,0.43785563,2.0E-4],\n" + 
        "[\"IL\", \"Israel\",0.2348641,0.375,0.27468386,0.43601614,8.0E-4],\n" + 
        "[\"IM\", \"Isle of Man\",0.16569759,0.33333334,0.8174351,0.0,0.0013],\n" + 
        "[\"IN\", \"India\",0.38146797,0.15315315,0.7237233,0.5397615,0.0023999999],\n" + 
        "[\"IO\", \"British Indian Ocean Territory\",0.0,0.0,0.0,0.0,0.0],\n" + 
        "[\"JE\", \"Jersey\",0.20982727,0.33333334,1.0,0.0,0.0013],\n" + 
        "[\"YT\", \"Mayotte\",0.0,0.0,0.0,0.0,0.0]\n" + 
        "]";
    
      object = json.parse( large);
      System.out.println( object);
  }
}
