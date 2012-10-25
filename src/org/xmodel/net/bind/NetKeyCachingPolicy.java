package org.xmodel.net.bind;

import java.io.IOException;
import org.jboss.netty.channel.Channel;
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
  public NetKeyCachingPolicy( BindProtocol protocol, Channel channel, long key, int timeout, String[] statics)
  {
    this( new UnboundedCache(), protocol, channel, key, timeout, statics);
  }
  
  public NetKeyCachingPolicy( ICache cache, BindProtocol protocol, Channel channel, long key, int timeout, String[] statics)
  {
    super( cache);
    
    this.protocol = protocol;
    this.channel = channel;
    this.key = key;
    this.timeout = timeout;
    
    setStaticAttributes( statics);
    getDiffer().setMatcher( new DefaultXmlMatcher( true));
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  @Override
  public void sync( IExternalReference reference) throws CachingException
  {
    try
    {
      protocol.syncRequestProtocol.send( channel, key, reference, timeout);
    }
    catch( IOException e)
    {
      throw new CachingException( "Unable to sync reference: "+reference, e);
    }
  }

  private BindProtocol protocol;
  private Channel channel;
  private long key;
  private int timeout;
}
