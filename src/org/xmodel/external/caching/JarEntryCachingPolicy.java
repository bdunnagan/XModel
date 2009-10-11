/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xml.XmlIO;

/**
 * A caching policy for loading a jar file entry.
 */
public class JarEntryCachingPolicy extends ConfiguredCachingPolicy
{
  public JarEntryCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  public JarEntryCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "entry"});
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    JarEntry thisEntry = (JarEntry)reference.getAttribute( "entry");
    if ( thisEntry == null) return;
    
    JarFile jarFile = getJarFile( reference);
    try
    {
      reference.removeChildren();
      InputStream stream = jarFile.getInputStream( thisEntry);
      XmlIO xmlIO = new XmlIO();
      IModelObject element = xmlIO.read( stream);
      reference.addChild( element);
      stream.close();
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to load jar entry: "+thisEntry, e);
    }
  }
  
  /**
   * Returns the JarFile stored on the root reference.
   * @param reference The reference being synced.
   * @return Returns the JarFile stored on the root reference.
   */
  private JarFile getJarFile( IExternalReference reference)
  {
    reference = getJarReference( reference);
    return (JarFile)reference.getAttribute( "jar");
  }
  
  /**
   * Returns the ancestor of the specified reference that has the JarCachingPolicy.
   * @param reference The starting point of the search.
   * @return Returns the ancestor of the specified reference that has the JarCachingPolicy.
   */
  private IExternalReference getJarReference( IExternalReference reference)
  {
    IModelObject ancestor = reference;
    while( ancestor != null)
    {
      if ( ancestor instanceof IExternalReference)
      {
        reference = (IExternalReference)ancestor;
        if ( reference.getCachingPolicy() instanceof JarCachingPolicy)
          return reference;
      }
      ancestor = ancestor.getParent();
    }
    return null;
  }
}
