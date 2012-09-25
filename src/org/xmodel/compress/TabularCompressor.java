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

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
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
  public TabularCompressor()
  {
    this( false);
  }
  
  /**
   * Create a TabularCompressor that optionally omits the tag table from the compressed output
   * when no new tags have been added since the previous call to the <code>compress</code>
   * method.
   * @param progressive True if tag table can be omitted.
   */
  public TabularCompressor( boolean progressive)
  {
    this.factory = new ModelObjectFactory();
    this.map = new LinkedHashMap<String, Integer>();
    this.table = new ArrayList<String>();
    this.predefined = false;
    this.progressive = progressive;
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
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject)
   */
  @Override
  public ChannelBuffer compress( IModelObject element) throws IOException
  {
    // content
    ChannelBuffer content = ChannelBuffers.dynamicBuffer( ByteOrder.BIG_ENDIAN, initialContentBufferSize);
    writeElement( content, element);
  
    // header (including table)
    ChannelBuffer header = ChannelBuffers.dynamicBuffer( ByteOrder.BIG_ENDIAN, initialHeaderBufferSize);
  
    // write header flags
    byte flags = 0;
    if ( predefined) flags |= 0x20;
    header.writeByte( flags);
    
    // write table if necessary
    if ( !predefined) writeTable( header);    
    
    // progressive compression assumes a send/receive pair of compressors to remember table entries
    if ( progressive) predefined = true;
    
    return ChannelBuffers.wrappedBuffer( header, content);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(org.jboss.netty.buffer.ChannelBuffer)
   */
  @Override
  public IModelObject decompress( ChannelBuffer input) throws IOException
  {
    // header flags
    byte flags = (byte)input.readByte();
    boolean predefined = (flags & 0x20) != 0;
    
    // table
    if ( !predefined) readTable( input);
        
    // content
    return readElement( input);
  }

  /**
   * Read an element from the input stream.
   * @param stream The input stream.
   * @return Returns the new element.
   */
  private IModelObject readElement( ChannelBuffer stream) throws IOException, CompressorException
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
  private void writeElement( ChannelBuffer stream, IModelObject element) throws IOException, CompressorException
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
  private void readAttributes( ChannelBuffer stream, IModelObject node) throws IOException, CompressorException
  {
    boolean useJavaSerialization = false;
    
    // read count
    int count = stream.readByte();
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
  private void readChildren( ChannelBuffer stream, IModelObject node) throws IOException, CompressorException
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
  private void writeAttributes( ChannelBuffer stream, IModelObject node) throws IOException, CompressorException
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
      stream.writeByte( count + 128);
      
      // write attributes
      for( String attrName: attrNames)
      {
        writeHash( stream, attrName);
        writeObject( stream, node.getAttributeNode( attrName));
      }
    }
    else
    {
      // write count
      stream.writeByte( count);
      
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
  private void writeChildren( ChannelBuffer stream, IModelObject node) throws IOException, CompressorException
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
  private String readHash( ChannelBuffer stream) throws IOException, CompressorException
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
  private void writeHash( ChannelBuffer stream, String name) throws IOException, CompressorException
  {
    Integer hash = map.get( name);
    if ( hash == null)
    {
      hash = hashIndex++;
      table.add( name);
      map.put( name, hash);
      if ( progressive) predefined = false;
    }
    writeValue( stream, hash);
  }
  
  /**
   * Read text from the specified stream.
   * @param stream The input stream.
   * @return Returns the text.
   */
  private String readText( ChannelBuffer stream) throws IOException
  {
    int length = readValue( stream);
    byte[] bytes = new byte[ length];
    stream.readBytes( bytes);
    return new String( bytes);
  }
  
  /**
   * Write text to the specified stream.
   * @param stream The output stream.
   * @param text The text.
   */
  private void writeText( ChannelBuffer stream, String text) throws CompressorException, IOException
  {
    writeValue( stream, text.length());
    stream.writeBytes( text.getBytes());
  }
  
  /**
   * Deserialize a Java Object from the stream.
   * @return Returns the object.
   */
  private Object readObject( ChannelBuffer stream) throws IOException, ClassNotFoundException
  {
    return serializer.readObject( stream);
  }
  
  /**
   * Serialize a Java Object to the stream.
   * @param stream The stream.
   * @param node The node containing the object.
   * @return Returns the number of bytes written.
   */
  private int writeObject( ChannelBuffer stream, IModelObject node) throws IOException
  {
    return serializer.writeObject( stream, node);
  }

  /**
   * Read the hash table from the stream.
   * @param stream The input stream.
   */
  private void readTable( ChannelBuffer stream) throws IOException, CompressorException
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
  private void writeTable( ChannelBuffer stream) throws IOException, CompressorException
  {
    // write table size
    Set<String> keys = map.keySet();
    writeValue( stream, keys.size());

    // write entries
    for( String key: keys) 
    {
      stream.writeBytes( key.getBytes());
      stream.writeByte( 0);
    }
  }
  
  /**
   * Read a value stored at the hash size.
   * @param stream The input stream.
   * @return Returns the value.
   */
  private int readValue( ChannelBuffer stream) throws IOException
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
  private void writeValue( ChannelBuffer stream, int value) throws IOException, CompressorException
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
    
  private final static int initialContentBufferSize = 256;
  private final static int initialHeaderBufferSize = 256;
  
  private List<String> table;
  private Map<String, Integer> map;
  private int hashIndex;
  private boolean predefined;
  private boolean progressive;
}
