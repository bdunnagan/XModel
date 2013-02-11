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

import java.util.concurrent.Executor;
import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.record.AddChildRecord;
import org.xmodel.record.ChangeAttributeRecord;
import org.xmodel.record.ClearAttributeRecord;
import org.xmodel.record.RemoveChildRecord;

/**
 * A listener that mirrors changes from a master element to a slave element using the slave element's dispatcher.
 * The slave element does not necessarily have to be a complete clone of the master element. However, the behavior
 * of this mechanism is undefined if changes are made to the master that cannot be applied to the slave.
 */
public class MasterSlaveListener extends NonSyncingListener
{
  public MasterSlaveListener( IModelObject master, IModelObject slave, Executor executor)
  {
    this.master = master;
    this.slave = slave;
    this.executor = executor;
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
  public synchronized void notifyAddChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyAddChild( parent, child, index);
    
    IChangeRecord record = new AddChildRecord( getPath( parent), child, index);
    if ( executor != null) executor.execute( new ApplyRecordRunnable( record)); else uninstall( master);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public synchronized void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
  {
    super.notifyRemoveChild( parent, child, index);
    
    IChangeRecord record = new RemoveChildRecord( getPath( parent), index);
    if ( executor != null) executor.execute( new ApplyRecordRunnable( record)); else uninstall( master);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public synchronized void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    super.notifyChange( object, attrName, newValue, oldValue);
    
    IChangeRecord record = new ChangeAttributeRecord( getPath( object), attrName, newValue);
    if ( executor != null) executor.execute( new ApplyRecordRunnable( record)); else uninstall( master);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public synchronized void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    super.notifyClear( object, attrName, oldValue);
    
    IChangeRecord record = new ClearAttributeRecord( getPath( object), attrName);
    if ( executor != null) executor.execute( new ApplyRecordRunnable( record)); else uninstall( master);
  }

  /**
   * Returns the relative path of the specified element.
   * @param element The element.
   * @return Returns the path of the specified element.
   */
  protected IPath getPath( IModelObject element)
  {
    return ModelAlgorithms.createRelativePath( master, element);
  }
  
  private class ApplyRecordRunnable implements Runnable
  {
    public ApplyRecordRunnable( IChangeRecord record)
    {
      this.record = record;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override 
    public void run()
    {
      record.applyChange( slave);
    }
    
    private IChangeRecord record;
  }
  
  private IModelObject master;
  private IModelObject slave;
  private Executor executor;
}
