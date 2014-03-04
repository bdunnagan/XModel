/*
 * Xidget - XML Widgets based on JAHM
 * 
 * IFeatured.java
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

/**
 * An interface for classes which support the feature semantic. This is really
 * a specialized implementation of the adapter pattern, so I'm calling these
 * adapters <i>features</i> instead.
 */
public interface IFeatured
{
  /**
   * Returns null or an instance of the specified feature.
   * @param clss The interface class.
   * @return Returns null or an instance of the specified feature.
   */
  public <T> T getFeature( Class<T> clss);  
}
