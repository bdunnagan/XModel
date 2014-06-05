package org.xmodel.compress;

public class ByteArrayInputStream extends java.io.ByteArrayInputStream
{
  public ByteArrayInputStream( byte[] buf)
  {
    super( buf);
  }

  public ByteArrayInputStream( byte[] buf, int offset, int length)
  {
    super( buf, offset, length);
  }

  /* (non-Javadoc)
   * @see java.io.ByteArrayInputStream#mark(int)
   */
  @Override
  public void mark( int readAheadLimit)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Mark the current location in the stream so that the <code>snapshot</code> 
   * method will return the an input stream beginning at the current offset. 
   */
  public void mark()
  {
    mark = pos;
  }
  
  /* (non-Javadoc)
   * @see java.io.ByteArrayInputStream#reset()
   */
  @Override
  public void reset()
  {
    pos = mark;
  }

  /**
   * @return Returns a snapshot of the stream at the time of the last mark.
   */
  public ByteArrayInputStream snapshot()
  {
    int length = pos - mark;
    return new ByteArrayInputStream( buf, mark, length);
  }
  
  private int mark;
}
