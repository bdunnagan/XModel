/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;

/**
 * An IFileAssociation for various text file associations including <i>.txt</i> and associations for various 
 * programming language files such as html, java, perl and python.
 */
public class TxtAssociation implements IFileAssociation
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
      char[] buffer = new char[ 1 << 16];
      StringBuilder content = new StringBuilder();
      BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
      while( reader.ready())
      {
        int count = reader.read( buffer);
        if ( count > 0) content.append( content, 0, count);
      }
      parent.setValue( content.toString());
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable read text file: "+name, e);
    }
  }
  
  private final static String[] extensions = { 
    ".txt", ".css", ".html", ".htm", ".java", ".rtf", ".pl", ".py"};
}
