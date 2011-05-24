package org.xmodel.net.nu.msg;

public class AttachResponse extends BytesMessage
{
  public final static byte type = 4;
  
  public AttachResponse( byte[] content)
  {
    super( type, content);
  }
}
