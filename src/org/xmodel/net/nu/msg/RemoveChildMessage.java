package org.xmodel.net.nu.msg;

public class RemoveChildMessage extends Message
{
  public final static byte type = 6;
  
  public RemoveChildMessage( String path, int index)
  {
    super( type);
    
    setLength( (4 + path.length()) + 4);
    
    this.path = path;
    this.index = index;
  }
  
  /**
   * @return Returns the index-path relative to the root.
   */
  public String getPath()
  {
    return path;
  }

  /**
   * @return Returns the insertion index.
   */
  public int getIndex()
  {
    return index;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    path = readString();
    index = readInt();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeString( path);
    writeInt( index);
  }

  private String path;
  private int index;
}
