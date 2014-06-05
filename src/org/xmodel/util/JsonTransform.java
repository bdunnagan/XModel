package org.xmodel.util;

import org.xmodel.IModelObject;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

public class JsonTransform
{
  public void transform( IModelObject node, StringBuilder json)
  {
    json.append( "{'");
    json.append( node.getType());
    json.append( "': ");
    
    if ( node.getAttributeNames().size() <= 1 && node.getChildren().size() == 0)
    {
      Object value = node.getValue();
      if ( value != null) transformValue( value, json); else json.append( "''");
    }
    else
    {
      json.append( "{");
      transformValue( node, json);    
      transformAttributes( node, json);
      transformChildren( node, json);
      json.append( "}");
    }
    
    json.append( "}");
  }

  private void transformValue( Object value, StringBuilder json)
  {
    if ( value instanceof Number)
    {
      json.append( ((Number)value).toString());
    }
    else if ( value instanceof Boolean)
    {
      json.append( ((Boolean)value).booleanValue()? "true": "false");
    }
    else
    {
      json.append( escape( value.toString()));
    }
  }
  
  /**
   * Serialize the attributes of an IModelObject into the specified JSON string.
   * @param node The node to be transformed.
   * @param json The JSON string.
   */
  public void transformAttributes( IModelObject node, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( "\"attributes\":");
    
    if ( pretty) json.append( ' ');
    json.append( "{");
    for( String attrName: node.getAttributeNames())
    {
      if ( attrName.length() > 0)
      {
        transformAttribute( node, attrName, json);
        json.append( ',');
        if ( pretty) json.append( ' ');
      }
    }
    
    int length = json.length();
    json.setLength( pretty? length-2: length-1);
    
    json.append( "}");
  }

  /**
   * Serialize an attribute (not a text node) of the specified node into the specified JSON string.
   * @param node The node.
   * @param attrName The attribute name.
   * @param json The JSON string.
   */
  public void transformAttribute( IModelObject node, String attrName, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( '\"'); 
    json.append( escape( attrName)); 
    json.append( "\":");
    
    if ( pretty) json.append( ' ');
    Object attrValue = node.getAttribute( attrName);
    json.append( escape( attrValue.toString()));
  }
  
  public void transformChildren( IModelObject node, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( "\"children\":");
    
    if ( pretty) json.append( ' ');
    json.append( "[");
    
    for( IModelObject child: node.getChildren())
      transform( child, json);
    
    json.append( "]");
    json.append( "}");
  }
  
  /**
   * Escape all JSON reserved characters in the specified string.
   * @param s The string to be escaped.
   * @return Returns the JSON escaped string.
   */
  public static String escape( String s)
  {
    StringBuilder sb = null;
    
    final int length = s.length();
    int start = 0;
    for( int i=0; i<length; i++)
    {
      char c = s.charAt( i);
      if ( c < 32 || c > 128)
      {
        if ( sb == null) sb = new StringBuilder();
        sb.append( s.substring( start, i));
        sb.append( "\\u");
        sb.append( String.format( "%04X", c));
        start = i+1;
      }
    }
    
    return (sb == null)? s: sb.toString();
  }
  
  private boolean pretty;
  
  public static void main( String[] args) throws Exception
  {
    String xml = 
        "<a>"+
//        "  <b i='1'>A</b>"+
//        "  <b i='2'>B</b>"+
//        "  <b i='3'>C</b>"+
        "</a>";
    
    IModelObject element = new XmlIO().read( xml);
    
    JsonTransform transform = new JsonTransform();
    StringBuilder json = new StringBuilder();
    transform.transform( element, json);
    System.out.println( json);
    
    JsonParser parser = new JsonParser();
    element = (IModelObject)parser.parse( json.toString());
    
    System.out.println( XmlIO.write( Style.printable, element));
  }
}
