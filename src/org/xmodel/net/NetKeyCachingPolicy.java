package org.xmodel.net;

import java.io.IOException;
import java.util.List;

import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;

/**
 * An ICachingPolicy that accesses data across a network.
 */
class NetKeyCachingPolicy extends AbstractCachingPolicy
{
  public NetKeyCachingPolicy( Protocol protocol)
  {
    this( new UnboundedCache(), protocol);
  }
  
  public NetKeyCachingPolicy( ICache cache, Protocol protocol)
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
      protocol.sendSyncRequest( reference);
    }
    catch( IOException e)
    {
      throw new CachingException( "Unable to sync reference: "+reference, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  @Override
  public void flush( IExternalReference reference) throws CachingException
  {
  }

  private Protocol protocol;
}
