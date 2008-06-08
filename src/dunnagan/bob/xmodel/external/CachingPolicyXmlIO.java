/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import java.io.InputStream;
import java.net.URL;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.caching.AnnotationTransform;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;

/**
 * An extension of XmlIO which preprocesses the data and extracts external reference metadata
 * annotations as defined in metadata.xsd. ExternalReference instances are created with 
 * ConfiguredCachingPolicy instances as defined by the metadata annotations according to the
 * metadata.xsd schema. The annotations are removed from the model.
 */
public class CachingPolicyXmlIO extends XmlIO
{
  public CachingPolicyXmlIO()
  {
    this( CachingPolicyXmlIO.class.getClassLoader());
  }
  
  /**
   * Construct with the specified ClassLoader for resolving ICachingPolicy class names.
   * @param loader The class loader.
   */
  public CachingPolicyXmlIO( ClassLoader loader)
  {
    transform = new AnnotationTransform();
    transform.setClassLoader( loader);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.XmlIO#read(java.io.InputStream)
   */
  @Override
  public IModelObject read( InputStream stream) throws XmlException
  {
    IModelObject raw = super.read( stream);
    try
    {
      return transform.transform( raw);
    } 
    catch( Exception e)
    {
      throw new XmlException( "Unable to preprocess xml.", e);
    }    
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xml.XmlIO#read(java.net.URL)
   */
  @Override
  public IModelObject read( URL url) throws XmlException
  {
    IModelObject raw = super.read( url);
    try
    {
      return transform.transform( raw);
    } 
    catch( Exception e)
    {
      throw new XmlException( "Unable to preprocess xml.", e);
    }    
  }
  
  private AnnotationTransform transform;
}
