package org.xmodel.net.nu;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.Xlate;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.log.SLog;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;

/**
 * An ICachingPolicy that accesses data across a network.
 */
public class NetworkCachingPolicy extends ConfiguredCachingPolicy
{
  public NetworkCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  public NetworkCachingPolicy( ICache cache)
  {
    super( cache);
    
    setStaticAttributes( new String[] { "id"});
    getDiffer().setMatcher( new DefaultXmlMatcher( true));
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    if ( client != null) 
    {
      try { client.disconnect();} catch( Exception e) {}
      client = null;
    }
    
    super.finalize();
  }

  /**
   * @return Returns the client used to communicate with the remote host.
   */
  public Client getClient()
  {
    return client;
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
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    host = Xlate.get( annotation, "host", Xlate.childGet( annotation, "host", "localhost"));
    port = Xlate.get( annotation, "port", Xlate.childGet( annotation, "port", Server.defaultPort));
    timeout = Xlate.get( annotation, "timeout", Xlate.childGet(  annotation, "timeout", 30000));
    
    readonly = Xlate.get( annotation, "readonly", Xlate.childGet( annotation, "readonly", false));
    query = Xlate.get( annotation, "query", Xlate.childGet( annotation, "query", "."));
    validate( query);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    if ( query == null)
    {
      query = Xlate.get( reference, "query", (String)null);
      if ( query == null) throw new CachingException( "Query not defined.");
    }
    
    SLog.debugf( this, "sync: %s:%d, %s", host, port, reference);
    
    if ( client != null && client.isConnected())
      try { client.disconnect();} catch( Exception e) {}
    
    try
    {
      client = new Client( host, port, true);
      client.setDispatcher( reference.getModel().getDispatcher());
      client.setPingTimeout( timeout);
      
      Session session = client.connect( timeout);
      if ( session != null) session.attach( timeout, query, pubsub, reference);
    }
    catch( Exception e)
    {
      throw new CachingException( String.format( 
          "Unable to attach to xpath, '%s'", query), e);
    }
  }

  /**
   * Validate the specified expression.
   * @param xpath The xpath expression.
   */
  private void validate( String xpath)
  {
    try
    {
      XPath.compileExpression( xpath);
    }
    catch( PathSyntaxException e)
    {
      String message = String.format( "Error in remote expression: %s\n", xpath);
      throw new CachingException( message, e);
    }
  }
  
  private String host;
  private int port;
  private boolean readonly;
  private String query;
  private int timeout;
}
