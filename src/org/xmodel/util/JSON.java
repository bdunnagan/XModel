package org.xmodel.util;

import org.xmodel.IModelObject;

/**
 * Class that transforms between a JSON string and an IModelObject.
 */
public class JSON
{
  /**
   * Serialize an IModelObject into the specified JSON string.
   * @param node The node to be serialized.
   * @param json The JSON string.
   */
  public void serialize( IModelObject node, StringBuilder json)
  {
    json.append( "{");
    serializeAttributes( node, json);
    serializeValue( node, json);    
    serializeChildren( node, json);
    json.append( "}");
  }

  public void serializeValue( IModelObject node, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( "\"value\":");
    
    if ( pretty) json.append( ' ');
    Object attrValue = node.getValue();
    json.append( escape( attrValue.toString()));
  }
  
  /**
   * Serialize the attributes of an IModelObject into the specified JSON string.
   * @param node The node to be serialized.
   * @param json The JSON string.
   */
  public void serializeAttributes( IModelObject node, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( "\"attributes\":");
    
    if ( pretty) json.append( ' ');
    json.append( "{");
    for( String attrName: node.getAttributeNames())
    {
      if ( attrName.length() > 0)
      {
        serializeAttribute( node, attrName, json);
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
  public void serializeAttribute( IModelObject node, String attrName, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( '\"'); 
    json.append( escape( attrName)); 
    json.append( "\":");
    
    if ( pretty) json.append( ' ');
    Object attrValue = node.getAttribute( attrName);
    json.append( escape( attrValue.toString()));
  }
  
  public void serializeChildren( IModelObject node, StringBuilder json)
  {
    if ( pretty) json.append( ' ');
    json.append( "\"children\":");
    
    if ( pretty) json.append( ' ');
    json.append( "[");
    
    for( IModelObject child: node.getChildren())
      serialize( child, json);
    
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
}
