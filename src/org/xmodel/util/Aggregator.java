/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Aggregator.java
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
package org.xmodel.util;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IDispatcher;


/**
 * A class which can dispatch runnables at high frequency insuring that the runnable will run only once
 * within a certain predesignated time window. One use of this mechanism is for detecting changes made
 * to a large subtree.
 */
public class Aggregator
{
  /**
   * Create an aggregator with the specified window.
   * @param window The window in nanoseconds.
   */
  public Aggregator( long window)
  {
    winms = (int)(window % 1000000);
    winns = (int)(window / 1000000);
    entries = new ArrayList<Entry>();
  }

  /**
   * Add a runnable and return its index. This method may not be called after the aggregator is started.
   * @param runnable The runnable to be added.
   * @return Returns the index allocated for the runnable.
   */
  public int add( IDispatcher dispatcher, Runnable runnable)
  {
    int index = entries.size();
    Entry entry = new Entry();
    entry.runnable = runnable;
    entry.dispatcher = dispatcher;
    entries.add( entry);
    return index;
  }
  
  /**
   * Remove the specified runnable.
   * @param runnable The runnable.
   */
  public void remove( Runnable runnable)
  {
    for( int i=0; i<entries.size(); i++)
      if ( entries.get( i).runnable == runnable)
      {
        entries.remove( i);
        return;
      }
  }
  
  /**
   * Start the aggregator thread.
   */
  public void start()
  {
    exit = false;
    thread = new Thread( dispatcher, "Aggregator");
    thread.setDaemon( true);
    thread.start();
  }
  
  /**
   * Stop the aggregator.
   */
  public void stop()
  {
    if ( thread != null)
    {
      exit = true;
      thread.interrupt();
    }
  }
  
  /**
   * Dispatch the runnable with the specified index.
   * @param index The index of the runnable.
   */
  public synchronized void dispatch( int index)
  {
    entries.get( index).dispatch1 = true;
  }
  
  private final Runnable dispatcher = new Runnable() {
    public void run()
    {
      while( !exit)
      {
        synchronized( this)
        {
          for( Entry entry: entries)
          {
            if ( entry.dispatch1) 
            {
              entry.dispatch1 = false;
              entry.dispatch2 = true;
            }
          }          
        }
        
        for( Entry entry: entries)
        {
          if ( exit) break;
          if ( entry.dispatch2)
          {
            entry.dispatch2 = false;
            entry.dispatcher.execute( entry.runnable);
          }
        }
        
        try { Thread.sleep( winms, winns);} catch( InterruptedException e) {}
      }
    }
  };
  
  private class Entry
  {
    Runnable runnable;
    IDispatcher dispatcher;
    boolean dispatch1;
    boolean dispatch2;
  }

  private boolean exit;
  private Thread thread;
  private int winms;
  private int winns;
  private List<Entry> entries;
}
