/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractTrigger.java
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

import org.xmodel.Xlate;
import org.xmodel.xaction.XActionDocument;

/**
 * An base implementation of the ITrigger interface.
 */
public abstract class AbstractTrigger implements ITrigger
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    this.document = document;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#getDocument()
   */
  public XActionDocument getDocument()
  {
    return document;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return Xlate.get( document.getRoot(), "name", (String)null);
  }
  
  protected XActionDocument document;
}
