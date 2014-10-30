package org.xmodel.json;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.util.JsonParser;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.function.StringFunction;

/**
 * Read/write element as json.
 */
public class JsonIO
{
  @SuppressWarnings("unchecked")
  public List<IModelObject> read( String json) throws ParseException
  {
    Object parsed = parser.parse( json);
    if ( parsed instanceof List) return (List<IModelObject>)parsed;
    return Collections.singletonList( (IModelObject)parsed);
  }
  
  public String write( List<IModelObject> elements)
  {
    StringBuilder json = new StringBuilder();
    write( elements, json);
    return json.toString();
  }
  
  public void write( List<IModelObject> elements, StringBuilder json)
  {
    json.append( '[');
    
    for( IModelObject element: elements)
    {
      write( element, json);
      json.append( ',');
    }
    
    removeTrailingSeperator( json);
    json.append( ']');
  }
  
  public String write( IModelObject element)
  {
    StringBuilder json = new StringBuilder();
    write( element, json);
    return json.toString();
  }

  public void write( IModelObject element, StringBuilder json)
  {
    json.append( "{\"");
    json.append( element.getType());
    json.append( "\":");
    writeContent( element, json);
    json.append( '}');
  }
  
  protected void writeContent( IModelObject element, StringBuilder json)
  {
    if ( !isComplex( element))
    {
      writeSimpleContent( element, json);
    }
    else
    {
      writeComplexContent( element, json);
    }
  }
  
  protected void writeSimpleContent( IModelObject element, StringBuilder json)
  {
    writeValue( element.getValue(), json);
  }
  
  protected void writeComplexContent( IModelObject element, StringBuilder json)
  {
    json.append( '{');
    json.append( '\n');
    
    for( String attrName: element.getAttributeNames())
    {
      json.append( "\"");
      json.append( (attrName.length() == 0)? "val": attrName);
      json.append( "\":");
      writeValue( element.getAttribute( attrName), json);
      json.append( ',');
    }
    
    Map<String, List<IModelObject>> groups = groupChildrenByType( element.getChildren());
    if ( groups != null)
    {
      for( Map.Entry<String, List<IModelObject>> entry: groups.entrySet())
      {
        json.append( "\"");
        json.append( entry.getKey());
        json.append( "\":");
        
        List<IModelObject> group = entry.getValue();
        if ( group.size() == 1 && !isComplex( group.get( 0)))
        {
          writeSimpleContent( group.get( 0), json);
          json.append( ',');
        }
        else if ( group.size() == 1)
        {
          IModelObject child = group.get( 0);
          if ( isValueOnly( child))
          {
            writeSimpleContent( child, json);
          }
          else
          {
            writeComplexContent( child, json);
          }
          json.append( ',');
        }
        else
        {
          json.append( "[");
          
          for( IModelObject child: group)
          {
            if ( isValueOnly( child))
            {
              writeSimpleContent( child, json);
            }
            else
            {
              writeComplexContent( child, json);
            }
            json.append( ',');
          }
          
          removeTrailingSeperator( json);
          json.append( ']');
          json.append( ',');
        }
      }
    }
    
    removeTrailingSeperator( json);
    json.append( '\n');
    json.append( '}');
  }
  
  protected boolean isValueOnly( IModelObject element)
  {
    Collection<String> attributes = element.getAttributeNames();
    boolean hasValue = attributes.contains( "");
    return attributes.size() == 1 && hasValue && element.getNumberOfChildren() == 0; 
  }
  
  protected boolean isComplex( IModelObject element)
  {
    Collection<String> attributes = element.getAttributeNames();
    boolean hasValue = attributes.contains( "");
    return (hasValue && attributes.size() > 1) || (!hasValue && attributes.size() > 0) || element.getNumberOfChildren() > 0;
  }
  
  protected Map<String, List<IModelObject>> groupChildrenByType( List<IModelObject> children)
  {
    if ( children.size() == 0) return null;
    Map<String, List<IModelObject>> map = new HashMap<String, List<IModelObject>>();
    for( IModelObject child: children)
    {
      List<IModelObject> list = map.get( child.getType());
      if ( list == null)
      {
        list = new ArrayList<IModelObject>();
        map.put( child.getType(), list);
      }
      list.add( child);
    }
    return map;
  }
  
  protected void writeValue( Object value, StringBuilder json)
  {
    if ( value == null)
    {
      json.append( "null");
    }
    else if ( value instanceof Number)
    {
      json.append( StringFunction.stringValue( (Number)value));
    }
    else if ( value instanceof Boolean)
    {
      json.append( value.toString());
    }
    else 
    {
      json.append( '"');
      writeString( value.toString(), json);
      json.append( '"');
    }
  }
  
  protected void writeString( String s, StringBuilder json)
  {
    for( int i=0; i<s.length(); i++)
    {
      char c = s.charAt( i);
      if ( c == '"')
      {
        json.append( '\\');
        json.append( c);
      }
      else
      {
        json.append( c);
      }
    }
  }
  
  protected void removeTrailingSeperator( StringBuilder json)
  {
    int n = json.length();
    if ( json.charAt( n-1) == ',') json.setLength( n-1);
  }
    
  private JsonParser parser = new JsonParser();
  
  public static void main( String[] args) throws Exception
  {
    // TESTS
    // 1. no value, no attrs, no child
   
    String xml =
      "<serviceProfile id=\"...\" name=\"My Service Profile\">"+
      "  <loadBalancer instrumentAddress=\"1.1.1.1\" app_resource_id=\"...\" vm_resource_id=\"...\" kvm_resource_id=\"...\" host_resource_id=\"...\"/>"+
      "  <webServer instrumentAddress=\"10.1.1.1\" app_resource_id=\"...\" vm_resource_id=\"...\" kvm_resource_id=\"...\" host_resource_id=\"...\"/>"+
      "  <webServer instrumentAddress=\"10.1.1.2\" app_resource_id=\"...\" vm_resource_id=\"...\" kvm_resource_id=\"...\" host_resource_id=\"...\"/>"+
      "  <webServer instrumentAddress=\"10.1.1.3\" app_resource_id=\"...\" vm_resource_id=\"...\" kvm_resource_id=\"...\" host_resource_id=\"...\"/>"+
      "</serviceProfile>";
    
    IModelObject el = new XmlIO().read( xml);
    
    System.out.println( XmlIO.write( Style.printable, el));
    
    JsonIO json = new JsonIO();
    String s = json.write( el);
    System.out.println( s);
 
    el = json.read( s).get( 0);
    System.out.println( XmlIO.write( Style.printable, el));
  }
}
