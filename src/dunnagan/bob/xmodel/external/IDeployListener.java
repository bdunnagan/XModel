/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import java.io.File;

/**
 * An interface for receiving notifications of the addition and removal of files
 * from one or more deployment sites (IDeploySite).
 */
public interface IDeployListener
{
  /**
   * Called when the given file is added to the given site.
   * @param site The site where the file was added.
   * @param file The file that was added.
   */
  public void notifyAddFile( IDeploySite site, File file);

  /**
   * Called when the given file is modified at the given site.
   * @param site The site where the file was modified.
   * @param file The file that was modified.
   */
  public void notifyModifyFile( IDeploySite site, File file);
  
  /**
   * Called when the given file is removed from the given site.
   * @param site The site where the file was removed.
   * @param file The file that was removed.
   */
  public void notifyRemoveFile( IDeploySite site, File file);
}
