package org.xmodel.net.nu.msg;

public class AttachMessage extends StringMessage
{
  public final static byte type = 1;
  
  public AttachMessage( String xpath)
  {
    super( type, xpath);
  }
}
