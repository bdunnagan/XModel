package org.xmodel.net.bind;

import org.xmodel.IDispatcher;
import org.xmodel.net.nu.ErrorProtocol;
import org.xmodel.net.nu.HeaderProtocol;
import org.xmodel.xpath.expression.IContext;

public class BindProtocol
{
  public BindProtocol( HeaderProtocol headerProtocol, ErrorProtocol errorProtocol, IContext context, IDispatcher dispatcher)
  {
    this.context = context;
    this.dispatcher = dispatcher;
    this.headerProtocol = headerProtocol;
    this.syncRequestProtocol = new SyncRequestProtocol( this);
    this.syncResponseProtocol = new SyncResponseProtocol( this);
    this.bindRequestProtocol = new BindRequestProtocol( this);
    this.errorProtocol = errorProtocol;
    this.upstreamCompressor = BindCompressor.newUpstreamCompressor( true);
    this.downstreamCompressor = BindCompressor.newDownstreamCompressor( this, true);
  }
  
  public IContext context;
  public IDispatcher dispatcher;
  public HeaderProtocol headerProtocol;
  public BindRequestProtocol bindRequestProtocol;
  public BindResponseProtocol bindResponseProtocol;
  public SyncRequestProtocol syncRequestProtocol;
  public SyncResponseProtocol syncResponseProtocol;
  public ErrorProtocol errorProtocol;
  public BindCompressor upstreamCompressor;
  public BindCompressor downstreamCompressor;
}
