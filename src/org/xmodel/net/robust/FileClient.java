/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FileClient.java
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class FileClient extends Client
{
  public FileClient( String host, String output)
  {
    super( host, 8080);
    
    String base = System.getProperty( "user.dir");
    System.out.println( "base="+base);
    file = new File( output);
    if ( !file.isAbsolute()) file = new File( base, output);
    
    if ( file.exists()) throw new IllegalArgumentException( "File exists: "+output);
    
    addListener( new IListener() {
      public void notifyOpen( ISession session)
      {
      }
      public void notifyClose( ISession session)
      {
      }
      public void notifyConnect( ISession session)
      {
        System.out.println( "connected...");
        try
        {
          readFrom( session);
        }
        catch( IOException e)
        {
          e.printStackTrace( System.err);
        }
        finally
        {
          session.close();
        }
      }
      public void notifyDisconnect( ISession session)
      {
        System.out.println( "disconnected.");
      }
    });
  }
  
  private void readFrom( ISession session) throws IOException
  {
    // read length
    DataInputStream dataIn = new DataInputStream( session.getInputStream());
    long length = dataIn.readLong();

    // read
    WritableByteChannel out = Channels.newChannel( new FileOutputStream( file));
    ReadableByteChannel in = Channels.newChannel( session.getInputStream());
    ByteBuffer buffer = ByteBuffer.allocateDirect( 1 << 16);
    int total = 0;
    int benchmark = 0;
    int nread = in.read( buffer);
    while( nread >= 0)
    {
      total += nread;
      benchmark += nread;
      if ( benchmark > 1000000) 
      {
        System.out.printf( "%3.2fMB of %3.2fMB\n", total / 1000000f, length / 1000000f);
        benchmark = 0;
      }
      buffer.flip();
      out.write( buffer);
      buffer.compact();
      if ( total > length) throw new IllegalStateException( "File transfer corrupted.");
      if ( total == length) break;
      nread = in.read( buffer);
    }
    System.out.printf( "downloaded: %3.2f MB\n", total / 1000000f);
    out.close();
    in.close();
  }
  
  private File file;
  
  public static void main( String[] args) throws Exception
  {
    if ( args.length < 2)
      throw new IllegalArgumentException(
        "Usage: <host> <output_file>");
    
    FileClient client = new FileClient( args[ 0], args[ 1]);
    client.open();
    
    while( client.isOpen())
    {
      try { Thread.sleep( 250);} catch( Exception e) {};
    }
    
    System.out.println( "done.");
  }
}
