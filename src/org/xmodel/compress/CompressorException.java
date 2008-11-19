/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.compress;

@SuppressWarnings("serial")
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
