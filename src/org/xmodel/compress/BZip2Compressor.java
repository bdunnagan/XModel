/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.compress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;


/**
 * A compressor which serializes the XML to a string and then compresses the entire string using 
 * the BZIP2 algorithm. This technique is slow due to the cost of the initial serialization. The
 * serialization performed by the TabularCompressor is faster and smaller with BZIP2 compression.
 */
public class BZip2Compressor extends AbstractCompressor
{
  public BZip2Compressor()
  {
    xmlIO = new XmlIO();
    buffer = new byte[ 1024];
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.AbstractCompressor#setFactory(org.xmodel.IModelObjectFactory)
   */
  @Override
  public void setFactory( IModelObjectFactory factory)
  {
    super.setFactory( factory);
    xmlIO.setFactory( factory);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject, java.io.OutputStream)
   */
  public void compress( IModelObject element, OutputStream stream) throws CompressorException
  {
    try
    {
      String xml = xmlIO.write( element);
      CBZip2OutputStream zip = new CBZip2OutputStream( stream);
      zip.write( xml.getBytes( "UTF-8"));
      zip.close();
    }
    catch( IOException e)
    {
      throw new CompressorException( e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(byte[], int)
   */
  public IModelObject decompress( InputStream stream) throws CompressorException
  {
    try
    {
      CBZip2InputStream zip = new CBZip2InputStream( stream);
      StringBuilder builder = new StringBuilder();
      int nread = zip.read( buffer);
      while( nread >= 0)
      {
        builder.append( new String( buffer, 0, nread));
        nread = zip.read( buffer);
      }
      zip.close();
      return xmlIO.read( builder.toString());
    }
    catch( IOException e)
    {
      throw new CompressorException( "Error encountered during decompression: ", e);
    }
    catch( XmlException e)
    {
      throw new CompressorException( "Badly formatted decompressed xml: ", e);
    }
  }

  private XmlIO xmlIO;
  private byte[] buffer;
}
