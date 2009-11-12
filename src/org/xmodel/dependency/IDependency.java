/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IDependency.java
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
package org.xmodel.dependency;

/**
 * An interface which defines a dependency between objects.
 */
public interface IDependency
{
  /**
   * Returns true if the dependent argument is subordinate to the target argument.
   * @param target The target object.
   * @param dependent The (possibly) dependent object.
   * @return Returns true if the dependent argument is subordinate to the target argument.
   */
  public boolean evaluate( Object target, Object dependent);
}
