/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

public class MessageException extends Exception
{
  public MessageException()
  {
  }

  public MessageException( String message)
  {
    super( message);
  }

  public MessageException( Throwable cause)
  {
    super( cause);
  }

  public MessageException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
