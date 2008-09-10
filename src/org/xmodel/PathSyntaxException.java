/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
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
