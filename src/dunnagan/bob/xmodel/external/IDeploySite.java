/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

/**
 * An interface for defining a deployment site which will notify listeners when files
 * are deployed at the site.  The site could be a URL or directory on the local filesystem.
 */
public interface IDeploySite
{
  /**
   * Add a listener to this site.
   * @param listener The listener to be added.
   */
  public void addListener( IDeployListener listener);

  /**
   * Remove a listener from this site.
   * @param listener The listener to be removed.
   */
  public void removeListener( IDeployListener listener);
}
