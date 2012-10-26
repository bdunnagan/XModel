package org.xmodel.net.bind;

import org.xmodel.IDispatcher;
import org.xmodel.compress.DefaultSerializer;
import org.xmodel.compress.ISerializer;
import org.xmodel.net.nu.ErrorProtocol;
import org.xmodel.net.nu.HeaderProtocol;
import org.xmodel.xpath.expression.IContext;

public class BindProtocol
{
  public BindProtocol( HeaderProtocol headerProtocol, ErrorProtocol errorProtocol, IContext context, IDispatcher dispatcher)
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
    this.errorProtocol = errorProtocol;
    this.clientCompressor = BindCompressor.newClientCompressor( this, true);
    this.serverCompressor = BindCompressor.newServerCompressor( true);
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
  public ErrorProtocol errorProtocol;
  public BindCompressor clientCompressor;
  public BindCompressor serverCompressor;
}
