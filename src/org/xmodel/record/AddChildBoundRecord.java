/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AddChildBoundRecord.java
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
package org.xmodel.record;

import org.xmodel.IBoundChangeRecord;
import org.xmodel.IChangeRecord;
import org.xmodel.INode;

/**
 * An implementation of IBoundChangeRecord for adding children.
 */
public class AddChildBoundRecord extends AbstractBoundRecord
{
  public AddChildBoundRecord( INode object, INode child)
  {
    super( object);
    this.child = child;
    this.index = -1;
  }
  
  public AddChildBoundRecord( INode object, INode child, int index)
  {
    super( object);
    this.child = child;
    this.index = index;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUndoRecord()
   */
  public IBoundChangeRecord createUndoRecord()
  {
    return new RemoveChildBoundRecord( object, child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return new AddChildRecord( getPath(), child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord(org.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( INode relative)
  {
    return new AddChildRecord( getRelativePath( relative), child, index);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getType()
   */
  public int getType()
  {
    return ADD_CHILD;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.record.AbstractChangeRecord#getChild()
   */
  @Override
  public INode getChild()
  {
    return child;
  }

  /* (non-Javadoc)
   * @see org.xmodel.record.AbstractChangeRecord#getIndex()
   */
  @Override
  public int getIndex()
  {
    return index;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#execute()
   */
  public void applyChange()
  {
    if ( index < 0)
      getBoundObject().addChild( child);
    else
      getBoundObject().addChild( child, index);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    INode parent = getBoundObject();
    return "add child: "+child+", index: "+index+", parent: "+parent;
  }
  
  INode child;
  int index;
}
