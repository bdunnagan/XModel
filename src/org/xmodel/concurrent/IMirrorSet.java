package org.xmodel.concurrent;

import org.xmodel.IModelObject;

/**
 * An interface for configuring a pair of IModelObject instances belonging to
 * different threads which mirror one another using some synchronization mechanism.
 */
public interface IMirrorSet
{
  /**
   * Get and/or create a copy of the master element for the current thread.
   * @param create True if the element should be created if it does not exist.
   * @return Returns null or the element for the current thread.
   */
  public IModelObject get( boolean create);
  
  /**
   * Start mirroring changes between mirrored element.
   */
  public void attach();
  
  /**
   * Stop mirroring changes between mirrored elements. This method must be 
   * called in order for this object to be garbage collected.
   */
  public void detach();
}
