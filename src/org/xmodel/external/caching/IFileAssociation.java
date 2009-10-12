/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.InputStream;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;


/**
 * An interface for file extension handlers used by the FileSystemCachingPolicy. An extension handler determines
 * how the content of a file with a particular extension is applied to the file element in the model created by
 * the FileSystemCachingPolicy.
 */
public interface IFileAssociation
{
  /**
   * Returns the extensions handled by this association.
   * @return Returns the extensions handled by this association.
   */
  public String[] getExtensions();
  
  /**
   * Read the specified file content and apply it to the specified parent file element.
   * @param parent The parent file element (as defined by FileSystemCachingPolicy).
   * @param name The name of the image.
   * @param stream The input stream.
   */
  public void apply( IModelObject parent, String name, InputStream stream) throws CachingException;
}
