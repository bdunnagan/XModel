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
