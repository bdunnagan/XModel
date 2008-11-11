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
