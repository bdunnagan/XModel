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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream which delegates to an instance of ISession.
 */
public class SessionInputStream extends InputStream
{
  protected SessionInputStream( ISession session)
  {
    this.oneByte = new byte[ 1];
    this.session = session;
  }
  
  /* (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException
  {
    if ( session.read( oneByte) == -1) 
      throw new EOFException();
    return (int)oneByte[ 0] & 0xff;
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read(byte[])
   */
  @Override
  public int read( byte[] buffer) throws IOException
  {
    return session.read( buffer);
  }

  /* (non-Javadoc)
   * @see java.io.InputStream#read(byte[], int, int)
   */
  @Override
  public int read( byte[] buffer, int offset, int length) throws IOException
  {
    return session.read( buffer, offset, length);
  }
  
  private byte[] oneByte;
  private ISession session;
}
