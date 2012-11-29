package org.xmodel.net;

public class XioException extends Exception
{
  private static final long serialVersionUID = 4445010712712665029L;

  public XioException()
  {
    super();
  }

  public XioException( String message, Throwable throwable)
  {
    super( message, throwable);
  }

  public XioException( String message)
  {
    super( message);
  }

  public XioException( Throwable throwable)
  {
    super( throwable);
  }

}
