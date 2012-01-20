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
import org.xmodel.log.Log;
import org.xmodel.xml.XmlIO;
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
    
    XmlIO xmlIO = new XmlIO();
    String host = Xlate.get( annotation, "host", Xlate.childGet( annotation, "host", (String)null));
    if ( host == null) 
    {
      String xml = xmlIO.write( annotation);
      throw new CachingException( "Host not defined in annotation: \n"+xml);
    }
    
    int port = Xlate.get( annotation, "port", Xlate.childGet( annotation, "port", 0));
    if ( port == 0)
    {
      String xml = xmlIO.write( annotation);
      throw new CachingException( "Port not defined in annotation: \n"+xml);
    }
    
    timeout = Xlate.get( annotation, "timeout", Xlate.childGet(  annotation, "timeout", reconnectDelay));
    
    try
    {
      client = new Client( host, port, timeout, true);
    }
    catch( IOException e)
    {
      throw new CachingException( "Problem creating client.", e);
    }
    
    xpath = Xlate.get( annotation, "xpath", Xlate.childGet( annotation, "xpath", (String)null));
    if ( xpath != null) validate( xpath);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    if ( xpath == null)
    {
      xpath = Xlate.get( reference, "xpath", (String)null);
      if ( xpath == null) throw new CachingException( "Query not defined.");
    }
    
    Session session = null;
    while( session == null)
    {
      try { session = client.connect( timeout);} catch( Exception e) { log.error( e.getMessage());}
      try { Thread.sleep( reconnectDelay);} catch( InterruptedException e) { break;}
      if ( session != null)
      {
        try { session.attach( xpath, reference);} catch( Exception e) { log.error( e.getMessage());}
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#flushImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void flushImpl( IExternalReference reference) throws CachingException
  {
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

  private final static Log log = Log.getLog( NetworkCachingPolicy.class);
  private final static int reconnectDelay = 1000;
  
  private Client client;
  private String xpath;
  private int timeout;
}
