package org.xmodel.compress;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that captures all data read and makes it available as a byte array.
 */
public class CaptureInputStream extends FilterInputStream
{
  public CaptureInputStream( InputStream in)
  {
    super( in);
  }
  
  /**
   * @return Returns a DataInputStream that wraps this stream.
   */
  public DataInputStream getDataIn()
  {
    if ( dataIn == null) dataIn = new DataInputStream( this);
    return dataIn;
  }
  
  /**
   * @return Returns a byte array input stream containing the captured data.
   */
  public ByteArrayInputStream getCaptureStream()
  {
    if ( in instanceof ByteArrayInputStream)
    {
      return ((ByteArrayInputStream)in).snapshot();
    }
    else
    {
      ByteArrayInputStream stream = new ByteArrayInputStream( capture.toByteArray());
      capture = null;
      return stream;
    }
  }
  
  /**
   * Start capturing data.
   */
  public void startCapture()
  {
    if ( in instanceof ByteArrayInputStream)
    {
      ((ByteArrayInputStream)in).mark();
    }
    else
    {
      capture = new ByteArrayOutputStream();
    }
  }
  
  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read() throws IOException
  {
    int b = super.read();
    if ( capture != null)
    {
      if ( b >= 0) capture.write( b);
    }
    return b;
  }

  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read(byte[])
   */
  @Override
  public int read( byte[] b) throws IOException
  {
    return read( b, 0, b.length);
  }

  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read(byte[], int, int)
   */
  @Override
  public int read( byte[] b, int off, int len) throws IOException
  {
    int nread = super.read( b, off, len);
    if ( capture != null)
    {
      if ( nread > 0) capture.write( b, off, nread);
    }
    return nread;
  }

  private ByteArrayOutputStream capture;
  private DataInputStream dataIn;
}
