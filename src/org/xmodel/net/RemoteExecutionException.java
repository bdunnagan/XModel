package org.xmodel.net;

public class RemoteExecutionException extends Exception
{
  private static final long serialVersionUID = 3303731300243685367L;

  public RemoteExecutionException()
  {
    super();
  }

  public RemoteExecutionException( String message, Throwable throwable)
  {
    super( message, throwable);
  }

  public RemoteExecutionException( String message)
  {
    super( message);
  }

  public RemoteExecutionException( Throwable throwable)
  {
    super( throwable);
  }

}
