/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.robust;

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
