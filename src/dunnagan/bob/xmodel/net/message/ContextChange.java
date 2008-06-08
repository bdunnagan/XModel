/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * A notification message sent to the client when the server context changes. The message 
 * does not carry any data. The client should respond by clear the cache of its root 
 * external reference.
 */
public class ContextChange extends Message
{
  public ContextChange()
  {
    super( "context");
  }
}
