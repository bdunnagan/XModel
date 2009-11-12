/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractBoundRecord.java
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

import org.xmodel.*;

/**
 * An abstract base class for IBoundChangeRecord which provides some convenient
 * methods for implementors.  This class uses XPath for creating an identity path.
 */
public abstract class AbstractBoundRecord extends AbstractChangeRecord implements IBoundChangeRecord
{
  /**
   * Create an IBoundChangeRecord which is bound to the specified object.
   * @param object The bound object.
   */
  public AbstractBoundRecord( IModelObject object)
  {
    this.object = object;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#getBoundObject()
   */
  public IModelObject getBoundObject()
  {
    return object;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IBoundChangeRecord#createUnboundRecord(org.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#getIdentityPath()
   */
  public IPath getPath()
  {
    return ModelAlgorithms.createIdentityPath( object);
  }
  
  /**
   * Returns the relative path for the bound object relative to the specified object.
   * @param relative The end of the relative path.
   * @return Returns the relative path for the bound object relative to the specified object.
   */
  protected IPath getRelativePath( IModelObject relative)
  {
    IPath path = ModelAlgorithms.createRelativePath( relative, object);
    if ( path != null) return path;
    return ModelAlgorithms.createIdentityPath( object);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IChangeRecord#applyChange(org.xmodel.IModelObject)
   */
  public void applyChange( IModelObject root)
  {
    if ( path != null)
    {
      IModelObject boundObject = object;
      object = path.queryFirst( root);
      applyChange();
      object = boundObject;
    }
  }
  
  IModelObject object;
}
