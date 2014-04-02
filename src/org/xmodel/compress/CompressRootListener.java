package org.xmodel.compress;

import org.xmodel.IModelObject;
import org.xmodel.ModelListener;

public class CompressRootListener extends ModelListener
{
  /* (non-Javadoc)
   * @see org.xmodel.ModelListener#notifyParent(org.xmodel.IModelObject, org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  @Override
  public void notifyParent( IModelObject child, IModelObject newParent, IModelObject oldParent)
  {
    // Insure that TabularCompressor knows which compressor to use by doing the following:
    
    // if newParent is not null and newParent does not have a caching policy
    // install ByteArrayStorageClass of child on parent
    // install ByteArrayCachingPolicy of child on parent, dirty=false
    
    // if newParent is null
    // remove ByteArrayStorageClass from oldParent
    // remove ByteArrayCachingPolicy from oldParent
  }
}
