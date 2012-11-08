package org.xmodel.script;

public class ExecuteException extends Exception
{
  private static final long serialVersionUID = 4481960773056201655L;

  public ExecuteException()
  {
  }

  public ExecuteException( String message)
  {
    super( message);
  }

  public ExecuteException( Throwable cause)
  {
    super( cause);
  }

  public ExecuteException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
