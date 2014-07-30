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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * Static global settings for the library including global access to an instance of IModel for handling
 * updates.  The default settings in this class provide conventions that allow the library to be used
 * without configuration.  However, each setting can be both globally and locally configured.  For example,
 * while the library will lazily create a global instance of ScheduledExecutorService if requested, instances
 * may be configured for specific purposes and at specific times.
 */
public class GlobalSettings
{
  public GlobalSettings()
  {
    contexts = new ConcurrentHashMap<String, IContext>();
    executor = new CurrentThreadExecutor();
  }
  
  /**
   * @return Returns the IModel instance for the current thread.
   */
  public IModel getModel()
  {
    IModel model = threadModel.get();
    if ( model == null)
    {
      model = new Model();
      threadModel.set( model);
    }
    return model;
  }

  /**
   * Get/create the context with the specified name.
   * @param parent The parent context.
   * @param name The name.
   * @return Returns the context associated with the specified name.
   */
  public IContext getNamedContext( IContext parent, String name)
  {
    IContext context = contexts.get( name);
    if ( context != null) return context;
    
    context = new StatefulContext( 
        parent,
        parent.getObject(),
        parent.getPosition(),
        parent.getSize());
    
    contexts.putIfAbsent( name, context);
    
    return contexts.get( name);
  }

  /**
   * Delete the context with the specified name.
   * @param name The name.
   */
  public void deleteContext( String name)
  {
    contexts.remove( name);
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
   * Set the default executor (see org.xodel.xpath.expression.IContext).
   * @param executor The executor.
   */
  public synchronized void setDefaultExecutor( Executor executor)
  {
    this.executor = executor;
  }
  
  /**
   * @return Returns the default executor.
   */
  public synchronized Executor getDefaultExecutor()
  {
    return executor;
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
  private ConcurrentHashMap<String, IContext> contexts;
  private ScheduledExecutorService scheduler;
  private Executor executor;
}