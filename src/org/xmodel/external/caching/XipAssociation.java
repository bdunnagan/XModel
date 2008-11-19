/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
   * @see org.xmodel.external.caching.IFileAssociation#apply(org.xmodel.IModelObject, java.io.File)
   */
  public void apply( IModelObject parent, File file) throws CachingException
  {
    try
    {
      TabularCompressor compressor = new TabularCompressor();
      IModelObject content = compressor.decompress( new BufferedInputStream( new FileInputStream( file)));
      parent.addChild( content);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to parse xml in compressed file: "+file, e);
    }
  }
  
  private final static String[] extensions = { ".xip"};
}
