/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TabularCompressor.java
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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;

/**
 * An implementation of ICompressor which creates a table of element tags so that the text of the
 * element tag can be replaced by the table index.
 * <p>
 * The tag table used by the compressor can be predefined. In this mode of operation the table is
 * not included in the output of the compressor and the same instance of the compressor will be able
 * to decompress the stream. Another instance of the compressor with the same predefined table will
 * also be able to decompress the stream. The table may be predefined manually or using a XSD or
 * simplified schema. In the latter case, the schema is traversed and indices are assigned to each
 * element in the schema. Therefore, two compressor instances which reference the same schema can
 * compress and decompress the stream without the need for the stream to contain the tag table.
 * <p>
 * Due to optimizations, elements may not contain more than 127 attributes.
 */
public class TabularCompressor extends AbstractCompressor
{
  /**
   * Create a TabularCompressor and use ZIP post compression. This provides a good tradeoff
   * between speed and size. For faster operation, turn post compression off. For somewhat
   * better compression, use BZIP2.
   */
  public TabularCompressor()
  {
    this.factory = new ModelObjectFactory();
    this.map = new LinkedHashMap<String, Integer>();
    this.table = new ArrayList<String>();
    this.predefined = false;
  }

  /**
   * Predefine the tag table. If the compressor does not encounter tags which are not defined
   * in the table then the table will not be written to the output. This is useful when all
   * the tags are known in advance.
   * @param table The table.
   */
  public void defineTagTable( List<String> table)
  {
    this.table = table;
    map.clear();
    for( int i=0; i<table.size(); i++) map.put( table.get( i), i);
    predefined = true;
  }
  
