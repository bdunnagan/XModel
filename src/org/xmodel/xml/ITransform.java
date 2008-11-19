/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xml;

import org.xmodel.IModelObject;

/**
 * An interface for transforming IModelObject instances.
 */
public interface ITransform
{
  /**
   * Transform the subtree rooted on the input argument, find the difference of the result
   * with the output argument subtree and apply the difference change set to the output.
   * @param input The root of the subtree to be transformed.
   * @param output The root of the subtree which will be modified.
   */
  public void transform( IModelObject input, IModelObject output);

  /**
   * Performs the exact inverse transform of the <code>transform</code> method.
   * @param input The root of the subtree to be transformed.
   * @param output The root of the subtree which will be modified.
   */
  public void inverseTransform( IModelObject input, IModelObject output);
  
  /**
   * Transform the subtree rooted on the specified input object and return the root of the new subtree.
   * @param input The root of the subtree to be transformed.
   * @return Returns the root of the transformed input subtree.
   */
  public IModelObject transform( IModelObject input);
  
  /**
   * Performs the exact inverse transform of the <code>transform</code> method.
   * @param input The root of the subtree to be transformed.
   * @return Returns the root of the transformed input subtree.
   */
  public IModelObject inverseTransform( IModelObject input);
  
  /**
   * Returns true if this transform has an inverse transform.
   * @return Returns true if this transform has an inverse transform.
   */
  public boolean hasInverse();
}
