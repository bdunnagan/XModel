/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * Refer to messages.xsd for documentation.
 */
public class SyncResponse extends Response
{
  public SyncResponse( String id)
  {
    super( id, "syncResponse");
  }
}
