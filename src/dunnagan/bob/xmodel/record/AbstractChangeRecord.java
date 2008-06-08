/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.record;

import dunnagan.bob.xmodel.IChangeRecord;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;

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
   * @see dunnagan.bob.xmodel.IChangeRecord#isType(int)
   */
  public boolean isType( int type)
  {
    return getType() == type;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getIdentityPath()
   */
  public IPath getPath()
  {
    return path;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getName()
   */
  public String getName()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getAttributeName()
   */
  public String getAttributeName()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getAttributeValue()
   */
  public Object getAttributeValue()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getChild()
   */
  public IModelObject getChild()
  {
    return null;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getIndex()
   */
  public int getIndex()
  {
    return -1;
  }
  
  IPath path;
}