  /**
   * Clear the predefined tag table.
   */
  public void clearTagTable()
  {
    map.clear();
    table.clear();
    predefined = false;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject, java.io.OutputStream)
   */
  public void compress( IModelObject element, OutputStream finalArrayOut) throws CompressorException
  {
    ByteArrayOutputStream contentArrayOut = new ByteArrayOutputStream( 1024);
    DataOutputStream contentOut = new DataOutputStream( contentArrayOut);
    
    try
    {
      // create content and decide if compression is required
      writeElement( contentOut, element);
      
      // write header
      byte header = 0;
      if ( predefined) header |= 0x20;
      finalArrayOut.write( header);
      
      // optionally compress everything but header
      OutputStream rawOut = finalArrayOut;
      
      // write table
      if ( !predefined)
      {
        DataOutputStream tableOut = new DataOutputStream( rawOut);
        writeTable( tableOut);
        tableOut.flush();
      }
      
      // write content
      contentOut.flush();
      byte[] content = contentArrayOut.toByteArray();
      rawOut.write( content);
      rawOut.flush();
      rawOut.close();
    }
    catch( IOException e)
    {
      throw new CompressorException( e);
    }
    
    predefined = true;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(java.io.InputStream)
   */
  public IModelObject decompress( InputStream rawArrayIn) throws CompressorException
  {
    try
    {
      // read header
      byte header = (byte)rawArrayIn.read();
      boolean predefined = (header & 0x20) != 0;
      
      if ( (header & 0xC0) != 0) throw new CompressorException( "Post compression not supported!");

      // optionally decompress everything but header
      InputStream rawIn = rawArrayIn;
      DataInputStream dataIn = new DataInputStream( rawIn);
      
      // read table
      if ( !predefined) readTable( dataIn);
      
      // read content
      IModelObject element = readElement( dataIn);
      dataIn.close();
      
      return element;
    }
    catch( IOException e)
    {
      throw new CompressorException( "Error in data stream: ", e);
    }
  }

  /**
   * Read an element from the input stream.
   * @param stream The input stream.
   * @return Returns the new element.
   */
  private IModelObject readElement( DataInputStream stream) throws IOException, CompressorException
  {
    // read tag name
    String type = readHash( stream);
    
    // create element
    IModelObject element = factory.createObject( null, type);
    readAttributes( stream, element);
    readChildren( stream, element);
    
    return element;
  }
  
  /**
   * Write an element to the output stream.
   * @param stream The output stream.
   * @param element The element.
   */
  private void writeElement( DataOutputStream stream, IModelObject element) throws IOException, CompressorException
  {
    // write tag name
    writeHash( stream, element.getType());
    
    // write attributes and children
    writeAttributes( stream, element);
    writeChildren( stream, element);
  }
  
  /**
   * Read the attributes in the specified buffer and populate the node.
   * @param stream The input stream.
   * @param node The node whose attributes are being read.
   */
  private void readAttributes( DataInputStream stream, IModelObject node) throws IOException, CompressorException
  {
    boolean useJavaSerialization = false;
    
    // read count
    int count = stream.read();
    if ( count > 127)
    {
      count -= 128;
      useJavaSerialization = true;
    }

    // read attributes
    if ( useJavaSerialization)
    {
      for( int i=0; i<count; i++)
      {
        String attrName = readHash( stream);
        try
        {
          Object attrValue = readObject( stream);
          node.setAttribute( attrName, attrValue);
        }
        catch( ClassNotFoundException e)
        {
          throw new CompressorException( String.format(
            "Unable to deserialize attribute, %s.", attrName), e);
        }
      }
    }
    else
    {
      for( int i=0; i<count; i++)
      {
        String attrName = readHash( stream);
        String attrValue = readText( stream);
        node.setAttribute( attrName, attrValue);
      }
    }
  }
  
  /**
   * Read the children in the specified buffer and populate the node.
   * @param stream The input stream.
   * @param node The node whose children are being read.
   */
  private void readChildren( DataInputStream stream, IModelObject node) throws IOException, CompressorException
  {
    // read count
    int count = readValue( stream);
    
    // read children
    for( int i=0; i<count; i++)
    {
      IModelObject child = readElement( stream);
      node.addChild( child);
    }
  }

  /**
   * Write the attributes of the specified node into the buffer at the given offset.
   * @param stream The output stream.
   * @param node The node whose attributes are to be written.
   */
  private void writeAttributes( DataOutputStream stream, IModelObject node) throws IOException, CompressorException
  {
    Collection<String> attrNames = node.getAttributeNames();
    boolean useJavaSerialization = false;
    int count = attrNames.size();
    
    if ( count > 127)
    {
      IPath path = ModelAlgorithms.createIdentityPath( node);
      throw new CompressorException( String.format(
        "Element has more than 127 attributes, %s.", path.toString()));
    }
    
    for( String attrName: attrNames)
    {
      Object attrValue = node.getAttribute( attrName);
      if ( !(attrValue instanceof CharSequence))
      {
        useJavaSerialization = true;
        break;
      }
    }
      
    if ( useJavaSerialization)
    {
      // write count
      stream.write( count + 128);
      
      // write attributes
      for( String attrName: attrNames)
      {
        writeHash( stream, attrName);
        writeObject( stream, node.getAttribute( attrName));
      }
    }
    else
    {
      // write count
      stream.write( count);
      
      // write attributes
      for( String attrName: attrNames)
      {
        writeHash( stream, attrName);
        writeText( stream, Xlate.get( node, attrName, ""));
      }
    }
  }
  
  /**
   * Write the children of the specified node into the buffer at the given offset.
   * @param stream The output stream.
   * @param node The node whose children are to be written.
   */
  private void writeChildren( DataOutputStream stream, IModelObject node) throws IOException, CompressorException
  {
    // write count
    List<IModelObject> children = node.getChildren();
    writeValue( stream, children.size());
    
    // write children
    for( IModelObject child: children)
      writeElement( stream, child);
  }
  
  /**
   * Read a hashed name from the stream.
   * @param stream The input stream.
   * @return Returns the name.
   */
  private String readHash( DataInputStream stream) throws IOException, CompressorException
  {
    int index = readValue( stream);
    if ( index >= table.size()) throw new CompressorException( "Table entry not found.");
    return table.get( index);
  }
  
  /**
   * Write a hashed name to the stream.
   * @param stream The output stream.
   * @param name The name.
   */
  private void writeHash( DataOutputStream stream, String name) throws IOException, CompressorException
  {
    Integer hash = map.get( name);
    if ( hash == null)
    {
      hash = hashIndex++;
      table.add( name);
      map.put( name, hash);
      predefined = false;
    }
    writeValue( stream, hash);
  }
  
  /**
   * Read text from the specified stream.
   * @param stream The input stream.
   * @return Returns the text.
   */
  private String readText( DataInputStream stream) throws IOException
  {
    int length = readValue( stream);
    byte[] bytes = new byte[ length];
    stream.readFully( bytes);
    return new String( bytes);
  }
  
  /**
   * Write text to the specified stream.
   * @param stream The output stream.
   * @param text The text.
   */
  private void writeText( DataOutputStream stream, String text) throws CompressorException, IOException
  {
    writeValue( stream, text.length());
    stream.writeBytes( text);
  }
  
  /**
   * Deserialize a Java Object from the stream.
   * @return Returns the object.
   */
  private Object readObject( DataInputStream stream) throws IOException, ClassNotFoundException
  {
    return serializer.readObject( stream);
  }
  
  /**
   * Serialize a Java Object to the stream.
   * @param stream The stream.
   * @param object The object.
   * @return Returns the number of bytes written.
   */
  private int writeObject( DataOutputStream stream, Object object) throws IOException
  {
    return serializer.writeObject( stream, object);
  }

  /**
   * Read the hash table from the stream.
   * @param stream The input stream.
   */
  private void readTable( DataInputStream stream) throws IOException, CompressorException
  {
    table = new ArrayList<String>();
    
    // read table size
    int count = readValue( stream);
    
    // read entries
    StringBuilder builder = new StringBuilder();
    for( int i=0; i<count; i++)
    {
      builder.setLength( 0);
      for( byte b = stream.readByte(); b != 0; b = stream.readByte())
      {
        builder.append( (char)b);
      }
      
      table.add( builder.toString());
    }
  }
  
  /**
   * Write the hash table to the stream.
   * @param stream The output stream.
   */
  private void writeTable( DataOutputStream stream) throws IOException, CompressorException
  {
    // write table size
    Set<String> keys = map.keySet();
    writeValue( stream, keys.size());

    // write entries
    for( String key: keys) 
    {
      stream.writeBytes( key);
      stream.write( 0);
    }
  }
  
  /**
   * Read a value stored at the hash size.
   * @param stream The input stream.
   * @return Returns the value.
   */
  private int readValue( DataInputStream stream) throws IOException
  {
    int b1 = stream.readUnsignedByte();
    if ( (b1 & 0x80) != 0)
    {
      b1 &= 0x7f;
      int b2 = stream.readUnsignedByte();
      int b3 = stream.readUnsignedByte();
      int b4 = stream.readUnsignedByte();
      return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
    else
    {
      return b1;
    }
  }
  
  /**
   * Write a value at the hash size.
   * @param stream The output stream.
   * @param value The value.
   */
  private void writeValue( DataOutputStream stream, int value) throws IOException, CompressorException
  {
    if ( value > 127)
    {
      value |= 0x80000000;
      stream.writeInt( value);
    }
    else
    {
      stream.writeByte( value);
    }
  }
    
  private List<String> table;
  private Map<String, Integer> map;
  private int hashIndex;
  private boolean predefined;
}
