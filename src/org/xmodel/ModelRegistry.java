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

/**
 * The registry where IModel instances are acquired. IModel instances are always associated 
 * with a particular thread. By default, IModel instances are created on demand and stored
 * in thread local data when the <code>getModel</code> method is called.  However, one 
 * instance of IModel may be associated with multiple threads by calling the <code>setModel</code>
 * method.
 */
public class ModelRegistry
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
   * Returns the singleton.
   * @return Returns the singleton.
   */
  public static ModelRegistry getInstance()
  {
    return instance;
  }

  private static ModelRegistry instance = new ModelRegistry();
  
  private ThreadLocal<IModel> threadModel = new ThreadLocal<IModel>();
}