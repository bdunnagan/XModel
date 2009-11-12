/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FileServer.java
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileServer extends Server
{
  public FileServer( String input)
  {
    file = new File( input);
    if ( !file.exists()) throw new IllegalArgumentException( "File does not exist: "+input);
    
    addHandler( new ServerHandler() {
      public void run( IServerSession session)
      {
        try
        {
          writeTo( session);
        }
        catch( IOException e)
        {
          e.printStackTrace( System.err);
        }
      }
    });
  }
  
  private void writeTo( IServerSession session) throws IOException
  {
    // write length
    DataOutputStream dataOut = new DataOutputStream( session.getOutputStream());
    dataOut.writeLong( file.length());
    
    // write file
    WritableByteChannel out = Channels.newChannel( session.getOutputStream());
    ReadableByteChannel in = Channels.newChannel( new FileInputStream( file));
    ByteBuffer buffer = ByteBuffer.allocateDirect( 1 << 15);
    int nread = in.read( buffer);
    while( nread >= 0)
    {
      buffer.flip();
      out.write( buffer);
      buffer.compact();
      nread = in.read( buffer);
    }
    in.close();
    out.close();
  }
  
  private File file;

  public static void main( String[] args) throws Exception
  {
    if ( args.length < 1)
      throw new IllegalArgumentException(
        "Usage: <input_file>");
    
    FileServer server = new FileServer( args[ 0]);
    server.start( 8080);
  }
}
