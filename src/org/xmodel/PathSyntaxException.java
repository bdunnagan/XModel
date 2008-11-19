/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

@SuppressWarnings("serial")
public class PathSyntaxException extends Exception
{
  public PathSyntaxException( String message)
  {
    super( message);
  }
  
  public PathSyntaxException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
