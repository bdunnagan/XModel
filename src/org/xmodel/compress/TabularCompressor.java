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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.util.ByteCounterInputStream;

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
  public enum Order { depthFirst, breadthFirst};
  
  public TabularCompressor()
  {
    this( false, false, Order.breadthFirst);
  }
  
  /**
   * Create a TabularCompressor that optionally omits the tag table from the compressed output
   * when no new tags have been added since the previous call to the <code>compress</code>
   * method.
   * @param stateful True if tag table can be omitted.
   * @param shallow True if shallow, on-demand de-serialization should be used (implies breadth-first).
   * @param order The ordering of the elements.
   */
  public TabularCompressor( boolean stateful, boolean shallow, Order order)
  {
    this.factory = new ModelObjectFactory();
    this.map = new LinkedHashMap<String, Integer>();
    this.table = new ArrayList<String>();
    this.predefined = false;
    this.stateful = stateful;
    this.shallow = shallow;
    this.order = order;
    this.charset = Charset.forName( "UTF-8");
    
    if ( shallow && order == Order.depthFirst)
      throw new IllegalArgumentException( "Shallow flag requires breadth-first ordering.");
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
    // initialize table from element, if present
    initTable( element);
    
    // content
    MultiByteArrayOutputStream content = new MultiByteArrayOutputStream();
    writeElement( new DataOutputStream( content), element);
    
    // header (including table)
    MultiByteArrayOutputStream header = new MultiByteArrayOutputStream();
  
    // write header flags
    byte flags = 0;
    if ( predefined) flags |= 0x20;
    if ( order == Order.breadthFirst) flags |= 0x40;
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
    DataInputStream input = new DataInputStream( stream);
    
    // header flags
    int flags = input.readUnsignedByte();
    boolean predefined = (flags & 0x20) != 0;
    order = ((flags & 0x40) != 0)? Order.breadthFirst: Order.depthFirst;
    
    // table
    if ( !predefined) readTable( input);

    // log
    log.debugf( "%x.decompress(): predefined=%s", hashCode(), predefined);
    
    // content
    return readElement( input);
  }

  /**
   * Read an element from the input stream.
   * @param stream The input stream.
   * @return Returns the new element.
   */
  protected IModelObject readElement( DataInputStream stream) throws IOException, CompressorException
  {
    if ( shallow)
    {
      // read tag
      String type = readHash( stream);
      IModelObject element = factory.createExternalObject( null, type);
      
      // read attributes, but not number of children
      readAttributes( stream, element);
      
      DecompressCachingPolicy cachingPolicy = new DecompressCachingPolicy( this, new StreamReference( stream));
      cachingPolicy.elements = Collections.singletonList( element);
      element.setCachingPolicy( cachingPolicy);
      element.setDirty( true);
      
      return element;
    }
    else
    {
      IModelObject root = null;

      // read tree
      Deque<IModelObject> deque = new ArrayDeque<IModelObject>();
      deque.add( nullParent);
      while( deque.size() > 0)
      {
        IModelObject parent = deque.removeFirst();

        // read tag
        String type = readHash( stream);
        IModelObject element = factory.createObject( null, type);
        
        // read attributes and number of children
        readAttributes( stream, element);
        int numChildren = readValue( stream);

        // add child to parent
        if ( parent != nullParent) parent.addChild( element); else root = element;
        
        // push parent onto stack once for each of its children
        for( int i=0; i<numChildren; i++)
        {
          if ( order == Order.depthFirst) deque.addFirst( element); else deque.addLast( element);
        }
      }
      
      return root;
    }
  }
  
  /**
   * Write an element to the output stream.
   * @param stream The output stream.
   * @param element The element.
   */
  protected void writeElement( DataOutputStream stream, IModelObject element) throws IOException, CompressorException
  {
    // write tree
    Deque<IModelObject> deque = new ArrayDeque<IModelObject>();
    deque.add( element);
    while( deque.size() > 0)
    {
      element = deque.removeFirst();
      
      // write tag and attributes
      writeHash( stream, element.getType());
      writeAttributes( stream, element, element.getAttributeNames());
      
      // use stream, if present, to write content of children (note that number of children is not written)
      ICachingPolicy cachingPolicy = element.getCachingPolicy();
      if ( element.isDirty() && cachingPolicy != null && cachingPolicy instanceof DecompressCachingPolicy)
      {
        copyStream( ((DecompressCachingPolicy)cachingPolicy).streamRef.stream, stream);
        break;
      }
      else
      {
        // write number of children
        List<IModelObject> children = element.getChildren();
        writeValue( stream, children.size());
  
        // write child tags
        for( IModelObject child: children)
        {
          if ( order == Order.depthFirst) deque.addFirst( child); else deque.addLast( child);
        }
      }
    }
  }
  
  /**
   * Copy all data from the specified input stream to the specified output stream.
   * @param in The input stream.
   * @param out The output stream.
   */
  private void copyStream( DataInputStream in, DataOutputStream out) throws IOException
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
   * Read the children in the specified buffer and populate the node.
   * @param streamRef The stream reference.
   * @param root The node whose children are being read.
   */
  protected void readChildren( StreamReference streamRef, IModelObject root) throws IOException, CompressorException
  {
    DataInputStream stream = streamRef.stream;
    
    // read number of children
    int numChildren = readValue( stream);

    // configure caching policy to read children on demand
    DecompressCachingPolicy cachingPolicy = (numChildren > 0)? new DecompressCachingPolicy( this, streamRef): null;
    
    for( int i=0; i<numChildren; i++)
    {
      String type = readHash( stream);
      
      IModelObject child = factory.createExternalObject( null, type);
      readAttributes( stream, child);
      
      child.setCachingPolicy( cachingPolicy);
      child.setDirty( true);

      root.addChild( child);
    }
    
    if ( cachingPolicy != null) cachingPolicy.elements = root.getChildren();
  }
  
  /**
   * Read the attributes in the specified buffer and populate the node.
   * @param stream The input stream.
   * @param node The node whose attributes are being read.
   */
  protected void readAttributes( DataInputStream stream, IModelObject node) throws IOException, CompressorException
  {
    boolean useJavaSerialization = false;
    
    // read count
    int count = stream.readUnsignedByte();
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
          Object attrValue = serializer.readObject( stream);
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
   * Read a hashed name from the stream.
   * @param stream The input stream.
   * @return Returns the name.
   */
  protected String readHash( DataInputStream stream) throws IOException, CompressorException
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
  protected String readText( DataInputStream stream) throws IOException
  {
    int length = readValue( stream);
    byte[] bytes = new byte[ length];
    stream.readFully( bytes);
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
  protected void readTable( DataInputStream stream) throws IOException, CompressorException
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
  }
  
  /**
   * Initialize table using instance of TabularCompressor in DecompressCachingPolicy, if present.
   * @param element The element to be compressed.
   */
  protected void initTable( IModelObject element)
  {
    ICachingPolicy cachingPolicy = element.getCachingPolicy();
    if ( cachingPolicy != null && cachingPolicy instanceof DecompressCachingPolicy)
    {
      TabularCompressor compressor = ((DecompressCachingPolicy)cachingPolicy).compressor;
      map = compressor.map;
      table = compressor.table;
    }
  }
  
  /**
   * Read a value stored at the hash size.
   * @param stream The input stream.
   * @return Returns the value.
   */
  protected int readValue( DataInputStream stream) throws IOException
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
      
  public static class DecompressCachingPolicy extends AbstractCachingPolicy
  {
    public DecompressCachingPolicy( TabularCompressor compressor, StreamReference streamRef)
    {
      streamRef.count++;
      this.streamRef = streamRef;
      this.compressor = compressor;
      
      setStaticAttributes( new String[] { "*"});
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
     */
    @Override
    public void sync( IExternalReference reference) throws CachingException
    {
      if ( complete) return;
      complete = true;
      
      try
      {
        for( IModelObject parent: elements)
          compressor.readChildren( streamRef, parent);

        if ( --streamRef.count == 0) 
          streamRef.stream.close();
      }
      catch( IOException e)
      {
        throw new RuntimeException( "Caught exception during deserialization: ", e);
      }
    }

    private TabularCompressor compressor;
    private StreamReference streamRef;
    protected List<IModelObject> elements;
    private boolean complete;
  }
  
  private static class StreamReference
  {
    public StreamReference( DataInputStream stream)
    {
      this.stream = stream;
      this.count = 0;
    }
    
    public DataInputStream stream;
    public long count;
  }
  
  private final static Log log = Log.getLog( TabularCompressor.class);
  private final static IModelObject nullParent = new ModelObject( "");
 
  private List<String> table;
  private Map<String, Integer> map;
  private boolean predefined;
  private boolean stateful;
  private boolean shallow;
  private Order order;
  private Charset charset;
  
  public static void main( String[] args) throws Exception
  {
    boolean shallow = false;
    
    ByteCounterInputStream in = new ByteCounterInputStream( new BufferedInputStream( new FileInputStream( "KingLear.xip")));
    TabularCompressor c1 = new TabularCompressor( false, shallow, Order.breadthFirst);
    IModelObject book = c1.decompress( in);
    
    book.getChildren();
    
    FileOutputStream out = new FileOutputStream( "KingLear-2.xip");
    TabularCompressor c2 = new TabularCompressor( false, shallow, Order.breadthFirst);
    c2.compress( book, out);
    out.close();
  }
}
