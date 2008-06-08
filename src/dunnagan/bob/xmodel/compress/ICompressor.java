/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.compress;

import java.io.InputStream;
import java.io.OutputStream;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An interface for algorithms which compress an IModelObject tree into a byte sequence.
 */
public interface ICompressor
{
  /**
   * Compress the specified tree into a byte sequence.
   * @param element The root of the tree to be compressed.
   * @return Returns the byte array.
   */
  public byte[] compress( IModelObject element) throws CompressorException;
  
  /**
   * Decompress the specified byte sequence.
   * @param bytes A byte array.
   * @param offset The offset into the byte array.
   * @return Returns the decompressed element.
   */
  public IModelObject decompress( byte[] bytes, int offset) throws CompressorException;
  
  /**
   * Compress to the specified output stream.
   * @param element The element to compress.
   * @param stream The output stream.
   */
  public void compress( IModelObject element, OutputStream stream) throws CompressorException;
  
  /**
   * Decompress from the specified input stream.
   * @param stream The input stream.
   * @return Returns the decompressed element.
   */
  public IModelObject decompress( InputStream stream) throws CompressorException;
}
