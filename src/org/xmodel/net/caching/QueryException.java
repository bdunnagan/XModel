/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.caching;

/**
 * A generic protocol exception.
 */
@SuppressWarnings("serial")
public class QueryException extends Exception
{
  public QueryException()
  {
    super();
  }

  public QueryException( String message, Throwable cause)
  {
    super( message, cause);
  }

  public QueryException( String message)
  {
    super( message);
  }

  public QueryException( Throwable cause)
  {
    super( cause);
  }
}
