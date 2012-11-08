package org.xmodel.script;

public class CompileException extends Exception
{
  private static final long serialVersionUID = 5084357971990021708L;

  public CompileException()
  {
  }

  public CompileException( String message)
  {
    super( message);
  }

  public CompileException( Throwable cause)
  {
    super( cause);
  }

  public CompileException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
