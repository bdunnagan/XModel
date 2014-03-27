package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import org.xmodel.future.AsyncFuture;
import org.xmodel.net.IXioChannel;
import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.XioPeer;
import org.xmodel.net.execution.ExecutionPrivilege;
import org.xmodel.xpath.expression.IContext;

public class AmqpXioPeer extends XioPeer
{
  public AmqpXioPeer( 
      IXioChannel channel, 
      IXioPeerRegistry registry, 
      IContext context, 
      Executor executor, 
      ScheduledExecutorService scheduler,
      ExecutionPrivilege privilege)
  {
    super( channel, registry, context, executor, scheduler, privilege);

    registerChannel = (AmqpXioChannel)channel;

    this.executor = executor;
    this.scheduler = scheduler;
    this.privilege = privilege;
  }
  
  /**
   * Create a new channel for the specified registration name.
   * @param qName The qualified name with which the endpoint registered.
   * @return Returns a new channel.
   */
  public AmqpXioPeer deriveRegisteredPeer( String qName) throws IOException
  {
    AmqpXioChannel channel = (AmqpXioChannel)getChannel();
    
    String name = AmqpQualifiedNames.parseRegistrationName( qName);
    AmqpXioChannel newChannel = channel.deriveRegisteredChannel();
    newChannel.setOutputQueue( AmqpQueueNames.getOutputQueue( qName), false, true);
    
    AmqpXioPeer peer = new AmqpXioPeer( newChannel, getPeerRegistry(), getNetworkEventContext(), executor, scheduler, privilege);
    peer.qualifiedName = qName;
    newChannel.setPeer( peer);

    class TimeoutTask implements AsyncFuture.IListener<AmqpXioPeer>
    {
      public TimeoutTask( AmqpXioPeer peer) { this.peer = peer;}
      public void notifyComplete( AsyncFuture<AmqpXioPeer> future) throws Exception { peer.close();}
      private AmqpXioPeer peer;
    }
    
    newChannel.startConsumer( AmqpQueueNames.getInputQueue( name), false, true);
    newChannel.startHeartbeatTimeout().addListener( new TimeoutTask( peer));
    
    return peer;
  }

  /**
   * Set the subscription channel for this peer.
   * @param channel The channel.
   */
  public void setSubscribeChannel( AmqpXioChannel channel)
  {
    super.setChannel( channel);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#register(java.lang.String)
   */
  @Override
  public void register( String name) throws IOException, InterruptedException
  {
    if ( registerChannel == null) throw new IllegalStateException( "Peer is not connected.");
    
    // send qualified names to the server
    // TODO: this only supports one name registration
    qualifiedName = AmqpQualifiedNames.createQualifiedName( name);
    registerProtocol.registerRequestProtocol.send( registerChannel, qualifiedName);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#unregister(java.lang.String)
   */
  @Override
  public void unregister( String name) throws IOException, InterruptedException
  {
    if ( registerChannel == null) throw new IllegalStateException( "Peer is not connected.");
    
    // send qualified names to the server
    registerProtocol.unregisterRequestProtocol.send( registerChannel, AmqpQualifiedNames.createQualifiedName( name));
  }
  
  /**
   * Register again with the same qualified name.
   */
  public void reregister() throws IOException, InterruptedException
  {
    if ( registerChannel == null) throw new IllegalStateException( "Peer is not connected.");
    
    // send qualified names to the server
    registerProtocol.registerRequestProtocol.send( registerChannel, qualifiedName);
  }

  /**
   * Send an echo-request message on the specified channel.
   * @param channel The channel.
   */
  public void heartbeat( AmqpXioChannel channel) throws IOException
  {
    echoProtocol.requestProtocol.send( channel);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return qualifiedName;
  }

  private AmqpXioChannel registerChannel;
  private String qualifiedName;
  private Executor executor;
  private ScheduledExecutorService scheduler;
  private ExecutionPrivilege privilege;
}
