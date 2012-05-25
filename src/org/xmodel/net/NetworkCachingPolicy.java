package org.xmodel.net;

import java.io.IOException;
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
    
    String host = Xlate.get( annotation, "host", Xlate.childGet( annotation, "host", "localhost"));
    int port = Xlate.get( annotation, "port", Xlate.childGet( annotation, "port", Server.defaultPort));
    timeout = Xlate.get( annotation, "timeout", Xlate.childGet(  annotation, "timeout", reconnectDelay));
    
    try
    {
      client = new Client( host, port, timeout, true);
    }
    catch( IOException e)
    {
      throw new CachingException( "Problem creating client.", e);
    }
    
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
    
    Session session = null;
    while( session == null)
    {
      try 
      { 
        session = client.connect( timeout);
        break;
      } 
      catch( Exception e) 
      { 
        SLog.error( this, e.getMessage());
      }
      
      try { Thread.sleep( reconnectDelay);} catch( InterruptedException e) { break;}
    }
    
    if ( session != null) 
    {
      try
      {
        session.attach( query, reference);
      }
      catch( IOException e)
      {
        throw new CachingException( String.format( 
          "Unable to attach to xpath, %s.", query), e);
      }
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

  private final static int reconnectDelay = 1000;
  
  private Client client;
  private String query;
  private int timeout;
}
