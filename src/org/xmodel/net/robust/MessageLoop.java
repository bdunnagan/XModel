/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * MessageLoop.java
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

import java.io.InputStream;
import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;

/**
 * A class which implements a message processing loop in a dedicated thread.
 */
public abstract class MessageLoop implements Runnable
{
  /**
   * Create a message loop with the specified thread name.
   * @param name The name of the message loop thread.
   * @param session The session to which this message loop belongs.
   */
  public MessageLoop( String name, ISession session)
  {
    this( name, session, new TabularCompressor( PostCompression.none));
  }
  
  /**
   * Create a message loop with the specified thread name.
   * @param name The name of the message loop thread.
   * @param session The session to which this message loop belongs.
   * @param compressor The compressor to use to decompress incoming messages.
   */
  public MessageLoop( String name, ISession session, ICompressor compressor)
  {
    this.thread = new Thread( this, name);
    this.session = session;
    this.compressor = compressor;
  }

  /**
   * Called when a message is received in the message loop.
   * @param message The message.
   */
  public abstract void handle( IModelObject message);

  /**
   * Called when the message loop is terminated by a caught throwable. 
   * @param t The throwable that was intercepted.
   */
  public abstract void terminated( Throwable t);
  
  /**
   * Start the message loop processing.
   */
  public void start()
  {
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Stop the message loop processing. The message loop must be discarded
   * after this method is called.
   */
  public void stop()
  {
    if ( thread == null) return;

    try
    {
      exit = true;
      thread.interrupt();
      thread.join();
    }
    catch( InterruptedException e)
    {
    }
    finally
    {
      thread = null;
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    while( !exit)
    {
      try
      {
        InputStream stream = session.getInputStream();
        while( !exit)
        {
          IModelObject message = compressor.decompress( stream);
          handle( message);
        }
      }
      catch( Throwable t)
      {
        if ( !exit) terminated( t);
        exit = true;
      }
    }
  }
  
  private boolean exit;
  private Thread thread;
  private ISession session;
  private ICompressor compressor;
}
