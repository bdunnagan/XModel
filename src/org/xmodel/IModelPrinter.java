/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An interface for rendering a model whole or in part to an OutputStream.
 */
public interface IModelPrinter
{
  /**
   * Render the subtree rooted at the specified node.
   * @param object The root of the subtree to render.
   * @return Returns a string containing the rendered tree.
   */
  public String renderTree( IModelObject object);
  
  /**
   * Render the specified node.
   * @param object The node to render.
   * @return Returns a string containing the rendered node.
   */
  public String renderObject( IModelObject object);

  /**
   * Render the subtree rooted at the specified node to the specified OutputStream.
   * @param object The root of the subtree to render.
   */
  public void printTree( OutputStream stream, IModelObject object) throws IOException;

  /**
   * Render the specified node to the specified OutputStream.
   * @param object The node to render.
   */
  public void printObject( OutputStream stream, IModelObject object) throws IOException;
}
