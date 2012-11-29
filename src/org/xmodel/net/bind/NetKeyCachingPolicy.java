package org.xmodel.net.bind;

import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;

/**
 * A caching policy for elements belonging to a remote bound model whose peers are external references.
 */
class NetKeyCachingPolicy extends AbstractCachingPolicy
{
  public NetKeyCachingPolicy( BindProtocol protocol, Channel channel, int netID, int timeout, String[] statics)
  {
    this( new UnboundedCache(), protocol, channel, netID, timeout, statics);
  }
  
  public NetKeyCachingPolicy( ICache cache, BindProtocol protocol, Channel channel, int netID, int timeout, String[] statics)
  {
    super( cache);
    
    this.protocol = protocol;
    this.channel = channel;
    this.netID = netID;
    this.timeout = timeout;
    
    setStaticAttributes( statics);
    getDiffer().setMatcher( new DefaultXmlMatcher( true));
  }
  
  /**
   * @return Returns the network identifier.
   */
  public int getNetID()
  {
    return netID;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  @Override
  public void sync( IExternalReference reference) throws CachingException
  {
    try
    {
      IModelObject element = protocol.syncRequestProtocol.send( channel, netID, timeout);
      if ( element == null) 
      {
        throw new CachingException( String.format( 
            "Unable to sync network reference: netID=%X", netID));
      }
      
      update( reference, element);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to sync reference: "+reference, e);
    }
  }

  private BindProtocol protocol;
  private Channel channel;
  private int netID;
  private int timeout;
}
