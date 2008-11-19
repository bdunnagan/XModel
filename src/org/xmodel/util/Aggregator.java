/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
