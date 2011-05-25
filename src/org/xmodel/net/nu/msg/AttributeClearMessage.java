package org.xmodel.net.nu.msg;

public class AttributeClearMessage extends Message
{
  public final static byte type = 6;
  
  public AttributeClearMessage( String path, String attrName)
  {
    super( type);
    
    setLength( (4 + path.length()) + (4 + attrName.length()));
    
    this.path = path;
    this.attrName = attrName;
  }
  
  /**
   * @return Returns the index-path relative to the root.
   */
  public String getPath()
  {
    return path;
  }

  /**
   * @return Returns the attribute name.
   */
  public String getAttrName()
  {
    return attrName;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    path = readString();
    attrName = readString();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeString( path);
    writeString( attrName);
  }

  private String path;
  private String attrName;
}
