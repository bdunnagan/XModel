package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
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
    newChannel.declareOutputQueue( AmqpQueueNames.getOutputQueue( name), false, true);
    
    AmqpXioPeer peer = new AmqpXioPeer( newChannel, getPeerRegistry(), getNetworkEventContext(), executor, scheduler, privilege);
    peer.qualifiedName = qName;
    newChannel.setPeer( peer);
    
    newChannel.startConsumer( AmqpQueueNames.getInputQueue( name), false, true);
    newChannel.startHeartbeat( 9000, false);
    
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

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    if ( !(object instanceof AmqpXioPeer)) return false;
    
    AmqpXioPeer other = (AmqpXioPeer)object;
    if ( qualifiedName == null && qualifiedName != other.qualifiedName) return false;
    if ( qualifiedName != null && other.qualifiedName != null && !qualifiedName.equals( other.qualifiedName)) return false;
    
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#hashCode()
   */
  @Override
  public int hashCode()
  {
    return (qualifiedName == null)? System.identityHashCode( this): qualifiedName.hashCode();
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
