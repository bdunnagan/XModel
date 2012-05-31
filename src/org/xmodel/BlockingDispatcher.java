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
import java.util.concurrent.atomic.AtomicBoolean;


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
  }

  /**
   * Dequeue and execute all the runnables on the queue.
   * @return Returns false if dispatcher has been shutdown.
   */
  public boolean process()
  {
    try
    {
      Runnable first = queue.take();
      
      if ( shutdown.get() && immediate)
        return false;
      
      dequeued.add( first);
      queue.drainTo( dequeued);
      
      for( Runnable runnable: dequeued)
        if ( runnable != null) 
          runnable.run();
      
      dequeued.clear();
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
}
