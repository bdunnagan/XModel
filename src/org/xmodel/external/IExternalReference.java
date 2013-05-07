/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IExternalReference.java
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

import org.xmodel.IModelObject;

/**
 * An interface for the root of a subtree which is backed by an external storage location.
 * Synchronization of the subtree with the external content is controlled by an instance of
 * ICachingPolicy. IExternalReferences are associated with an ICache which, in conjunction with the
 * ICachingPolicy, manages the amount of data cached in memory. IExternalReferences are initially
 * dirty when constructed.
 */
public interface IExternalReference extends IModelObject
{
}
