package org.xmodel.lss;

public class Record
{
  public Record()
  {
  }
  
  /**
   * @return Returns true if this is a data record.
   */
  public boolean isData()
  {
    return flags == 0;
  }
  
  /**
   * @return Returns true if this is an index node.
   */
  public boolean isIndex()
  {
    return (flags & (StorageController.nodeFlag | StorageController.leafFlag)) != 0;
  }
  
  /**
   * @return Returns true if this is an index leaf node.
   */
  public boolean isIndexLeaf()
  {
    return (flags & StorageController.leafFlag) != 0;
  }
  
  /**
   * @return Returns true if this record is marked garbage.
   */
  public boolean isGarbage()
  {
    return (flags & StorageController.garbageFlag) != 0;
  }
  
  /**
   * Set the garbage field.
   * @param garbage True marks the record as garbage.
   */
  public void setGarbage( boolean garbage)
  {
    flags |= StorageController.garbageFlag;
  }
  
  /**
   * @return Returns the header flags.
   */
  public byte getFlags()
  {
    return flags;
  }
  
  /**
   * Set the header flags.
   * @param flags The flags.
   */
  public void setFlags( byte flags)
  {
    this.flags = flags;
  }

  /**
   * @return Returns the length of the record.
   */
  public long getLength()
  {
    return length;
  }
  
  /**
   * Set the length field.
   * @param length The length.
   */
  public void setLength( long length)
  {
    this.length = length;
  }
  
  /**
   * @return Returns the content of the record.
   */
  public byte[] getContent()
  {
    return content;
  }
  
  /**
   * Set the content of the record.
   * @param content The content.
   */
  public void setContent( byte[] content)
  {
    this.content = content;
  }

  private byte flags;
  private long length;
  private byte[] content;
}
