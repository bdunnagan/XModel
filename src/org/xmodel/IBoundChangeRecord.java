/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IBoundChangeRecord.java
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
 * An interface for change records to specific objects in the domain model. This interface also
 * provides a method for converting bound change records into unbound change records. There will be
 * some overhead in creating unbound records because the IPath has to be derived from the bound
 * IModelObject.
 */
public interface IBoundChangeRecord extends IChangeRecord
{
  /**
   * Creates an IBoundChangeRecord which will undo the result of executing this record.
   * @return Returns an IBoundChangeRecord which will undo this record.
   */
  public IBoundChangeRecord createUndoRecord();
  
  /**
   * Returns the domain object which is bound to this change record.
   * @return Returns the domain object bound to this change record.
   */
  public IModelObject getBoundObject();
  
  /**
   * Creates an unbound record from this change record.
   * @return Returns an unbound record for this bound record.
   */
  public IChangeRecord createUnboundRecord();
  
  /**
   * Creates an unbound record from this change record whose path is relative to the specified object.
   * @return Returns an unbound record for this bound record.
   */
  public IChangeRecord createUnboundRecord( IModelObject relative);
  
  /**
   * Apply this change record to the bound object.
   */
  public void applyChange();
}
