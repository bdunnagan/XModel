package org.xmodel.net.bind;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.IModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.external.IExternalReference;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.net.HeaderProtocol.Type;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.XioException;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;

public class BindRequestProtocol
{
  public BindRequestProtocol( BindProtocol bundle)
  {
    this.bundle = bundle;
    this.listeners = new HashMap<IModelObject, UpdateListener>();
    this.bindings = new ArrayList<IExternalReference>();
  }
  
  /**
   * Reset this instance by releasing internal resources.  This method should be called after 
   * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
   */
  public synchronized void reset()
  {
    for( Map.Entry<IModelObject, UpdateListener> entry: listeners.entrySet())
      bundle.executor.execute( new UninstallListenerRunnable( entry.getKey(), entry.getValue())); 
    listeners.clear();
    
    for( IExternalReference binding: bindings)
      bundle.executor.execute( new SetDirtyRunnable( binding));
    
    bindings.clear();
  }
  
  /**
   * Returns the UpdateListener installed on the specified element.
   * @param element The element.
   * @return Returns null or the listener.
   */
  public synchronized UpdateListener getListener( IModelObject element)
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
   * @return Returns false if a timeout occurs.
   */
  public boolean send( IExternalReference reference, IXioChannel channel, boolean readonly, String query, int timeout) throws InterruptedException
  {
    long correlation = bundle.bindResponseProtocol.nextCorrelation( reference);
    log.debugf( "BindRequestProtocol.send (sync): corr=%d, timeout=%d, readonly=%s, query=%s", correlation, timeout, readonly, query);
    
    byte[] queryBytes = query.getBytes( charset);
    
    ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 1 + queryBytes.length, Type.bindRequest, correlation);
    buffer.writeByte( readonly? 1: 0);
    buffer.writeBytes( queryBytes);
    
    // ignoring write buffer overflow for this type of messaging
    channel.write(buffer);
    
    // wait for response from server
    IModelObject received = bundle.bindResponseProtocol.waitForResponse( correlation, timeout);
    if ( received == null) return false;

    // TODO: Updating the reference requires changing the remote index in the compressor,
    //       so just remove children and repopulate for now.
    reference.removeChildren();
    reference.getCachingPolicy().update( reference, received);

    // optionally install listeners for bidirectional binding
    UpdateListener listener = null;
    if ( !readonly)
    {
      listener = new UpdateListener( bundle.updateProtocol, channel, query);
      listener.install( reference);
    }

    // save stuff for cleanup when connection is closed
    synchronized( this)
    {
      bindings.add( reference);
      if ( listener != null) listeners.put( reference, listener);
    }
    
    return true;
  }
  
  /**
   * Handle a bind request.
   * @param channel The channel.
   * @param buffer The buffer.
   * @param length The length of the message.
   * @param correlation The correlation number.
   */
  public void handle( IXioChannel channel, ChannelBuffer buffer, long length, long correlation) throws XioException
  {
    boolean readonly = buffer.readByte() == 1;
    
    byte[] queryBytes = new byte[ (int)length - 5];
    buffer.readBytes( queryBytes);
    
    String query = new String( queryBytes, charset);
    try
    {
      IExpression queryExpr = XPath.compileExpression( new String( queryBytes, charset));
      Executor executor = bundle.context.getExecutor();
      executor.execute( new BindRunnable( channel, correlation, readonly, query, queryExpr));
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
  private void bind( IXioChannel channel, long correlation, boolean readonly, String query, IExpression queryExpr)
  {
    synchronized( bundle.context)
    {
      try
      {
        IModelObject target = (bundle.context != null)? queryExpr.queryFirst( bundle.context): null;
        bundle.bindResponseProtocol.send( channel, correlation, target);
        
        if ( target != null)
        {
          UpdateListener listener = new UpdateListener( bundle.updateProtocol, channel, query);
          listener.install( target);
          synchronized( this) { listeners.put( target, listener);}
        }
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
      }
    }
  }
  
  private class BindRunnable implements Runnable
  {
    public BindRunnable( IXioChannel channel, long correlation, boolean readonly, String query, IExpression queryExpr)
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
    
    private IXioChannel channel;
    private long correlation;
    private boolean readonly;
    private String query;
    private IExpression queryExpr;
  }
  
  private static class SetDirtyRunnable implements Runnable
  {
    public SetDirtyRunnable( IExternalReference reference)
    {
      this.reference = reference;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      // TODO: locking is incorrect, so don't attempt to set dirty yet.
      //reference.setDirty( true);
    }

    private IExternalReference reference;
  }
  
  private static class UninstallListenerRunnable implements Runnable
  {
    public UninstallListenerRunnable( IModelObject element, UpdateListener listener)
    {
      this.element = element;
      this.listener = listener;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      listener.uninstall( element);
    }

    private IModelObject element;
    private UpdateListener listener;
  }
  
  private final static Log log = Log.getLog( BindRequestProtocol.class);
  private final static Charset charset = Charset.forName( "UTF-8");

  private BindProtocol bundle;
  private Map<IModelObject, UpdateListener> listeners;
  private List<IExternalReference> bindings;
}
