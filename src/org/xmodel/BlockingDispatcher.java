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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.xmodel.log.SLog;


/**
 * An implementation of IDispatcher which allows runnables to be executed in the current thread.
 * This implementation assumes that the producer is executing in another thread.
 */
public class BlockingDispatcher implements IDispatcher
{
  public BlockingDispatcher()
  {
    queue = new ArrayBlockingQueue<Runnable>( 100);
    dequeued = new ArrayList<Runnable>();
    shutdown = new AtomicBoolean();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IDispatcher#execute(java.lang.Runnable)
   */
  public void execute( Runnable runnable)
  {
    SLog.verbosef( this, "Enqueue runnable: %s", runnable.getClass().getSimpleName());
    if ( !receiving) SLog.warn( this, "process() has not been called, yet.");
    try
    {
      if ( !shutdown.get()) queue.put( runnable);
    }
    catch( InterruptedException e)
    {
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#shutdown(boolean)
   */
  @Override
  public void shutdown( boolean immediate)
  {
    this.immediate = immediate;
    this.shutdown.set( true);
    
    queue.offer( new Runnable() {
      public void run()
      {
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
      SLog.verbose( this, "Waiting for dequeue ...");
      Runnable first = queue.poll( 5, TimeUnit.SECONDS);
      
      if ( shutdown.get() && immediate)
        return false;
      
      if ( first != null)
      {
        dequeued.add( first);
        queue.drainTo( dequeued);
        
        SLog.verbosef( this, "Dequeued %d runnables: ", dequeued.size());
        for( Runnable runnable: dequeued)
        {
          if ( runnable != null) 
          {
            SLog.verbosef( this, "Executing runnable: %s", runnable.getClass().getSimpleName());
            try
            {
              runnable.run();
            }
            catch( Exception e)
            {
              SLog.exception( this, e);
            }
            SLog.verbose( this, "Done.");
          }
        }
        
        dequeued.clear();
      }
      
      return true;
    }
    catch( InterruptedException e)
    {
      shutdown( true);
      return false;
    }
  }
  
  private BlockingQueue<Runnable> queue;
  private List<Runnable> dequeued;
  private AtomicBoolean shutdown;
  private boolean immediate;
  private boolean receiving;
}
