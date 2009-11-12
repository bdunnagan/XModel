/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractChangeRecord.java
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

import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;
import org.xmodel.IPath;

/**
 * An abstract base class for IChangeRecord which provides some convenient
 * methods for implementors.
 */
public abstract class AbstractChangeRecord implements IChangeRecord
{
  /**
   * Create a change record which does not have an path. It is assumed that the subclass will
   * provide an implementation of getPath().
   */
  protected AbstractChangeRecord()
  {
  }
  
  /**
   * Create a change record with the specified path.
   * @param path The path where the change will be applied.
   */
  public AbstractChangeRecord( IPath path)
  {
    this.path = path;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#isType(int)
   */
  public boolean isType( int type)
  {
    return getType() == type;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getIdentityPath()
   */
  public IPath getPath()
  {
    return path;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getName()
   */
  public String getName()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getAttributeName()
   */
  public String getAttributeName()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getAttributeValue()
   */
  public Object getAttributeValue()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getChild()
   */
  public IModelObject getChild()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getIndex()
   */
  public int getIndex()
  {
    return -1;
  }
  
  IPath path;
}
