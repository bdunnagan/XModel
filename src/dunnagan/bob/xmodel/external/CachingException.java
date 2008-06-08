/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

/**
 * An exception which is thrown by implementations of ICachingPolicy.
 */
public class CachingException extends RuntimeException
{
  /**
   * Create a CachingException with the specified message.
   * @param message The exception message.
   */
  public CachingException( String message)
  {
    super( message);
  }
  
  /**
   * Create a CachingException with the specified message and cause.
   * @param message The exception message.
   * @param cause The exception which caused this exception.
   */
  public CachingException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
