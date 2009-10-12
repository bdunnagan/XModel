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
 * An IFileAssociation for the jar file <i>.jar</i> extension.
 */
public class JarAssociation implements IFileAssociation
{
  /* (non-Javadoc)
   * @see org.xmodel.external.caching.IFileAssociation#getAssociations()
   */
  public String[] getExtensions()
  {
    return extensions;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.caching.IFileAssociation#apply(org.xmodel.IModelObject, java.lang.String, java.io.InputStream)
   */
  @Override
  public void apply( IModelObject parent, String name, InputStream stream) throws CachingException
  {
  }

  private final static String[] extensions = { ".jar"};
}
