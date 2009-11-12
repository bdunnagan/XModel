/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SessionInputStream.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
