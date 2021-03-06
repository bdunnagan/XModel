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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.storage.ByteArrayStorageClass;
import org.xmodel.storage.IStorageClass;
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
    this.stateful = stateful;
    this.shallow = shallow;
    this.charset = Charset.forName( "UTF-8");
    
    clearTable();
  }

  /**
   * Set the default table to contain the specified tags.
   * @param tags The tags.
   */
  public static void setImplicitTable( String[] tags)
  {
    globalTable = new ArrayList<String>();
    globalMap = new LinkedHashMap<String, Integer>();
    
    CRC32 crc = new CRC32();
    for( int i=0; i<tags.length; i++)
    {
      String tag = tags[ i];
      if ( !globalMap.containsKey( tags))
      {
        log.verbosef( "Implicit tag: [%d] '%s'", i, tag);
        crc.update( tag.getBytes());
        globalMap.put( tag, i);
        globalTable.add( tag);
      }
    }
    
    log.infof( "Implicit table hash: %X", crc.getValue());
  }
  
  /**
   * Predefine the tag table. If the compressor does not encounter tags which are not defined
   * in the table then the table will not be written to the output. This is useful when all
   * the tags are known in advance.
   * @param table The table.
   */
  public void defineTable( List<String> table)
  {
    this.table = new ArrayList<String>( table);
    map.clear();
    for( int i=0; i<table.size(); i++) map.put( table.get( i), i);
    predefined = true;
  }
  
  /**
   * @return Returns the tag table.
   */
  public List<String> getTable()
  {
    return table;
  }
  
  /**
   * Clear the predefined tag table.
   */
  public void clearTable()
  {
    map = new LinkedHashMap<String, Integer>();
    table = new ArrayList<String>();
    predefined = false;
    if ( globalTable != null) table.addAll( globalTable);
    if ( globalMap != null) map.putAll( globalMap);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.compress.ICompressor#compress(org.xmodel.IModelObject)
   */
  @Override
  public List<byte[]> compress( IModelObject element) throws IOException
  {
    resolveTable( element);
    thruBytesWritten = 0;
    
    // content
    MultiByteArrayOutputStream content = new MultiByteArrayOutputStream();
    writeElement( new DataOutputStream( content), element);
    
    // header (including table)
    MultiByteArrayOutputStream header = new MultiByteArrayOutputStream();
  
    // write header flags
    byte flags = (byte)((globalTable.size() > 0)? 0x10: 0);
    if ( predefined) flags |= 0x20;
    header.write( flags);
    
    // write table if necessary
    if ( !predefined) writeTable( new DataOutputStream( header));
    
    // log
    log.verbosef( "Compressed '%s' with table: %s", element.getType(), dumpTable());
    
    // progressive compression assumes a send/receive pair of compressors to remember table entries
    if ( stateful) predefined = true;

    log.debugf( "Compression: total=%1.1fK, thru=%1.1fK, thru-ratio=%1.0f%%", 
      (header.getWritten() + content.getWritten()) / 1000f,
      thruBytesWritten / 1000f,
      ((double)thruBytesWritten / content.getWritten()) * 100f);
    
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
    boolean importGlobal = (flags & 0x10) != 0;
    
    // table
    if ( !predefined) readTable( input, importGlobal);

    // log
    log.verbosef( "%x.decompress(): predefined=%s", hashCode(), predefined);
    
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
  private IModelObject readElementShallow( CaptureInputStream stream, ByteArrayCachingPolicy cachingPolicy) throws IOException, CompressorException
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
      thruBytesWritten += nread;
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
        serializer.writeObject( stream, (attrName.length() == 0)? node.getValue(): node.getAttribute( attrName));
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
    readChildren( stream, node, shallow);
  }
  
  /**
   * Read the children in the specified buffer and populate the node.
   * @param stream The input stream.
   * @param node The node whose children are being read.
   * @param shallow True if partial decompression should be employed.
   */
  protected void readChildren( CaptureInputStream stream, IModelObject node, boolean shallow) throws IOException, CompressorException
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
      hash = table.size();
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
   * @param importGlobal True if global tags should be imported.
   */
  protected void readTable( CaptureInputStream stream, boolean importGlobal) throws IOException, CompressorException
  {
    if ( importGlobal)
    {
      clearTable();
    }
    else
    {
      map = new LinkedHashMap<String, Integer>();
      table = new ArrayList<String>();
    }
    
    // read table size
    int count = readValue( stream);
    
    // read entries
    ByteBuffer buf = ByteBuffer.allocate( 512);
    for( int i=0; i<count; i++)
    {
      buf.clear();
      
      int b = stream.read();
      while( b != 0)
      {
        if ( (b & 0x80) == 0)
        {
          buf.put( (byte)b);
        }
        else
        {
          throw new CompressorException( "UTF-8 encoded xml is not yet supported");
        }
        
        b = stream.read();
      }
      
      String tag = new String( buf.array(), 0, buf.position(), charset);
      log.debugf( "Tag: %s", tag);
      
      reserveTag( tag);
    }
  }
  
  /**
   * Write the hash table to the stream.
   * @param stream The output stream.
   */
  protected void writeTable( DataOutputStream stream) throws IOException, CompressorException
  {
    // write table hash
    //stream.writeInt( getTableHash());
    
    // get global table size
    int globalCount = globalTable.size();
    
    // write table size
    writeValue( stream, table.size() - globalCount);

    // write entries
    for( int i=globalCount; i<table.size(); i++) 
    {
      stream.write( table.get( i).getBytes( charset));
      stream.write( 0);
    }
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
   * Find the largest element in the specified sub-tree that has not been decompressed and 
   * redefine the table of this compressor so that it can be compressed with being synced. 
   * @param root The root of the sub-tree to search.
   */
  private void resolveTable( IModelObject root)
  {
    long t0 = System.nanoTime();
    
    List<IModelObject> dirty = new ArrayList<IModelObject>();
    IModelObject largest = null;
    int largestSize = 0;
    
    Deque<IModelObject> deque = new ArrayDeque<IModelObject>();
    deque.addLast( root);
    
    while( !deque.isEmpty())
    {
      IModelObject element = deque.removeFirst();
      if ( element.isDirty())
      {
        IStorageClass storageClass = element.getStorageClass(); 
        if ( storageClass instanceof ByteArrayStorageClass)
        {
          dirty.add( element);
          
          ByteArrayInputStream stream = ((ByteArrayStorageClass)storageClass).getStream();
          int size = stream.available();
          if ( size > largestSize)
          {
            largest = element;
            largestSize = size;
          }
        }
      }
      else
      {
        for( IModelObject child: element.getChildren())
          deque.addLast( child);
      }
    }
    
    // sync smaller partially decompressed elements
    if ( dirty != null)
    {
      boolean wasShallow = shallow;
      shallow = false;
      int unresolved = 0;
      for( IModelObject element: dirty)
      {
        if ( element.getCachingPolicy() != largest.getCachingPolicy())
        {
          boolean compatible = compareTables( 
              ((ByteArrayCachingPolicy)largest.getCachingPolicy()).compressor, 
              ((ByteArrayCachingPolicy)element.getCachingPolicy()).compressor);
          if ( !compatible)
          {
            unresolved++;
            element.getChildren();
          }
        }
      }
      shallow = wasShallow;
      
      log.debugf( "Synced %d elements with conflicting tables", unresolved);
    }
    
    // update table
    if ( largest != null)
    {
      log.debugf( "Copying table of largest partially decompressed element, %s", largest.getType());
      
      clearTable();
      ByteArrayCachingPolicy cachingPolicy = (ByteArrayCachingPolicy)largest.getCachingPolicy();
      for( String tag: cachingPolicy.compressor.table) reserveTag( tag);
    }
    
    long t1 = System.nanoTime();
    log.debugf( "Compressor table resolved in %1.1fms", (t1 - t0) / 1e6);
  }
  
  /**
   * Compare the tables of the two compressors.
   * @param c1 The first compressor.
   * @param c2 The second compressor.
   * @return Returns true if the tables are compatible.
   */
  private static boolean compareTables( TabularCompressor c1, TabularCompressor c2)
  {
    for( int i=0; i<c1.table.size() && i<c2.table.size(); i++)
    {
      if ( !c1.table.get( i).equals( c2.table.get( i)))
          return false;
    }
    return true;
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
      sb.append( table.get( i));
      sb.append( ", ");
    }
    if ( sb.length() > 0) sb.setLength( sb.length() - 2);
    return sb.toString();
  }
  
  /**
   * Add a tag to the tag table.
   * @param tag The element/attribute name.
   */
  private void reserveTag( String tag)
  {
    if ( !map.containsKey( tag))
    {
      map.put( tag, table.size());
      table.add( tag);
    }
  }

  /**
   * Clone this compressor and its current map/table.
   * @return Returns the cloned compressor.
   */
  private TabularCompressor cloneThis()
  {
    TabularCompressor clone = new TabularCompressor( stateful, shallow);
    clone.predefined = predefined;
    clone.charset = charset;
    
    clone.map = new LinkedHashMap<String, Integer>();
    clone.table = new ArrayList<String>();
    for( int i=0; i<table.size(); i++)
      clone.reserveTag( table.get( i));
    
    return clone;
  }
    
  protected final static Log log = Log.getLog( TabularCompressor.class);
  
  private static List<String> globalTable = Collections.emptyList();
  private static Map<String, Integer> globalMap = Collections.emptyMap();
 
  private List<String> table;
  private Map<String, Integer> map;
  private boolean predefined;
  private boolean stateful;
  private boolean shallow;
  private Charset charset;
  private int thruBytesWritten;
  
  public static void main( String[] args) throws Exception
  {
    log.setLevel( Log.debug);
    //Log.getLog( ByteCounterInputStream.class).setLevel( Log.all);

//    System.out.println( "\n\n----- #2 Decompressing (shallow) -----\n");
//    ByteCounterInputStream fin = new ByteCounterInputStream( new FileInputStream( "request.xip"));
//    TabularCompressor c1 = new TabularCompressor( false, true, Order.breadthFirst);
//    IModelObject req = c1.decompress( fin);
//    System.out.println( XmlIO.write( Style.printable, req));
//    System.exit( 1);
 
    
    String[] tags = { 
        "result", "", "item", "response", "summary", "http4", "http6", "url", "ping4", "ping6", "min", "max", "avg", "interfaces",
        "exception", "request"," assign", "name", "connections", "script", "package", "logi", "logd", "logecreate", "var", "info",
        "id", "version", "localAddress", "remoteAddress", "nic", "add", "delete", "move", "source", "target", "return", "address",
        "mac", "prefix", "scope", "ipv4", "ipv6", "status", "results", "type", "timestamp", "error", "load", "start", "dns", "connect",
        "first", "size", "redirect", "landing"
      };
    Arrays.sort( tags);
    TabularCompressor.setImplicitTable( tags);
//    
//    File file = new File( "/Users/bdunnagan/git/Sonar/IP6Sonar2/src/com/nephos6/ip6sonar/web/ipsonar-scripts.xip");
    
//    ICompressor c = new TabularCompressor( false, true);
//    IModelObject purchases = c.decompress( new BufferedInputStream( new FileInputStream( file)));
//    System.out.println( XmlIO.write( Style.printable, purchases));
    
    String hex = "1F8B0800000000000000A552CB8A143114BDF1B11851147AE7AA1994DE4C8ABCEA91F81C5B172E446166218814A93C7A4ABBAA9AAAA47010C14FB7BA5BA61910145C04CEBD398773937B1EDC335D1B5C1BC07471027DD9EAC65D15E1727328625B1F6863B78E13310E13AE2D6CFAEE8B33610B7B3774B1376E8F4DD75B674B1D0EFDC6056D75D0F0D55DC2A8D7D141E38641AF769202D00CEE26088EAC1B717043A02011DC1F9BC46CE2F69431D46B50086EAF745C397882003D86A7086EA589E0F00CE03982799549AE25F105A1B9D0D4CB8A59CA0937AC90362B327881E011B75E5A927B9C91DC6051F01C6B9D6A9C5705B3DC114FB4018E60C6081598124CC4394D15658A487889E0E11FFA49460521044EE1E60C6EBC4270A77581969BBE1E292C11FC9C1EF8E9F87B5CBC3FC36F3E9EE3B71FCEF0BBD3A56AB429B5B5FD42CDE3C26B4533C59D125EE554D96C71327547D70F75D74E8C6C5B5EB1D934BEF226258AAB3C55BED869BD57DEEDF4BFE50743B55DECDEA8FEE6ECE2C7F1C9FC7F4712D746A2194B18CD134A45C2B8F8ABFF67D87FD6EC62BAE9C77AE8FAF2A21BC22E8ECB6B69D8138FA6D4EA7697B8E5BFACFBF5B4EE34358ECA5C63C128C5427389AB540B2C652504333477DCC22FE93DB68213030000";
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    for( int i=0; i<hex.length(); i+=2)
    {
      int c = Integer.parseInt( hex.substring( i, i+2), 16);
      b.write( c);
    }
    b.flush();
    b.close();
    
    ICompressor c = new ZipCompressor( new TabularCompressor( false, false));
    IModelObject o = c.decompress( new ByteArrayInputStream( b.toByteArray()));
    System.out.println( XmlIO.write( Style.printable, o));
  }
}
