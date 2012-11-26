package org.xmodel.net.bind;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.XioChannelHandler.Type;
import org.xmodel.net.XioException;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;

public class BindRequestProtocol
{
  public BindRequestProtocol( BindProtocol bundle)
  {
    this.bundle = bundle;
    this.listeners = new ConcurrentHashMap<IModelObject, UpdateListener>();
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public void reset()
  {
    for( Map.Entry<IModelObject, UpdateListener> entry: listeners.entrySet())
      entry.getValue().uninstall( entry.getKey());
    listeners.clear();
  }
  
  /**
   * Returns the UpdateListener installed on the specified element.
   * @param element The element.
   * @return Returns null or the listener.
   */
  public UpdateListener getListener( IModelObject element)
  {
    return listeners.get( element);
  }
  
  /**
   * Send a bind request.
   * @param reference The reference being remotely bound.
   * @param channel The channel.
   * @param readonly True if binding should be readonly.
   * @param query The query to bind.
   * @param timeout The timeout in milliseconds.
   */
  public void send( IExternalReference reference, Channel channel, boolean readonly, String query, int timeout) throws InterruptedException
  {
    int correlation = bundle.bindResponseProtocol.nextCorrelation( reference);
    log.debugf( "BindRequestProtocol.send (sync): corr=%d, timeout=%d, readonly=%s, query=%s", correlation, timeout, readonly, query);
    
    byte[] queryBytes = query.getBytes();
    
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 5 + queryBytes.length, Type.bindRequest, 0);
    buffer.writeInt( correlation);
    buffer.writeByte( readonly? 1: 0);
    buffer.writeBytes( queryBytes);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
    
    bundle.bindResponseProtocol.waitForResponse( correlation, timeout);
  }
  
  /**
   * Handle a bind request.
   * @param channel The channel.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  public void handle( Channel channel, ChannelBuffer buffer, long length) throws XioException
  {
    int correlation = buffer.readInt();
    boolean readonly = buffer.readByte() == 1;
    
    byte[] queryBytes = new byte[ (int)length - 5];
    buffer.readBytes( queryBytes);
    
    String query = new String( queryBytes);
    try
    {
      IExpression queryExpr = XPath.compileExpression( new String( queryBytes));
      bundle.context.getModel().dispatch( new BindRunnable( channel, correlation, readonly, query, queryExpr));
    }
    catch( PathSyntaxException e)
    {
      throw new XioException( String.format( "Invalid query: {%s}", query), e);
    }
  }
  
  /**
   * Bind the specified query.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param readonly True if the readonly binding is requested.
   * @param query The expression string.
   * @param queryExpr The compiled expression.
   */
  private void bind( Channel channel, int correlation, boolean readonly, String query, IExpression queryExpr)
  {
    try
    {
      bundle.context.getModel().writeLockUninterruptibly();
      
      IModelObject target = (bundle.context != null)? queryExpr.queryFirst( bundle.context): null;
      bundle.bindResponseProtocol.send( channel, correlation, target);
      
      if ( target != null)
      {
        UpdateListener listener = new UpdateListener( bundle.updateProtocol, channel, query);
        listener.install( target);
        listeners.put( target, listener);
      }
    }
    catch( IOException e)
    {
      SLog.exception( this, e);
    }
    finally
    {
      bundle.context.getModel().writeUnlock();
    }
  }
  
  private class BindRunnable implements Runnable
  {
    public BindRunnable( Channel channel, int correlation, boolean readonly, String query, IExpression queryExpr)
    {
      this.channel = channel;
      this.correlation = correlation;
      this.readonly = readonly;
      this.query = query;
      this.queryExpr = queryExpr;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      bind( channel, correlation, readonly, query, queryExpr);
    }
    
    private Channel channel;
    private int correlation;
    private boolean readonly;
    private String query;
    private IExpression queryExpr;
  }
  
  private final static Log log = Log.getLog( BindRequestProtocol.class);

  private BindProtocol bundle;
  private Map<IModelObject, UpdateListener> listeners;
}
