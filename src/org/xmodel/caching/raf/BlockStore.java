package org.xmodel.caching.raf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.xmodel.log.Log;

/**
 * A simple, random-access block storage algorithm.
 */
public class BlockStore
{
  /**
   * Create a block storage device with the specified minimum block size.
   * @param filename The path of the file store.
   * @param blockSize The smallest allocation unit in bytes.
   */
  public BlockStore( String filename, int blockSize, double recycleRatio) throws IOException
  {
    if ( new File( filename).exists()) throw new IllegalArgumentException( "File, '"+filename+"' exists.");
    
    this.blockSize = blockSize;
    this.recycle = new ConcurrentSkipListMap<Long, Pointer>();
    this.recycleRatio = (float)recycleRatio;
    this.allocLock = new Object();
    
    this.file = new RandomAccessFile( filename, "rwd");
    this.file.writeInt( blockSize);
  }
  
  /**
   * Open a block storage device that was already created via this class.
   * @param filename The path of the file store.
   */
  public BlockStore( String filename, double recycleRatio) throws IOException
  {
    if ( !new File( filename).exists()) throw new IllegalArgumentException( "File, '"+filename+"' does not exist.");
    
    this.recycle = new ConcurrentSkipListMap<Long, Pointer>();
    this.recycleRatio = (float)recycleRatio;
    this.allocLock = new Object();
    
    this.file = new RandomAccessFile( filename, "rwd");
    this.blockSize = file.readInt();
  }
  
  /**
   * Close this device.
   */
  public void close() throws IOException
  {
    file.close();
  }
  
  /**
   * Close this device quietly.
   */
  public void closeQuietly()
  {
    try
    {
      file.close();
    }
    catch( Exception e)
    {
      log.exception( e);
    }
  }
  
  /**
   * Allocate the specified number of bytes from the store.
   * @param length The number of bytes.
   * @return Returns a pointer to the allocated space. 
   */
  public Pointer alloc( long length) throws IOException
  {
    synchronized( allocLock)
    {
      Map.Entry<Long, Pointer> entry = recycle.higherEntry( length);
      if ( entry != null)
      {
        double ratio = (double)entry.getValue().length / length;
        if ( ratio <= recycleRatio)
        {
          Pointer pointer = recycle.remove( entry.getKey());
          if ( pointer != null) return pointer;
        }
      }
    }

    length += blockSize - length % blockSize;
    Pointer pointer = new Pointer( 0, length);
    
    synchronized( file)
    {
      pointer.position = file.getFilePointer() + 16;
      file.setLength( file.length() + 16 + length);
      return pointer;
    }
  }
  
  /**
   * Free the bytes associated with the specified pointer.
   * @param pointer The pointer.
   */
  public void free( Pointer pointer)
  {
    synchronized( file)
    {
      
      recycle.put( pointer.length, pointer);
    }
  }

  /**
   * File store pointer.
   */
  public static final class Pointer
  {
    public Pointer( long position, long length)
    {
      this.position = position;
      this.length = length;
    }
    
    public long position;
    public long length;
  }
  
  private final static Log log = Log.getLog( BlockStore.class);
  
  private Object allocLock;
  private RandomAccessFile file;
  private int blockSize;
  protected NavigableMap<Long, Pointer> recycle;
  private float recycleRatio;
}
