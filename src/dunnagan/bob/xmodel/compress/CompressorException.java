/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.compress;

public class CompressorException extends RuntimeException
{
  public CompressorException()
  {
    super();
  }

  public CompressorException( String message, Throwable cause)
  {
    super( message, cause);
  }

  public CompressorException( String message)
  {
    super( message);
  }

  public CompressorException( Throwable cause)
  {
    super( cause);
  }
}
