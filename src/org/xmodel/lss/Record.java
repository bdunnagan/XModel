package org.xmodel.lss;

import java.io.IOException;

public class Record<K>
{
  public Record( IRecordFormat<K> format)
  {
    this.format = format;
  }
  
  public Record( IRecordFormat<K> format, K key, byte[] content, boolean garbage)
  {
    this.format = format;
    this.key = key;
    this.content = content;
    this.garbage = garbage;
  }

  public boolean isGarbage()
  {
    return garbage;
  }
  
  public long getLength()
  {
    return content.length;
  }
  
  public K getKey() throws IOException
  {
    if ( key == null && content != null)
      key = format.extractKeyFromRecord( content);
    return key;
  }
  
  public byte[] getContent()
  {
    return content;
  }
  
  protected IRecordFormat<K> format;
  protected K key;
  protected byte[] content;
  protected boolean garbage;
}
