/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
