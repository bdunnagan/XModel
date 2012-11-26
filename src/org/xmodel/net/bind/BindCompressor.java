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
package org.xmodel.net.bind;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.INode;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.NetworkCachingPolicy;

/**
 * A TabularCompressor that does tracks the network identifiers of remote-bound elements.
 */
public class BindCompressor extends TabularCompressor
{
  public final static class BindResult
  {
    public INode element;
    public int netID;
  }
  
  private BindCompressor( BindProtocol protocol, boolean progressive)
  {
    super( progressive);
    
    this.protocol = protocol;
    this.localMap = new ConcurrentHashMap<Integer, INode>();
    this.remoteMap = new ConcurrentHashMap<Integer, INode>();
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    localMap.clear();
    remoteMap.clear();
  }
  
  /**
   * Create a new request compressor.
   * @param protocol The protocol bundle.
   * @param progressive See TabularCompressor for more information.
   * @return Returns the new compressor.
   */
  public static BindCompressor newRequestCompressor( BindProtocol protocol, boolean progressive)
  {
    return new BindCompressor( protocol, progressive);
  }
  
  /**
   * Create a new response compressor.
   * @param progressive See TabularCompressor for more information.
   * @return Returns the new compressor.
   */
  public static BindCompressor newResponseCompressor( boolean progressive)
  {
    return new BindCompressor( null, progressive);
  }
  
  /**
   * Find an element by its local network identifier.
   * @param netID The network identifier.
   * @return Returns null or the element.
   */
  public INode findLocal( int netID)
  {
    return localMap.get( netID);
  }
  
  /**
   * Find an element by its remote network identifier.
   * @param netID The network identifier.
   * @return Returns null or the element.
   */
  public INode findRemote( int netID)
  {
    return remoteMap.get( netID);
  }

  /**
   * Set the element associated with the specified remote network identifier.
   * @param netID The network identifier.
   * @param element The imposter element.
   */
  public void setRemoteImposter( int netID, INode element)
  {
    remoteMap.put( netID, element);
  }
  
  /**
   * Returns the local network identifier for the specified element.
   * @param element An element that was previously sent downstream.
   * @return Returns the local network identifier.
   */
  public int getLocalNetID( INode element)
  {
    return System.identityHashCode( element);
  }
  
  /**
   * Returns the remote network identifier for the specified element.
   * @param element An element that was previously received from upstream.
   * @return Returns the remote network identifier.
   */
  public int getRemoteNetID( INode element)
  {
    if ( !(element instanceof IExternalReference)) return 0;
    
    NetKeyCachingPolicy cachingPolicy = (NetKeyCachingPolicy)((IExternalReference)element).getCachingPolicy();
    return cachingPolicy.getNetID();
  }
  
  /**
   * Set the channel for the next call to the <code>decompress</code> method.
   * @param channel The channel.
   */
  public void setChannel( Channel channel)
  {
    this.channel = channel;
  }
  
  /**
   * Set the timeout for the next call to the <code>decompress</code> method.
   * @param timeout The timeout.
   */
  public void setTimeout( int timeout)
  {
    this.timeout = timeout;
  }

  /**
   * Decompress the next element and update the specified reference.
   * @param input The input buffer.
   * @param reference The reference being remotely bound.
   */
  public void decompress( ChannelBuffer input, IExternalReference reference) throws IOException
  {
    // header flags
    int flags = input.readUnsignedByte();
    boolean predefined = (flags & 0x20) != 0;
    
    // table
    if ( !predefined) readTable( input);

    // log
    log.debugf( "%x.decompress(): predefined=%s", hashCode(), predefined);
    
    // read element
    readElement( input, reference);
  }

  /**
   * Read an element from the input stream.  Note that this method performs the updating of the reference argument,
   * instead of leaving that operation to the caching policy.  This method takes responsibility for adding the
   * secondary caching stages.
   * @param stream The input stream.
   * @param binding The reference being remotely bound.
   * @return Returns the new element.
   */
  protected INode readElement( ChannelBuffer stream, IExternalReference binding) throws IOException, CompressorException
  {
    // read network id
    int netID = stream.readInt();
    
    // read tag name
    String type = readHash( stream);
    
    // read flags
    byte flags = stream.readByte();
    
    // create element
    INode element;
    if ( binding != null)
    {
      element = binding;
      ((NetworkCachingPolicy)binding.getCachingPolicy()).setRemoteNetID( netID);
    }
    else if ( (flags & 0x01) != 0)
    {
      // read static attributes
      int count = readValue( stream);
      String[] attrNames = new String[ count];
      for( int i=0; i<count; i++)
      {
        String attrName = readHash( stream);
        attrNames[ i] = attrName;
      }
      
      // create element
      IExternalReference reference = factory.createExternalObject( null, type);
      NetKeyCachingPolicy cachingPolicy = new NetKeyCachingPolicy( protocol, channel, netID, timeout, attrNames);
      reference.setCachingPolicy( cachingPolicy);
      reference.setDirty( (flags & 0x02) != 0);
      
      element = reference;
    }
    else
    {
      element = factory.createObject( null, type);
    }
    
    // map element to network identifier
    remoteMap.put( netID, element);

    readAttributes( stream, element);
    readChildren( stream, element);

    // disassociate from model so it can be passed to a new thread
    element.clearModel();
    
    return element;
  }
  
  /**
   * Read an element from the input stream.
   * @param stream The input stream.
   * @return Returns the new element.
   */
  @Override
  protected INode readElement( ChannelBuffer stream) throws IOException, CompressorException
  {
    return readElement( stream, null);
  }
  
  /**
   * Write an element to the output stream.
   * @param stream The output stream.
   * @param element The element.
   */
  @Override
  protected void writeElement( ChannelBuffer stream, INode element) throws IOException, CompressorException
  {
    IExternalReference reference = (element instanceof IExternalReference)? (IExternalReference)element: null;
    
    // write network id
    int netID = System.identityHashCode( element);
    localMap.put( netID, element);
    stream.writeInt( netID);

    // write tag name
    writeHash( stream, element.getType());
    
    // write flags
    byte flags = 0;
    if ( reference != null) flags |= 0x01;
    if ( element.isDirty()) flags |= 0x02;
    stream.writeByte( flags);
    
    // write static attributes
    if ( reference != null)
    {
      String[] attrNames = reference.getStaticAttributes();
      writeValue( stream, attrNames.length);
      for( String attrName: attrNames) writeHash( stream, attrName);
    }
   
    // write attributes and children
    if ( element.isDirty())
    {
      writeAttributes( stream, element, Arrays.asList( reference.getStaticAttributes()));
    }
    else
    {
      writeAttributes( stream, element, element.getAttributeNames());
      writeChildren( stream, element);
    }
  }

  private final static Log log = Log.getLog( BindCompressor.class);
  
  private BindProtocol protocol;
  private Map<Integer, INode> localMap;
  private Map<Integer, INode> remoteMap;
  private Channel channel;
  private int timeout;
}
