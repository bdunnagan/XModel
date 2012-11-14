package org.xmodel.net;

import java.io.IOException;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;

/**
 * An ICachingPolicy that accesses data across a network.
 */
public class NetKeyCachingPolicy extends AbstractCachingPolicy
{
<<<<<<< HEAD
  public NetKeyCachingPolicy( ProtocolOld protocol, ILink link, int session, Long key)
=======
  public NetKeyCachingPolicy( Protocol protocol, Channel link, int session, Long key)
>>>>>>> 23620e8975ab136320fc52595c9a3de09d487013
  {
    this( new UnboundedCache(), protocol);
    
    this.link = link;
    this.session = session;
    this.key = key;
  }
  
  public NetKeyCachingPolicy( ICache cache, ProtocolOld protocol)
  {
    super( cache);
    this.protocol = protocol;
    setStaticAttributes( new String[] { "id"});
    getDiffer().setMatcher( new DefaultXmlMatcher( true));
  }

  /**
   * Set the static attributes.
   * @param list The list of static attribute names.
   */
  public void setStaticAttributes( List<String> list)
  {
    setStaticAttributes( list.toArray( new String[ 0]));
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  @Override
  public void sync( IExternalReference reference) throws CachingException
  {
    try
    {
      // TODO: this needs to use the same timeout as root
      protocol.sendSyncRequest( link, session, Integer.MAX_VALUE, key, reference);
    }
    catch( IOException e)
    {
      throw new CachingException( "Unable to sync reference: "+reference, e);
    }
  }

<<<<<<< HEAD
  private ProtocolOld protocol;
  private ILink link;
=======
  private Protocol protocol;
  private Channel link;
>>>>>>> 23620e8975ab136320fc52595c9a3de09d487013
  private int session;
  private Long key;
}
