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
import java.nio.charset.Charset;
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
import org.xmodel.external.ICachingPolicy;
import org.xmodel.log.Log;
import org.xmodel.storage.ByteArrayStorageClass;
import org.xmodel.storage.IStorageClass;
import org.xmodel.util.ByteCounterInputStream;
import org.xmodel.util.HexDump;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

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
    this( false, true);
  }

  public TabularCompressor( boolean stateful)
  {
    this( stateful, true);
  }

  /**
   * Create a TabularCompressor that optionally omits the tag table from the compressed output
   * when no new tags have been added since the previous call to the <code>compress</code>
   * method.
   * @param stateful True if tag table can be omitted.
   * @param shallow True if shallow deserialization should be performed.
   */
  public TabularCompressor( boolean stateful, boolean shallow)
  {
    this.factory = new ModelObjectFactory();
    this.map = new LinkedHashMap<String, Integer>();
    this.table = new ArrayList<String>();
    this.predefined = false;
    this.stateful = stateful;
    this.shallow = shallow;
    this.charset = Charset.forName( "UTF-8");
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
   * @return Returns the tag table.
   */
  public List<String> getTagTable()
  {
    return table;
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
  public List<byte[]> compress( IModelObject element) throws IOException
  {
    ICachingPolicy cachingPolicy = element.getCachingPolicy();
    if ( cachingPolicy instanceof ByteArrayCachingPolicy)
    {
      ICompressor compressor = ((ByteArrayCachingPolicy)cachingPolicy).compressor;
      if ( compressor != this) return compressor.compress( element);
    }
    
    // content
    MultiByteArrayOutputStream content = new MultiByteArrayOutputStream();
    writeElement( new DataOutputStream( content), element);
    
    // header (including table)
    MultiByteArrayOutputStream header = new MultiByteArrayOutputStream();
  
    // write header flags
    byte flags = 0;
    if ( predefined) flags |= 0x20;
    header.write( flags);
    
    // write table if necessary
    if ( !predefined) writeTable( new DataOutputStream( header));  
    
    // log
    log.debugf( "%x.compress( %s): predefined=%s", hashCode(), element.getType(), predefined);
    
    // progressive compression assumes a send/receive pair of compressors to remember table entries
    if ( stateful) predefined = true;

    List<byte[]> buffers = header.getBuffers();
    buffers.addAll( content.getBuffers());
    return buffers;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject, java.io.OutputStream)
   */
  @Override
  public void compress( IModelObject element, OutputStream stream) throws IOException
  {
    List<byte[]> buffers = compress( element);
    for( byte[] buffer: buffers) stream.write( buffer);
  }

  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#decompress(java.io.InputStream)
   */
  @Override
  public IModelObject decompress( InputStream stream) throws IOException
  {
    CaptureInputStream input = new CaptureInputStream( stream);
    
    // header flags
    int flags = input.getDataIn().readUnsignedByte();
    boolean predefined = (flags & 0x20) != 0;
    
    // table
    if ( !predefined) readTable( input);

    // log
    log.debugf( "%x.decompress(): predefined=%s", hashCode(), predefined);
    
    // content
    return shallow? readElementShallow( input, null): readElement( input);
  }

  /**
   * Read an element from the input stream.
   * @param stream The input stream.
   * @return Returns the new element.
   */
  public IModelObject readElement( CaptureInputStream stream) throws IOException, CompressorException
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
   * Read an element from the specified byte array and instrument with ByteArrayCachingPolicy.
   * @param stream The input stream.
   * @param cachingPolicy The caching policy.
   * @return Returns the new element.
   */
  public IModelObject readElementShallow( CaptureInputStream stream, ByteArrayCachingPolicy cachingPolicy) throws IOException, CompressorException
  {
    if ( cachingPolicy == null) cachingPolicy = new ByteArrayCachingPolicy( cloneThis());
    
    // read tag name
    String type = readHash( stream);
    
    // create element
    IModelObject element = factory.createExternalObject( null, type);
    readAttributes( stream, element);
    
    // start capturing data for later caching
    stream.startCapture();
    
    // consume children to position pointer to next sibling
    consumeChildren( stream);
    
    // store captured data
    ByteArrayInputStream captured = stream.getCaptureStream();
    element.setStorageClass( new ByteArrayStorageClass( element.getStorageClass(), captured));
    
    // configure element for caching
    element.setCachingPolicy( cachingPolicy);
    element.setDirty( true);
    
    return element;
  }
  
  /**
   * Consume an element from the input stream.
   * @param stream The stream.
   */
  protected void consumeElement( CaptureInputStream stream) throws IOException
  {
    readHash( stream);
    readAttributes( stream, null);
    consumeChildren( stream);
  }
  
  /**
   * Write an element to the output stream.
   * @param stream The output stream.
   * @param element The element.
   */
  protected void writeElement( DataOutputStream stream, IModelObject element) throws IOException, CompressorException
  {
    // write tag name
    writeHash( stream, element.getType());
    
    // write attributes and children
    writeAttributes( stream, element, element.getAttributeNames());
    
    // write children
    IStorageClass storageClass = element.getStorageClass();
    if ( element.isDirty() && storageClass instanceof ByteArrayStorageClass)
    {
      ByteArrayInputStream byteIn = ((ByteArrayStorageClass)storageClass).getStream();
      byteIn.mark();
      copyStream( byteIn, stream);
      byteIn.reset();
    }
    else
    {
      writeChildren( stream, element);
    }
  }
  
  /**
   * Copy all data from the specified input stream to the specified output stream.
   * @param in The input stream.
   * @param out The output stream.
   */
  private void copyStream( InputStream in, DataOutputStream out) throws IOException
  {
    byte[] buffer = new byte[ 4096];
    while( true)
    {
      int nread = in.read( buffer);
      if ( nread < 0) break;
      out.write( buffer, 0, nread);
    }
  }

  /**
   * Read the attributes in the specified buffer and populate the node.
   * @param stream The input stream.
   * @param node The node whose attributes are being read.
   */
  protected void readAttributes( CaptureInputStream stream, IModelObject node) throws IOException, CompressorException
  {
    boolean useJavaSerialization = false;
    
    // read count
    int count = stream.getDataIn().readUnsignedByte();
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
          Object attrValue = serializer.readObject( stream.getDataIn());
          if ( node != null) node.setAttribute( attrName, attrValue);
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
        if ( node != null) node.setAttribute( attrName, attrValue);
      }
    }
  }
  
  /**
   * Write the attributes of the specified node into the buffer at the given offset.
   * @param stream The output stream.
   * @param node The node whose attributes are to be written.
   * @param attrNames The names of the attributes to write.
   */
  protected void writeAttributes( DataOutputStream stream, IModelObject node, Collection<String> attrNames) throws IOException, CompressorException
  {
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
        if ( attrName.length() == 0) serializer.writeValue( stream, node); 
        else serializer.writeObject( stream, node.getAttribute( attrName));
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
   * Read the children in the specified buffer and populate the node.
   * @param stream The input stream.
   * @param node The node whose children are being read.
   */
  protected void readChildren( CaptureInputStream stream, IModelObject node) throws IOException, CompressorException
  {
    ByteArrayCachingPolicy cachingPolicy = (ByteArrayCachingPolicy)node.getCachingPolicy();
    
    // read count (must be read, since information belongs to this element)
    int count = readValue( stream);
    
    // read children
    for( int i=0; i<count; i++)
    {
      IModelObject child = shallow? readElementShallow( stream, cachingPolicy): readElement( stream);
      node.addChild( child);
    }
  }
  
  /**
   * Consume the children from the specified stream.
   * @param stream The stream.
   */
  protected void consumeChildren( CaptureInputStream stream) throws IOException
  {
    // read count (must be read, since information belongs to this element)
    int count = readValue( stream);
    
    // consume children
    for( int i=0; i<count; i++)
      consumeElement( stream);
  }

  /**
   * Write the children of the specified node into the buffer at the given offset.
   * @param stream The output stream.
   * @param node The node whose children are to be written.
   */
  protected void writeChildren( DataOutputStream stream, IModelObject node) throws IOException, CompressorException
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
  protected String readHash( CaptureInputStream stream) throws IOException, CompressorException
  {
    int index = readValue( stream);
    if ( index >= table.size()) 
    {
      log.errorf( "Compressor table:\n%s", dumpTable());
      throw new CompressorException( String.format( "Table entry %d not found.", index));
    }
    return table.get( index);
  }
  
  /**
   * Write a hashed name to the stream.
   * @param stream The output stream.
   * @param name The name.
   */
  protected void writeHash( DataOutputStream stream, String name) throws IOException, CompressorException
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
  protected String readText( CaptureInputStream stream) throws IOException
  {
    int length = readValue( stream);
    byte[] bytes = new byte[ length];
    stream.getDataIn().readFully( bytes);
    return new String( bytes, charset);
  }
  
  /**
   * Write text to the specified stream.
   * @param stream The output stream.
   * @param text The text.
   */
  protected void writeText( DataOutputStream stream, String text) throws CompressorException, IOException
  {
    byte[] bytes = text.getBytes( charset);
    writeValue( stream, bytes.length);
    stream.write( bytes);
  }
  
  /**
   * Read the hash table from the stream.
   * @param stream The input stream.
   */
  protected void readTable( CaptureInputStream stream) throws IOException, CompressorException
  {
    table = new ArrayList<String>();
    
    // read table size
    int count = readValue( stream);
    
    // read entries
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<count; i++)
    {
      sb.setLength( 0);
      
      int b = stream.read();
      while( b != 0)
      {
        if ( (b & 0x80) == 0)
        {
          sb.append( (char)b);
        }
        else
        {
          throw new CompressorException( "UTF-8 encoded xml is not yet supported");
        }
        
        b = stream.read();
      }
      
      String s = sb.toString();
      map.put( s, table.size());
      table.add( s);
    }
    
    stream.read();
  }
  
  /**
   * Write the hash table to the stream.
   * @param stream The output stream.
   */
  protected void writeTable( DataOutputStream stream) throws IOException, CompressorException
  {
    // write table size
    Set<String> keys = map.keySet();
    writeValue( stream, keys.size());

    // write entries
    for( String key: keys) 
    {
      stream.write( key.getBytes( charset));
      stream.write( 0);
    }
    
    stream.write( "|".getBytes());
  }
  
  /**
   * Read a value stored at the hash size.
   * @param stream The input stream.
   * @return Returns the value.
   */
  protected int readValue( CaptureInputStream stream) throws IOException
  {
    DataInputStream dataIn = stream.getDataIn();
    
    int b1 = dataIn.readUnsignedByte();
    if ( (b1 & 0x80) != 0)
    {
      b1 &= 0x7f;
      int b2 = dataIn.readUnsignedByte();
      int b3 = dataIn.readUnsignedByte();
      int b4 = dataIn.readUnsignedByte();
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
  protected void writeValue( DataOutputStream stream, int value) throws IOException, CompressorException
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
 
  /**
   * Dump the string table.
   * @return Returns a string containing the table dump.
   */
  private String dumpTable()
  {
    StringBuilder sb = new StringBuilder();
    for( int i=0; i<table.size(); i++)
    {
      sb.append( String.format( "%d = '%s'\n", i, table.get( i)));
    }
    return sb.toString();
  }
  
  /**
   * Clone this compressor and its current map/table.
   * @return Returns the cloned compressor.
   */
  private TabularCompressor cloneThis()
  {
    TabularCompressor clone = new TabularCompressor( stateful, shallow);
    clone.hashIndex = hashIndex;
    clone.predefined = predefined;
    clone.charset = charset;
    clone.table.addAll( table);
    for( int i=0; i<table.size(); i++)
      clone.map.put( table.get( i), i);
    return clone;
  }
    
  private final static Log log = Log.getLog( TabularCompressor.class);
 
  private List<String> table;
  private Map<String, Integer> map;
  private int hashIndex;
  private boolean predefined;
  private boolean stateful;
  private boolean shallow;
  private Charset charset;
  
  public static void main( String[] args) throws Exception
  {
    //Log.getLog( ByteCounterInputStream.class).setLevel( Log.all);

//    System.out.println( "\n\n----- #2 Decompressing (shallow) -----\n");
//    ByteCounterInputStream fin = new ByteCounterInputStream( new FileInputStream( "request.xip"));
//    TabularCompressor c1 = new TabularCompressor( false, true, Order.breadthFirst);
//    IModelObject req = c1.decompress( fin);
//    System.out.println( XmlIO.write( Style.printable, req));
//    System.exit( 1);
 
    
    String xml =
      "<A>" +
      "  <B>" +
      "    <F/>" +
      "  </B>" +
      "  <C>" +
      "    <G>" +
      "      <I/>" +      
      "    </G>" +
      "  </C>" +
      "  <D>" +
      "    <H/>" +
      "  </D>" +
      "  <E/>" +
      "</A>";

    IModelObject el = new XmlIO().read( xml);
    
    System.out.println( "----- #1 Compressing -----\n");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    TabularCompressor c = new TabularCompressor( false, false);
    c.compress( el, out);
    out.close();
    
    byte[] b1 = out.toByteArray();
    System.out.printf( "%s\n", HexDump.toString( b1));
    
    System.out.println( "\n\n----- #2 Decompressing (shallow) -----\n");
    ByteCounterInputStream in = new ByteCounterInputStream( new ByteArrayInputStream( b1));
    c = new TabularCompressor( false, true);
    el = c.decompress( in);

//    IModelObject n1 = el.getChild( 0);
    //n1.removeChild( 0);
//    el.getChild( 1).getChild( 0).getChild( 0);
    
    System.out.println( "\n\n----- #3 Compressing -----\n");
    out = new ByteArrayOutputStream();
    c = new TabularCompressor( false, false);
    c.compress( el, out);
    out.close();
    
    byte[] b2 = out.toByteArray();
    System.out.printf( "\n\n%s\n", HexDump.toString( b2));
    System.out.println( XmlIO.write( Style.printable, el));
    
    
    
    System.out.println( "\n\n----- #4 Decompressing (shallow) -----\n");
    in = new ByteCounterInputStream( new ByteArrayInputStream( b2));
    c = new TabularCompressor( false, true);
    el = c.decompress( in);

    System.out.println( XmlIO.write( Style.printable, el));
  }
}
