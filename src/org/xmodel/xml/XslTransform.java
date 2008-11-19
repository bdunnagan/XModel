/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xml;

import java.io.IOException;
import java.net.URL;

import org.jdom.Document;
import org.jdom.transform.XSLTransformException;
import org.jdom.transform.XSLTransformer;
import org.xmodel.IModelObject;
import org.xmodel.diff.IXmlDiffer;
import org.xmodel.diff.XmlDiffer;


/**
 * An implementation of ITransform which uses the JDOM XSL transformation class. The transform
 * is cached in memory to improve performance.  The transform can be discarded after each use
 * to free memory.
 * <p>
 * It is usually a good idea to ensure that the XSL transform preserves the IModelObject name
 * when the object identity in the transformed tree is unchanged.  This makes it easy for the
 * DifferenceEngine object to quickly figure out what was changed by the transform.
 * @deprecated
 */
public class XslTransform implements ITransform
{
  /**
   * Create a one-way transform using the specified XSL stylesheet.
   * @param xslURL The file containing the XSL stylesheet.
   */
  public XslTransform( URL xslURL) throws XSLTransformException
  {
    this( new XmlConverter(), xslURL, null);
  }

  /**
   * Create a bi-directional transform using the specified XSL stylesheets. Obviously, no test
   * is performed to ensure that the transforms are, in fact, inverses of each other. However,
   * it is an error if the transforms are not exact inverses.
   * @param xslURL1 The file containing the first transform.
   * @param xslURL2 The file containing the inverse of the first transform.
   */
  public XslTransform( URL xslURL1, URL xslURL2) throws XSLTransformException
  {
    this( new XmlConverter(), xslURL1, xslURL2);
  }
  
  /**
   * Create a one-way transform using the specified XSL stylesheet.
   * @param converter The converter which converts between JDOM and Xmodel.
   * @param xslURL The file containing the XSL stylesheet.
   */
  public XslTransform( IXmlConverter converter, URL xslURL) throws XSLTransformException
  {
    this( converter, xslURL, null);
  }

  /**
   * Create a bi-directional transform using the specified XSL stylesheets. Obviously, no test
   * is performed to ensure that the transforms are, in fact, inverses of each other. However,
   * it is an error if the transforms are not exact inverses.
   * @param converter The converter which converts between JDOM and Xmodel.
   * @param xslURL1 The file containing the first transform.
   * @param xslURL2 The file containing the inverse of the first transform.
   */
  public XslTransform( IXmlConverter converter, URL xslURL1, URL xslURL2) throws XSLTransformException
  {
    this.converter = converter;
    this.differ = new XmlDiffer();
  	try
  	{
  		transform = new XSLTransformer( xslURL1.openStream());
  		if ( xslURL2 != null) inverseTransform = new XSLTransformer( xslURL2.openStream());
  	}
  	catch (IOException e) 
    {
  		System.out.println(e.getMessage());
  	}
  }

  /**
   * Set the DifferenceEngine which is used to find the difference between the input subtree and
   * the transformed subtree.  The DifferenceEngine returns the IChangeSet.
   * @param differenceEngine The new DifferenceEngine to use.
   */
  public void setDiffer( IXmlDiffer differ)
  {
    this.differ = differ;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.ITransform#transform(
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void transform( IModelObject input, IModelObject output)
  {
    IModelObject newOutput = transform( input);
    differ.diffAndApply( output, newOutput);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.ITransform#inverseTransform(
   * org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public void inverseTransform( IModelObject input, IModelObject output)
  {
    IModelObject newOutput = inverseTransform( input);
    differ.diffAndApply( output, newOutput);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.ITransform#transform(org.xmodel.IModelObject)
   */
  public IModelObject transform( IModelObject input)
  {
    Document document = converter.convert( input);
    try
    {
      document = transform.transform( document);
      return converter.convert( document);
    }
    catch( XSLTransformException e)
    {
      System.err.println( "Failed XSL transform: ");
      e.printStackTrace( System.err);
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.ITransform#inverseTransform(org.xmodel.IModelObject)
   */
  public IModelObject inverseTransform( IModelObject input)
  {
    Document document = converter.convert( input);
    try
    {
      if ( hasInverse())
      {
        document = inverseTransform.transform( document);
        return converter.convert( document);
      }
    }
    catch( XSLTransformException e)
    {
      System.err.println( "Failed XSL transform: ");
      e.printStackTrace( System.err);
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.ITransform#hasInverse()
   */
  public boolean hasInverse()
  {
    return (inverseTransform != null);
  }

  IXmlConverter converter;
  XSLTransformer transform;
  XSLTransformer inverseTransform;
  IXmlDiffer differ;
}
