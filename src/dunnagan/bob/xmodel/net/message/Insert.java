/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;


/**
 * Refer to messages.xsd for documentation.
 */
public class Insert extends Message
{
  public Insert( String parentID, int index)
  {
    super( "insert");
    content.getCreateChild( "id").setValue( parentID);
    content.getCreateChild( "index").setValue( index);
  }
}
