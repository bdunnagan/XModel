/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IFileAssociation.java
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
package org.xmodel.external.caching;

import java.io.InputStream;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;


/**
 * An interface for file extension handlers used by the FileSystemCachingPolicy. An extension handler determines
 * how the content of a file with a particular extension is applied to the file element in the model created by
 * the FileSystemCachingPolicy.
 */
public interface IFileAssociation
{
  /**
   * Returns the extensions handled by this association.
   * @return Returns the extensions handled by this association.
   */
  public String[] getExtensions();
  
  /**
   * Read the specified file content and apply it to the specified parent file element.
   * @param parent The parent file element (as defined by FileSystemCachingPolicy).
   * @param name The name of the image.
   * @param stream The input stream.
   */
  public void apply( IModelObject parent, String name, InputStream stream) throws CachingException;
}
