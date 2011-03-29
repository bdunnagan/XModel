/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * UndoListener.java
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
package org.xmodel.concurrent;

import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelListener;
import org.xmodel.record.AddChildRecord;
import org.xmodel.record.ChangeAttributeRecord;
import org.xmodel.record.ClearAttributeRecord;
import org.xmodel.record.RemoveChildRecord;

/**
 * An IModelListener that sends unbound change records to a QueueMirrorSet.
 */
class ChangeSetListener extends ModelListener
{
  public ChangeSetListener( IModelObject root, QueueMirrorSet mirror)
  {
    this.root = root;
    this.mirror = mirror;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    IChangeRecord record = new AddChildRecord( getPath( parent), child, index);
    mirror.post( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    IChangeRecord record = new RemoveChildRecord( getPath( parent), index);
    mirror.post( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    IChangeRecord record = new ChangeAttributeRecord( getPath( object), attrName, newValue);
    mirror.post( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    IChangeRecord record = new ClearAttributeRecord( getPath( object), attrName);
    mirror.post( record);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
   */
  public void notifyDirty( IModelObject object, boolean dirty)
  {
  }
  
  /**
   * Returns the relative path of the specified element.
   * @param element The element.
   * @return Returns the path of the specified element.
   */
  protected IPath getPath( IModelObject element)
  {
    return ModelAlgorithms.createRelativePath( root, element);
  }
  
  private IModelObject root;
  private QueueMirrorSet mirror;
}
