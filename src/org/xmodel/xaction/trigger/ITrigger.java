/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ITrigger.java
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
package org.xmodel.xaction.trigger;

import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;

/**
 * An interface for triggers which observe a model an execute a script autonomously 
 * when certain critera are met. 
 */
public interface ITrigger
{
  /**
   * Configure the trigger from the specified document.
   * @param document The document.
   */
  public void configure( XActionDocument document);
  
  /**
   * Returns the document for this trigger.
   * @return Returns the document for this trigger.
   */
  public XActionDocument getDocument();
  
  /**
   * Activate the trigger in the specified context.
   * @param context The context.
   */
  public void activate( IContext context);
  
  /**
   * Deactivate the trigger in the specified context.
   * @param context The context.
   */
  public void deactivate( IContext context);
}
