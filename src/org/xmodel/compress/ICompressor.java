/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ICompressor.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.compress;

import java.io.InputStream;
import java.io.OutputStream;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;

/**
 * An interface for algorithms which compress an IModelObject tree into a byte sequence.
 */
public interface ICompressor
{
  /**
   * Set the factory used to create decompressed objects.
   * @param factory The factory.
   */
  public void setFactory( IModelObjectFactory factory);

  /**
   * Set the instance of ISerializer for serializing attribute values.
   * @param serializer The serializer.
   */
  public void setSerializer( ISerializer serializer);
  
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
