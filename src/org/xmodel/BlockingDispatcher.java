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
    processing = false;
    shutdown = 0;
    thread = Thread.currentThread();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IDispatcher#execute(java.lang.Runnable)
   */
  public void execute( Runnable runnable)
  {
    log.verbosef( "[%s] Enqueue runnable: %s", thread.getName(), runnable);
    if ( !processing) log.warn( "process() has not been called, yet.");
    try
    {
      if ( shutdown == 0) queue.put( runnable);
    }
    catch( InterruptedException e)
    {
      log.warnf( "[%s] Enqueue was interrupted for runnable, %s", thread.getName(), runnable);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    if ( watchdog != null)
    {
      watchdog.interrupt();
      watchdog = null;
    }
    
    shutdown = immediate? 2: 1;
    
    queue.offer( new Runnable() {
      public void run()
      {
        log.warnf( "[%s] BlockingDispatcher has been shutdown.", thread.getName());
      }
    });
  }

  /**
   * Dequeue and execute all the runnables on the queue.
   * @return Returns false if dispatcher has been shutdown.
   */
  public boolean process()
  {
    processing = true;
    
    if ( watchdog == null)
    {
      watchdog = new Watchdog( watchdogTimeout * 1000);
      watchdog.start();
    }
    
    try
    {
      if ( shutdown > 0) return false;
      
      log.verbosef( "[%s] Waiting for dequeue ...", thread.getName());
      Runnable first = queue.poll( petTimeout, TimeUnit.SECONDS);
      if ( watchdog != null) watchdog.pet();
      
      if ( shutdown == 2) return false;
      
      if ( first != null)
      {
        dequeued.add( first);
        queue.drainTo( dequeued);
        
        log.verbosef( "[%s] Dequeued %d runnables: ", thread.getName(), dequeued.size());
        for( Runnable runnable: dequeued)
        {
          if ( runnable != null) 
          {
            log.verbosef( "[%s] Executing runnable: %s", thread.getName(), runnable);
            runnable.run();
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

  private static int watchdogTimeout = 5 * 60;
  private static int petTimeout = 3 * 60;
  
  private static Log log = Log.getLog( BlockingDispatcher.class);
  
  private BlockingQueue<Runnable> queue;
  private List<Runnable> dequeued;
  private volatile boolean processing;
  private volatile int shutdown;
  private Watchdog watchdog;
  private Thread thread;
}
