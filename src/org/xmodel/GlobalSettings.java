/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ModelRegistry.java
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Static global settings for the library including global access to an instance of IModel for handling
 * updates.  The default settings in this class provide conventions that allow the library to be used
 * without configuration.  However, each setting can be both globally and locally configured.  For example,
 * while the library will lazily create a global instance of ScheduledExecutorService if requested, instances
 * may be configured for specific purposes and at specific times.
 */
public class GlobalSettings
{
  /**
   * Sets the implementation of IModel for the current thread.
   * @param model The model.
   */
  public synchronized void setModel( IModel model)
  {
    threadModel.set( model);
  }
  
  /**
   * This method is identical to calling <code>getModel( true)</code>.
   * @return Returns the implementation of IModel for the current thread.
   */
  public synchronized IModel getModel()
  {
    return getModel( true);
  }
  
  /**
   * Get and/or create a IModel instance for the current thread.
   * @param create True if IModel instance should be created if it doesn't already exist.
   * @return Returns the IModel instance for the current thread.
   */
  public synchronized IModel getModel( boolean create)
  {
    IModel model = threadModel.get();
    if ( model == null && create)
    {
      model = new Model();
      threadModel.set( model);
    }
    return model;
  }

  /**
   * Set the global scheduler.
   * @param scheduler The scheduler.
   * @return Returns null or the previous scheduler.
   */
  public synchronized ScheduledExecutorService setScheduler( ScheduledExecutorService scheduler)
  {
    ScheduledExecutorService old = this.scheduler;
    this.scheduler = scheduler;
    return old;
  }
  
  /**
   * Returns the global scheduler, creating one if it does not already exist.
   * @return Returns the global scheduler.
   */
  public synchronized ScheduledExecutorService getScheduler()
  {
    if ( scheduler == null) scheduler = Executors.newScheduledThreadPool( 1, new ThreadFactory() {
      public Thread newThread( Runnable runnable)
      {
        Thread thread = new Thread( runnable, "Scheduler");
        thread.setDaemon( true);
        return thread;
      }
    });
    return scheduler;
  }
  
  /**
   * Returns the singleton.
   * @return Returns the singleton.
   */
  public static GlobalSettings getInstance()
  {
    return instance;
  }

  private static GlobalSettings instance = new GlobalSettings();
  
  private ThreadLocal<IModel> threadModel = new ThreadLocal<IModel>();
  private ScheduledExecutorService scheduler;
}