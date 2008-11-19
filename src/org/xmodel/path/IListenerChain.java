/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.path;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.IPathListener;
import org.xmodel.xpath.expression.IContext;


/**
 * An interface for a chain of listeners used to implement the IPathListener mechanism.  A listener chain
 * consists of an ordered list of IListenerChainLink instances which correspond to the elements of an IPath.
 */
public interface IListenerChain
{
  /**
   * Returns the array of listeners (the chain).
   * @return Returns the array of listeners (the chain).
   */
  public IListenerChainLink[] getLinks();

  /**
   * Returns the IPath to which the IListenerChain belongs.
   * @return Returns the IPath to which the IListenerChain belongs.
   */
  public IPath getPath();

  /**
   * Returns the context of the path evaluation (the bound context).
   * @return Returns the context of the path evaluation.
   */
  public IContext getContext();

  /**
   * Returns the client IPathListener.
   * @return Returns the client IPathListener.
   */
  public IPathListener getPathListener();

  /**
   * Install the first link in the listener chain on the specified object.
   * @param object The object where the listener chain will be installed.
   */
  public void install( IModelObject object);
  
  /**
   * Uninstall the first link in the listener chain on the specified object.
   * @param object The object where the listener chain will be uninstalled.
   */
  public void uninstall( IModelObject object);
  
  /**
   * Print debug information about layer install.
   * @param list The layer on which the listener will be installed.
   * @param pathIndex The index of the layer.
   */
  public void debugInstall( List<IModelObject> list, int pathIndex);
  
  /**
   * Print debug information about layer uninstall.
   * @param list The layer on which the listener will be uninstalled.
   * @param pathIndex The index of the layer.
   */
  public void debugUninstall( List<IModelObject> list, int pathIndex);
  
  /**
   * Print debug information about incremental install.
   * @param list The objects where the link will be installed.
   * @param pathIndex The index of the layer.
   */
  public void debugIncrementalInstall( List<IModelObject> list, int pathIndex);
  
  /**
   * Print debug information about incremental uninstall.
   * @param list The objects where the link will be uninstalled.
   * @param pathIndex The index of the layer.
   */
  public void debugIncrementalUninstall( List<IModelObject> list, int pathIndex);
}
