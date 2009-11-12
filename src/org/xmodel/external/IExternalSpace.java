/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IExternalSpace.java
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
package org.xmodel.external;

import java.net.URI;
import java.util.List;
import org.xmodel.IModelObject;

/**
 * An interface for creating a data-model to represent a URI specification for a particular URI scheme.
 * This interface is used by the <code>fn:doc</code> function to resolve a URI.
 */
public interface IExternalSpace
{
  /**
   * Returns true if this space serves the scheme of the specified URI.
   * @param uri The URI.
   * @return Returns true if this space serves the scheme of the specified URI.
   */
  public boolean contains( URI uri);
  
  /**
   * Execute the specified URI query. The result will be an empty set for non-node-set queries.
   * @param uri The URI specification.
   * @return Returns the result-set of the query.
   */
  public List<IModelObject> query( URI uri) throws CachingException;
}
