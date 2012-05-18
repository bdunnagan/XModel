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
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IDispatcher#execute(java.lang.Runnable)
   */
  public void execute( Runnable runnable)
  {
    try
    {
      queue.put( runnable);
    }
    catch( InterruptedException e)
    {
    }
  }
  
  /**
   * Dequeue and execute all the runnables on the queue.
   */
  public void process()
  {
    try
    {
      Runnable first = queue.take();
      
      dequeued.add( first);
      queue.drainTo( dequeued);
      
      for( Runnable runnable: dequeued)
        if ( runnable != null) 
          runnable.run();
      
      dequeued.clear();
    }
    catch( InterruptedException e)
    {
    }
  }
  
  private BlockingQueue<Runnable> queue;
  private List<Runnable> dequeued;
}
