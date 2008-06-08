/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelObject;
import dunnagan.bob.xmodel.xml.XmlIO;

/**
 * Base class for all messages.
 */
public class Message
{
  public Message( String type)
  {
    content = new ModelObject( type);
  }
  
  public Message( String type, String id)
  {
    content = new ModelObject( type);
    content.setID( id);
  }
  
  /**
   * Add a result to the message.
   * @param value The result.
   */
  public void setResult( String value)
  {
    content.getCreateChild( "string").setValue( value);
  }

  /**
   * Add a result to the message.
   * @param value The result.
   */
  public void setResult( double value)
  {
    content.getCreateChild( "string").setValue( value);
  }

  /**
   * Add a result to the message.
   * @param value The result.
   */
  public void setResult( boolean value)
  {
    content.getCreateChild( "string").setValue( value);
  }

  /**
   * Add a result to the message.
   * @param element The result element.
   */
  public void setResult( IModelObject element)
  {
    content.getCreateChild( "nodes").addChild( element);
  }

  /**
   * Add a result to the message.
   * @param elements The result elements.
   */
  public void setResult( List<IModelObject> elements)
  {
    IModelObject result = new ModelObject( "result");
    IModelObject type = result.getCreateChild( "nodes");
    for( IModelObject element: elements) type.addChild( element);
    content.addChild( result);
  }

  /**
   * Create a pre-formatted exception for this message.
   * @param error The message to append to the exception.
   * @return Returns the new exception.
   */
  protected MessageException createException( String error)
  {
    StringBuilder builder = new StringBuilder();
    builder.append( content.toString()); builder.append( ": "); builder.append( error);
    return new MessageException( builder.toString());
  }

  /**
   * Returns the xml of the message.
   * @return Returns the xml of the message.
   */
  public String toXml()
  {
    return (new XmlIO()).write( content);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return content.toString();
  }

  public IModelObject content;
}
