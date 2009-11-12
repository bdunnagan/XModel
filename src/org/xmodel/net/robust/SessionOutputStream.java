/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SessionOutputStream.java
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
