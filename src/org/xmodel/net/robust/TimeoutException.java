/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.robust;

@SuppressWarnings("serial")
public class TimeoutException extends Exception
{
  public TimeoutException()
  {
    super();
  }

  public TimeoutException( String message, Throwable cause)
  {
    super( message, cause);
  }

  public TimeoutException( String message)
  {
    super( message);
  }

  public TimeoutException( Throwable message)
  {
    super( message);
  }
}
