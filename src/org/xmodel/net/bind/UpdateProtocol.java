package org.xmodel.net.bind;

import java.io.IOException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.net.nu.FullProtocolChannelHandler.Type;

public class UpdateProtocol
{
  public UpdateProtocol( BindProtocol protocol)
  {
    this.protocol = protocol;
    this.attrLengthEstimate = 64;
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param parent The parent.
   * @param child The child.
   * @param index The insertion index.
   */
  public void sendAddChild( Channel channel, IModelObject parent, IModelObject child, int index) throws IOException
  {
    long parentNetID = protocol.serverCompressor.getLocalNetID( parent);
        
    ChannelBuffer buffer2 = protocol.serverCompressor.compress( child);
    ChannelBuffer buffer1 = protocol.headerProtocol.writeHeader( Type.addChild, 12 + buffer2.readableBytes());
    buffer1.writeLong( parentNetID);
    buffer1.writeInt( index);
    
    long childNetID = protocol.serverCompressor.getLocalNetID( child);
    log.debugf( "UpdateProtocol.sendAddChild: parent=%s/%X, child=%s/%X, index=%d", parent.getType(), parentNetID, child.getType(), childNetID, index);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
  }
  
  /**
   * Handle an update.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handleAddChild( Channel channel, ChannelBuffer buffer) throws IOException
  {
    long parentNetID = buffer.readLong();
    int index = buffer.readInt();
    IModelObject child = protocol.clientCompressor.decompress( buffer);

    log.debugf( "UpdateProtocol.handleAddChild: parent=%X, child=%s, index=%d", parentNetID, child.getType(), index);
    
    protocol.dispatcher.execute( new AddChildEvent( channel, parentNetID, child, index));
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param parent The parent.
   * @param index The index of the child.
   */
  public void sendRemoveChild( Channel channel, IModelObject parent, int index) throws IOException
  {
    long parentNetID = protocol.serverCompressor.getLocalNetID( parent);
    
    ChannelBuffer buffer = protocol.headerProtocol.writeHeader( Type.removeChild, 12);
    buffer.writeLong( parentNetID);
    buffer.writeInt( index);
    
    log.debugf( "UpdateProtocol.sendRemoveChild: parent=%s/%X, index=%d", parent.getType(), parentNetID, index);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
  }

  /**
   * Handle an update.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handleRemoveChild( Channel channel, ChannelBuffer buffer) throws IOException
  {
    long parentNetID = buffer.readLong();
    int index = buffer.readInt();

    log.debugf( "UpdateProtocol.handleRemoveChild: parent=%X, index=%d", parentNetID, index);
    
    protocol.dispatcher.execute( new RemoveChildEvent( channel, parentNetID, index));
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param element The element.
   * @param attrName The name of the attribute.
   * @param newValue The new value of the attribute.
   */
  public void sendChangeAttribute( Channel channel, IModelObject element, String attrName, Object newValue) throws IOException
  {
    long netID = protocol.serverCompressor.getLocalNetID( element);
    byte[] attrNameBytes = attrName.getBytes();
    
    ChannelBuffer buffer2 = ChannelBuffers.dynamicBuffer( attrLengthEstimate);
    int attrLength = protocol.serializer.writeObject( buffer2, newValue);
    
    ChannelBuffer buffer1 = protocol.headerProtocol.writeHeader( Type.changeAttribute, 8 + 1 + attrNameBytes.length + attrLength);
    buffer1.writeLong( netID);
    writeAttrName( attrName, buffer1);
    
    log.debugf( "UpdateProtocol.sendChangeAttribute: parent=%X, attrName=%s, attrValue=%s", netID, attrName, newValue);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
  }

  /**
   * Handle an update.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handleChangeAttribute( Channel channel, ChannelBuffer buffer) throws IOException, ClassNotFoundException
  {
    long netID = buffer.readLong();
    String attrName = readAttrName( buffer);
    Object newValue = protocol.serializer.readObject( buffer);

    log.debugf( "UpdateProtocol.handleChangeAttribute: element=%X, attrName=%s, attrValue=%s", netID, attrName, newValue);
    
    protocol.dispatcher.execute( new ChangeAttributeEvent( channel, netID, attrName, newValue));
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param element The element.
   * @param attrName The name of the attribute.
   */
  public void sendClearAttribute( Channel channel, IModelObject element, String attrName) throws IOException
  {
    long netID = protocol.serverCompressor.getLocalNetID( element);
    byte[] attrNameBytes = attrName.getBytes();
    
    ChannelBuffer buffer = protocol.headerProtocol.writeHeader( Type.changeAttribute, 8 + 1 + attrNameBytes.length);
    buffer.writeLong( netID);
    writeAttrName( attrName, buffer);
    
    log.debugf( "UpdateProtocol.sendClearAttribute: parent=%X, attrName=%s", netID, attrName);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
  }

  /**
   * Handle an update.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handleClearAttribute( Channel channel, ChannelBuffer buffer) throws IOException
  {
    long netID = buffer.readLong();
    String attrName = readAttrName( buffer);

    log.debugf( "UpdateProtocol.handleClearAttribute: element=%X, attrName=%s", netID, attrName);
    
    protocol.dispatcher.execute( new ClearAttributeEvent( channel, netID, attrName));
  }
  
  /**
   * Send an update.
   * @param channel The channel.
   * @param element The element.
   * @param dirty The dirty state.
   */
  public void sendChangeDirty( Channel channel, IModelObject element, boolean dirty) throws IOException
  {
    long netID = protocol.serverCompressor.getLocalNetID( element);
    
    ChannelBuffer buffer = protocol.headerProtocol.writeHeader( Type.changeAttribute, 8 + 1);
    buffer.writeLong( netID);
    buffer.writeByte( dirty? 1: 0);
    
    log.debugf( "UpdateProtocol.sendChangeDirty: element=%X, dirty=%s", netID, dirty);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
  }
  
  /**
   * Handle an update.
   * @param channel The channel.
   * @param buffer The buffer.
   */
  public void handleChangeDirty( Channel channel, ChannelBuffer buffer) throws IOException
  {
    long netID = buffer.readLong();
    boolean dirty = buffer.readByte() != 0;

    log.debugf( "UpdateProtocol.handleChangeDirty: element=%X, dirty=%s", netID, dirty);
    
    protocol.dispatcher.execute( new ChangeDirtyEvent( channel, netID, dirty));
  }
  
  /**
   * Process an event.
   * @param channel The channel.
   * @param parentNetID The parent network identifier.
   * @param child The child.
   * @param index The insertion index.
   */
  private void processAddChild( Channel channel, long parentNetID, IModelObject child, int index)
  {
    IModelObject parent = protocol.clientCompressor.findRemote( parentNetID);
    if ( parent == null)
    {
      log.errorf( "UpdateProtocol.processAddChild: parent %X not found", parentNetID);
      return;
    }
    
    parent.addChild( child, index);
  }

  /**
   * Process an event.
   * @param channel The channel.
   * @param parentNetID The parent network identifier.
   * @param index The removal index.
   */
  private void processRemoveChild( Channel channel, long parentNetID, int index)
  {
    IModelObject parent = protocol.clientCompressor.findRemote( parentNetID);
    if ( parent == null)
    {
      log.errorf( "UpdateProtocol.processRemoveChild: parent %X not found", parentNetID);
      return;
    }
    
    parent.removeChild( index);
  }

  /**
   * Process an event.
   * @param channel The channel.
   * @param netID The network identifier.
   * @param attrName The name of the attribute.
   * @param attrValue The new value of the attribute.
   */
  private void processChangeAttribute( Channel channel, long netID, String attrName, Object attrValue)
  {
    IModelObject element = protocol.clientCompressor.findRemote( netID);
    if ( element == null)
    {
      log.errorf( "UpdateProtocol.processChangeAttribute: element %X not found", netID);
      return;
    }
    
    element.setAttribute( attrName, attrValue);
  }
  
  /**
   * Process an event.
   * @param channel The channel.
   * @param netID The network identifier.
   * @param attrName The name of the attribute.
   */
  private void processClearAttribute( Channel channel, long netID, String attrName)
  {
    IModelObject element = protocol.clientCompressor.findRemote( netID);
    if ( element == null)
    {
      log.errorf( "UpdateProtocol.processClearAttribute: element %X not found", netID);
      return;
    }
    
    element.removeAttribute( attrName);
  }
  
  /**
   * Process an event.
   * @param channel The channel.
   * @param netID The network identifier.
   * @param dirty The new dirty state.
   */
  private void processChangeDirty( Channel channel, long netID, boolean dirty)
  {
    IModelObject element = protocol.clientCompressor.findRemote( netID);
    if ( element == null)
    {
      log.errorf( "UpdateProtocol.processChangeDirty: element %X not found", netID);
      return;
    }
    
    if ( !(element instanceof IExternalReference))
    {
      log.errorf( "UpdateProtocol.processChangeDirty: element %X is not a reference", netID);
      return;
    }
    
    ((IExternalReference)element).setDirty( dirty);
  }
  
  /**
   * Read an attribute name from the specified buffer.
   * @param buffer The buffer.
   * @return Returns the attribute name.
   */
  private String readAttrName( ChannelBuffer buffer)
  {
    int length = (int)buffer.readByte() & 0xFF;
    byte[] bytes = new byte[ length];
    buffer.readBytes( bytes);
    return new String( bytes);
  }
  
  /**
   * Write an attribute name to the specified buffer.
   * @param attrName The attribute name.
   * @param buffer The buffer.
   */
  private void writeAttrName( String attrName, ChannelBuffer buffer)
  {
    byte[] bytes = attrName.getBytes();
    buffer.writeByte( bytes.length);
    buffer.writeBytes( bytes);
  }
  
  private final class AddChildEvent implements Runnable
  {
    public AddChildEvent( Channel channel, long parentNetID, IModelObject child, int index)
    {
      this.channel = channel;
      this.parentNetID = parentNetID;
      this.child = child;
      this.index = index;
    }
    
    public void run()
    {
      processAddChild( channel, parentNetID, child, index);
    }
    
    private Channel channel;
    private long parentNetID;
    private IModelObject child;
    private int index;
  }
  
  private final class RemoveChildEvent implements Runnable
  {
    public RemoveChildEvent( Channel channel, long parentNetID, int index)
    {
      this.channel = channel;
      this.parentNetID = parentNetID;
      this.index = index;
    }
    
    public void run()
    {
      processRemoveChild( channel, parentNetID, index);
    }
    
    private Channel channel;
    private long parentNetID;
    private int index;
  }
  
  private final class ChangeAttributeEvent implements Runnable
  {
    public ChangeAttributeEvent( Channel channel, long netID, String attrName, Object attrValue)
    {
      this.channel = channel;
      this.netID = netID;
      this.attrName = attrName;
      this.attrValue = attrValue;
    }
    
    public void run()
    {
      processChangeAttribute( channel, netID, attrName, attrValue);
    }
    
    private Channel channel;
    private long netID;
    private String attrName;
    private Object attrValue;
  }
  
  private final class ClearAttributeEvent implements Runnable
  {
    public ClearAttributeEvent( Channel channel, long netID, String attrName)
    {
      this.channel = channel;
      this.netID = netID;
      this.attrName = attrName;
    }
    
    public void run()
    {
      processClearAttribute( channel, netID, attrName);
    }
    
    private Channel channel;
    private long netID;
    private String attrName;
  }
  
  private final class ChangeDirtyEvent implements Runnable
  {
    public ChangeDirtyEvent( Channel channel, long netID, boolean dirty)
    {
      this.channel = channel;
      this.netID = netID;
      this.dirty = dirty;
    }
    
    public void run()
    {
      processChangeDirty( channel, netID, dirty);
    }
    
    private Channel channel;
    private long netID;
    private boolean dirty;
  }
  
  private final static Log log = Log.getLog( UpdateProtocol.class);
  
  private BindProtocol protocol;
  private int attrLengthEstimate;
}
