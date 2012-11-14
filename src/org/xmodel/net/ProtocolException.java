package org.xmodel.net;

public class ProtocolException extends Exception
{
  private static final long serialVersionUID = 4445010712712665029L;

  public ProtocolException()
  {
    super();
  }

  public ProtocolException( String message, Throwable throwable)
  {
    super( message, throwable);
  }

  public ProtocolException( String message)
  {
    super( message);
  }

  public ProtocolException( Throwable throwable)
  {
    super( throwable);
  }

}
