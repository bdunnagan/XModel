package org.xmodel.net;

import java.io.IOException;

public class XioException extends IOException
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
