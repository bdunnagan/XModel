/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Feb 25, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package dunnagan.bob.xmodel.net.robust;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream which delegates to an instance of ISession.
 */
public class SessionOutputStream extends OutputStream
{
  protected SessionOutputStream( ISession session)
  {
    this.oneByte = new byte[ 1];
    this.session = session;
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(int)
   */
  @Override
  public void write( int b) throws IOException
  {
    oneByte[ 0] = (byte)b;
    session.write( oneByte);
  }
  
  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[])
   */
  @Override
  public void write( byte[] buffer) throws IOException
  {
    if ( !session.write( buffer))
      throw new IOException( "Unsuccessful write operation.");
  }

  /* (non-Javadoc)
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  @Override
  public void write( byte[] buffer, int offset, int length) throws IOException
  {
    if ( !session.write( buffer, offset, length))
      throw new IOException( "Unsuccessful write operation.");
  }

  private byte[] oneByte;
  private ISession session;
}
