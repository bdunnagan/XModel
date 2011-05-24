package org.xmodel.net.nu.msg;

public class SyncMessage extends StringMessage
{
  public final static byte type = 3;
  
  public SyncMessage( String key)
  {
    super( type, key);
  }
}
