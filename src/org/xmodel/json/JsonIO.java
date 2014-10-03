package org.xmodel.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.function.StringFunction;

/**
 * Read/write element as json.
 */
public class JsonIO
{
  public List<IModelObject> read( String json, String ... attributes)
  {
    // TODO: implement and use xsd 
    throw new UnsupportedOperationException();
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
    
    for( String attrName: element.getAttributeNames())
    {
      json.append( "\"");
      json.append( (attrName.length() > 0)? attrName: "val");
      json.append( "\":");
      writeValue( element.getAttribute( attrName), json);
      json.append( ',');
    }
    
    List<IModelObject> children = new ArrayList<IModelObject>( element.getChildren());
    for( int i=0; i<children.size(); i++)
    {
      IModelObject child = children.get( i);
      if ( !isComplex( child))
      {
        json.append( "\"");
        json.append( child.getType());
        json.append( "\":");
        writeSimpleContent( child, json);
        json.append( ',');
      }
      else
      {
        json.append( "\"");
        json.append( child.getType());
        json.append( "\":[");
        writeComplexContent( child, json);
        json.append( ',');
        
        for( int j=i+1; j<children.size(); j++)
        {
          IModelObject child2 = children.get( j);
          if ( child2.getType().equals( child.getType()))
          {
            children.remove( j--);
            writeComplexContent( child2, json);
            json.append( ',');
          }
        }
        
        removeTrailingSeperator( json);
        json.append( ']');
        json.append( ',');
      }
    }
    
    removeTrailingSeperator( json);
    json.append( '}');
  }
  
  protected boolean isComplex( IModelObject element)
  {
    Collection<String> attributes = element.getAttributeNames();
    boolean hasValue = attributes.contains( "");
    return (hasValue && attributes.size() > 1) || (!hasValue && attributes.size() > 0) || element.getNumberOfChildren() > 0;
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
  
  public static void main( String[] args) throws Exception
  {
    // TESTS
    // 1. no value, no attrs, no child
    
    IModelObject el = new ModelObject( "result");
    IModelObject c1;
    
    c1 = new ModelObject( "sample");
    Xlate.set( c1, "id", "A");
    Xlate.set( c1, "timestamp", 1);
    Xlate.set( c1, 1);
    el.addChild( c1);
    
    c1 = new ModelObject( "x");
    //Xlate.set( c1, 2);
    el.addChild( c1);
    
    IModelObject c2;
    c2 = new ModelObject( "y");
    Xlate.set( c2, true);
    c1.addChild( c2);
    
    c1 = new ModelObject( "sample");
    Xlate.set( c1, "id", "B");
    Xlate.set( c1, "timestamp", 2);
    Xlate.set( c1, 2);
    el.addChild( c1);
    
    c1 = new ModelObject( "x");
    //Xlate.set( c1, 2);
    el.addChild( c1);
    
    JsonIO json = new JsonIO();
    System.out.println( json.write( el));
  }
}
