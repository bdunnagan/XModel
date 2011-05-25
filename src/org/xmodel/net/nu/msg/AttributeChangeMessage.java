package org.xmodel.net.nu.msg;

public class AttributeChangeMessage extends Message
{
  public final static byte type = 6;
  
  public AttributeChangeMessage( String path, String attrName, byte[] attrValue)
  {
    super( type);
    
    setLength( (4 + path.length()) + (4 + attrName.length()) + (4 + attrValue.length));
    
    this.path = path;
    this.attrName = attrName;
    this.attrValue = attrValue;
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
  
  /**
   * @return Returns the attribute value.
   */
  public byte[] getAttrValue()
  {
    return attrValue;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#read()
   */
  @Override
  protected void read()
  {
    path = readString();
    attrName = readString();
    attrValue = readBytes();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.msg.Message#write()
   */
  @Override
  protected void write()
  {
    writeString( path);
    writeString( attrName);
    writeBytes( attrValue, 0, attrValue.length);
  }

  private String path;
  private String attrName;
  private byte[] attrValue;
}
