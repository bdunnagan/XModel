/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IDispatcher.java
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
 * An interface which the server uses to pass queries to the xmodel thread.
 */
public interface IDispatcher
{
  /**
   * Execute the specified runnable in the xmodel thread.
   * @param runnable The runnable.
   */
  public void execute( Runnable runnable);
  
  /**
   * Instruct the dispatcher to stop executing new tasks. If the immediate flag is false then any queued tasks 
   * will be executed before the dispatcher terminates. Otherwise, the dispatcher will terminate after the currently
   * executing runnable(s) have completed. Some implementations may attempt to stop currently executing tasks by
   * interrupting the thread when the immediate flag is true.
   */
  public void shutdown( boolean immediate);
}
