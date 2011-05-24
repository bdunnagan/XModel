package org.xmodel.net.nu.msg;

public class ErrorResponse extends StringMessage
{
  public final static byte type = 0x3f;
  
  public ErrorResponse( String message)
  {
    super( type, message);
  }
}
