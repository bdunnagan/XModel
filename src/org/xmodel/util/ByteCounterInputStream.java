package org.xmodel.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ByteCounterInputStream extends FilterInputStream
{
  public ByteCounterInputStream( InputStream in)
  {
    super( in);
    counter = new AtomicLong( 0);
  }
  
  /**
   * @return Returns the current count of bytes read.
   */
  public long count()
  {
    return counter.get();
  }

  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read()
   */
  @Override
  public int read() throws IOException
  {
    int b = super.read();
    if ( b >= 0) counter.incrementAndGet();
    return b;
  }

  /* (non-Javadoc)
   * @see java.io.FilterInputStream#read(byte[], int, int)
   */
  @Override
  public int read( byte[] b, int off, int len) throws IOException
  {
    int n = super.read( b, off, len);
    if ( n >= 0) counter.addAndGet( n);
    return n;
  }
  
  private AtomicLong counter;
}
