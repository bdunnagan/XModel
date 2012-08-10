/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * BlockingDispatcher.java
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
package org.xmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.xmodel.log.Log;

/**
 * An implementation of IDispatcher which allows runnables to be executed in the current thread.
 * This implementation assumes that the producer is executing in another thread.
 */
public class BlockingDispatcher implements IDispatcher
{
  public BlockingDispatcher()
  {
    queue = new LinkedBlockingQueue<Runnable>();
    dequeued = new ArrayList<Runnable>();
    shutdown = 0;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IDispatcher#execute(java.lang.Runnable)
   */
  public void execute( Runnable runnable)
  {
    log.verbosef( "Enqueue runnable: %s", runnable.getClass().getSimpleName());
    if ( !receiving) 
      log.warn( "process() has not been called, yet.");
    try
    {
      if ( shutdown == 0) queue.put( runnable);
    }
    catch( InterruptedException e)
    {
      log.warnf( "Enqueue was interrupted for runnable, %s", runnable.getClass().getSimpleName());
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    shutdown = immediate? 2: 1;
    
    queue.offer( new Runnable() {
      public void run()
      {
        log.warn( "BlockingDispatcher has been shutdown.");
      }
    });
  }

  /**
   * Dequeue and execute all the runnables on the queue.
   * @return Returns false if dispatcher has been shutdown.
   */
  public boolean process()
  {
    receiving = true;
    
    try
    {
      if ( shutdown > 0) return false;
      
      log.verbose( "Waiting for dequeue ...");
      Runnable first = queue.poll( 10, TimeUnit.SECONDS);
      
      if ( shutdown == 2) return false;
      
      if ( first != null)
      {
        dequeued.add( first);
        queue.drainTo( dequeued);
        
        log.verbosef( "Dequeued %d runnables: ", dequeued.size());
        for( Runnable runnable: dequeued)
        {
          if ( runnable != null) 
          {
            log.verbosef( "Executing runnable: %s", runnable.getClass().getSimpleName());
            try
            {
              runnable.run();
            }
            catch( Exception e)
            {
              log.exception( e);
            }
          }
        }
        
        dequeued.clear();
      }
    }
    catch( InterruptedException e)
    {
      Thread.interrupted();
    }
    
    return true;
  }

  private static Log log = Log.getLog( BlockingDispatcher.class);
  
  private BlockingQueue<Runnable> queue;
  private List<Runnable> dequeued;
  private volatile boolean receiving;
  private volatile int shutdown;
}
