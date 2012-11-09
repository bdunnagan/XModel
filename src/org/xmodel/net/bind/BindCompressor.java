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
import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.external.IExternalReference;

public class BindCompressor extends TabularCompressor
{
  private BindCompressor( BindProtocol protocol, boolean progressive)
  {
    super( progressive);
    
    this.protocol = protocol;
    this.localMap = new HashMap<Integer, IModelObject>();
    this.remoteMap = new HashMap<Integer, IModelObject>();
  }
  
  /**
   * Create a new client-side compressor.
   * @param protocol The protocol bundle.
   * @param progressive See TabularCompressor for more information.
   * @return Returns the new compressor.
   */
  public static BindCompressor newClientCompressor( BindProtocol protocol, boolean progressive)
  {
    return new BindCompressor( protocol, progressive);
  }
  
  /**
   * Create a new server-side compressor.
   * @param progressive See TabularCompressor for more information.
   * @return Returns the new compressor.
   */
  public static BindCompressor newServerCompressor( boolean progressive)
  {
    return new BindCompressor( null, progressive);
  }
  
  /**
   * Find an element by its local network identifier.
   * @param netID The network identifier.
   * @return Returns null or the element.
   */
  public IModelObject findLocal( long netID)
  {
    return localMap.get( netID);
  }
  
  /**
   * Find an element by its remote network identifier.
   * @param netID The network identifier.
   * @return Returns null or the element.
   */
  public IModelObject findRemote( long netID)
  {
    return localMap.get( netID);
  }
  
  /**
   * Returns the local network identifier for the specified element.
   * @param element An element that was previously sent downstream.
   * @return Returns the local network identifier.
   */
  public long getLocalNetID( IModelObject element)
  {
    return System.identityHashCode( element);
  }
  
  /**
   * Returns the remote network identifier for the specified element.
   * @param element An element that was previously received from upstream.
   * @return Returns the remote network identifier.
   */
  public long getRemoteNetID( IModelObject element)
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
   * Read an element from the input stream.
   * @param stream The input stream.
   * @return Returns the new element.
   */
  protected IModelObject readElement( ChannelBuffer stream) throws IOException, CompressorException
  {
    // read tag name
    String type = readHash( stream);
    
    // read flags
    byte flags = stream.readByte();
    
    // read network id
    int netID = stream.readInt();
    
    // create element
    IModelObject element;
    if ( (flags & 0x01) != 0)
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
    
    readAttributes( stream, element);
    readChildren( stream, element);

    // store element by network id
    remoteMap.put( netID, element);

    // disassociate from model so it can be passed to a new thread
    element.clearModel();
    
    return element;
  }
  
  /**
   * Write an element to the output stream.
   * @param stream The output stream.
   * @param element The element.
   */
  protected void writeElement( ChannelBuffer stream, IModelObject element) throws IOException, CompressorException
  {
    IExternalReference reference = (element instanceof IExternalReference)? (IExternalReference)element: null;
    
    // write tag name
    writeHash( stream, element.getType());
    
    // write flags
    byte flags = 0;
    if ( reference != null) flags |= 0x01;
    if ( element.isDirty()) flags |= 0x02;
    stream.writeByte( flags);
    
    // write network id
    int netID = System.identityHashCode( element);
    localMap.put( netID, element);
    stream.writeInt( netID);

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

  private BindProtocol protocol;
  private Map<Integer, IModelObject> localMap;
  private Map<Integer, IModelObject> remoteMap;
  private Channel channel;
  private int timeout;
}
