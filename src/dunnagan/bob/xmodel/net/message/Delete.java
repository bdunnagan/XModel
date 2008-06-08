/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * Refer to messages.xsd for documentation.
 */
public class Delete extends Message
{
  public Delete( String id)
  {
    super( "delete");
    content.getCreateChild( "id").setValue( id);
  }
}
