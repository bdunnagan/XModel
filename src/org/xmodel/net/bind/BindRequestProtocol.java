package org.xmodel.net.bind;

import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.ErrorProtocol;
import org.xmodel.net.nu.FullProtocolChannelHandler.Type;
import org.xmodel.net.nu.HeaderProtocol;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;

public class BindRequestProtocol
{
  public BindRequestProtocol( BindProtocol bundle)
  {
    this.bundle = bundle;
    this.listeners = new HashMap<IModelObject, UpdateListener>();
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
   * @param channel The channel.
   * @param readonly True if binding should be readonly.
   * @param query The query to bind.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the query result.
   */
  public IModelObject send( Channel channel, boolean readonly, String query, int timeout) throws InterruptedException
  {
    int correlation = responseProtocol.nextCorrelation();
    log.debugf( "BindRequestProtocol.send (sync): corr=%d, timeout=%d, readonly=%s, query=%s", correlation, timeout, readonly, query);
    
    byte[] queryBytes = query.getBytes();
    
    ChannelBuffer buffer = headerProtocol.writeHeader( Type.bindRequest, 5 + queryBytes.length);
    buffer.writeInt( correlation);
    buffer.writeByte( readonly? 1: 0);
    buffer.writeBytes( queryBytes);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write( buffer);
    
    return bundle.bindResponseProtocol.waitForResponse( correlation, timeout);
  }
  
  /**
   * Handle a bind request.
   * @param channel The channel.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  public void handle( Channel channel, ChannelBuffer buffer, long length)
  {
    int correlation = buffer.readInt();
    boolean readonly = buffer.readByte() == 1;
    
    byte[] queryBytes = new byte[ (int)length - 5];
    buffer.readBytes( queryBytes);
    
    bundle.dispatcher.execute( new BindRunnable( channel, correlation, readonly, new String( queryBytes)));
  }
  
  /**
   * Bind the specified query.
   * @param channel The channel.
   * @param correlation The correlation.
   * @param readonly True if the readonly binding is requested.
   * @param query The path to bind.
   */
  private void bind( Channel channel, int correlation, boolean readonly, String queryText)
  {
    try
    {
      IExpression query = XPath.compileExpression( queryText);
      IModelObject target = query.queryFirst( bundle.context);
      if ( target != null)
      {
        responseProtocol.send( channel, correlation, target);
        
        UpdateListener listener = new UpdateListener( channel, queryText, target);
        listener.install( target);
        listeners.put( target, listener);
      }
      else
      {
        responseProtocol.send( channel, correlation, null);
      }
    }
    catch( Exception e)
    {
      SLog.exception( this, e);
      errorProtocol.sendError( channel, correlation, String.format( "Invalid query: {%s}", queryText));
    }
  }
  
  private class BindRunnable implements Runnable
  {
    public BindRunnable( Channel channel, int correlation, boolean readonly, String query)
    {
      this.channel = channel;
      this.correlation = correlation;
      this.readonly = readonly;
      this.query = query;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      bind( channel, correlation, readonly, query);
    }
    
    private Channel channel;
    private int correlation;
    private boolean readonly;
    private String query;
  }
  
  private final static Log log = Log.getLog( BindRequestProtocol.class);

  private BindProtocol bundle;
  private HeaderProtocol headerProtocol;
  private BindResponseProtocol responseProtocol;
  private ErrorProtocol errorProtocol;
  private Map<IModelObject, UpdateListener> listeners;
}
