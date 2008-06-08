/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.record;

import dunnagan.bob.xmodel.*;

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
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#getBoundObject()
   */
  public IModelObject getBoundObject()
  {
    return object;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord()
   */
  public IChangeRecord createUnboundRecord()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IBoundChangeRecord#createUnboundRecord(dunnagan.bob.xmodel.IModelObject)
   */
  public IChangeRecord createUnboundRecord( IModelObject relative)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IChangeRecord#getIdentityPath()
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
   * @see dunnagan.bob.xmodel.IChangeRecord#applyChange(dunnagan.bob.xmodel.IModelObject)
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
