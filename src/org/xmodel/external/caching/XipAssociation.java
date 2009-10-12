/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.InputStream;

import org.xmodel.IModelObject;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.external.CachingException;

/**
 * An IFileAssociation for the XModel <i>.xip</i> extension associated with the TabularCompressor.
 */
public class XipAssociation implements IFileAssociation
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
  public void apply( IModelObject parent, String name, InputStream stream) throws CachingException
  {
    try
    {
      TabularCompressor compressor = new TabularCompressor();
      IModelObject content = compressor.decompress( stream);
      parent.addChild( content);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to parse xml in compressed file: "+name, e);
    }
  }
  
  private final static String[] extensions = { ".xip"};
}
