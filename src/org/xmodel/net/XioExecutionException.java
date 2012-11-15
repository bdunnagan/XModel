package org.xmodel.net;

public class XioExecutionException extends Exception
{
  private static final long serialVersionUID = 3303731300243685367L;

  public XioExecutionException()
  {
    super();
  }

  public XioExecutionException( String message, Throwable throwable)
  {
    super( message, throwable);
  }

  public XioExecutionException( String message)
  {
    super( message);
  }

  public XioExecutionException( Throwable throwable)
  {
    super( throwable);
  }

}
