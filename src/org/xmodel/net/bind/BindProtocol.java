package org.xmodel.net.bind;

import org.xmodel.IDispatcher;
import org.xmodel.compress.DefaultSerializer;
import org.xmodel.compress.ISerializer;
import org.xmodel.net.nu.HeaderProtocol;
import org.xmodel.xpath.expression.IContext;

public class BindProtocol
{
  public BindProtocol( HeaderProtocol headerProtocol, IContext context, IDispatcher dispatcher)
  {
    this.context = context;
    this.dispatcher = dispatcher;
    this.serializer = new DefaultSerializer();
    this.headerProtocol = headerProtocol;
    this.bindRequestProtocol = new BindRequestProtocol( this);
    this.bindResponseProtocol = new BindResponseProtocol( this);
    this.unbindRequestProtocol = new UnbindRequestProtocol( this);
    this.syncRequestProtocol = new SyncRequestProtocol( this);
    this.syncResponseProtocol = new SyncResponseProtocol( this);
    this.updateProtocol = new UpdateProtocol( this);
    this.clientCompressor = BindCompressor.newClientCompressor( this, true);
    this.serverCompressor = BindCompressor.newServerCompressor( true);
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    headerProtocol.reset();
    bindRequestProtocol.reset();
    bindResponseProtocol.reset();
    unbindRequestProtocol.reset();
    syncRequestProtocol.reset();
    syncResponseProtocol.reset();
    updateProtocol.reset();
    clientCompressor.reset();
    serverCompressor.reset();
  }
  
  public IContext context;
  public IDispatcher dispatcher;
  public ISerializer serializer;
  public HeaderProtocol headerProtocol;
  public BindRequestProtocol bindRequestProtocol;
  public BindResponseProtocol bindResponseProtocol;
  public UnbindRequestProtocol unbindRequestProtocol;
  public SyncRequestProtocol syncRequestProtocol;
  public SyncResponseProtocol syncResponseProtocol;
  public UpdateProtocol updateProtocol;
  public BindCompressor clientCompressor;
  public BindCompressor serverCompressor;
}
