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

    this.executor = executor;
    this.scheduler = scheduler;
    this.privilege = privilege;
  }
  
  /**
   * Create a new channel for the specified registration name.
   * @param name The name with which the endpoint registered.
   * @return Returns a new channel.
   */
  public AmqpXioPeer deriveRegisteredPeer( String name) throws IOException
  {
    AmqpXioChannel channel = (AmqpXioChannel)getChannel();
    AmqpXioChannel newChannel = channel.deriveRegisteredChannel( AmqpQualifiedNames.parseRegistrationName( name));
    AmqpXioPeer peer = new AmqpXioPeer( newChannel, getPeerRegistry(), getNetworkEventContext(), executor, scheduler, privilege);
    peer.qualifiedName = name;
    return peer;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#register(java.lang.String)
   */
  @Override
  public void register( String name) throws IOException, InterruptedException
  {
    // send qualified names to the server
    super.register( AmqpQualifiedNames.createQualifiedName( name));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.XioPeer#unregister(java.lang.String)
   */
  @Override
  public void unregister( String name) throws IOException, InterruptedException
  {
    // send qualified names to the server
    super.unregister( AmqpQualifiedNames.createQualifiedName( name));
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

  private String qualifiedName;
  private Executor executor;
  private ScheduledExecutorService scheduler;
  private ExecutionPrivilege privilege;
}
