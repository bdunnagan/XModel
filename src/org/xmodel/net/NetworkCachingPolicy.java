package org.xmodel.net;

import java.util.ArrayList;
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
import org.xmodel.xpath.expression.StatefulContext;

/**
 * An ICachingPolicy that accesses data across a network.
 */
public class NetworkCachingPolicy extends ConfiguredCachingPolicy
{
  public final static int defaultPort = 27600;
  
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
    close();
    super.finalize();
  }

  /**
   * Close the network connection.
   */
  public void close()
  {
    if ( client != null && client.isConnected())
      client.close();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    host = Xlate.get( annotation, "host", Xlate.childGet( annotation, "host", "localhost"));
    port = Xlate.get( annotation, "port", Xlate.childGet( annotation, "port", defaultPort));
    timeout = Xlate.get( annotation, "timeout", Xlate.childGet(  annotation, "timeout", 30000));
    retryCount = Xlate.get( annotation, "retryCount", Xlate.childGet(  annotation, "retryCount", 3));
    retryDelays = parseRetryDelay( Xlate.get( annotation, "retryDelays", Xlate.childGet(  annotation, "retryDelays", "500, 1000, 3000, 5000")));
    
    readonly = Xlate.get( annotation, "readonly", Xlate.childGet( annotation, "readonly", false));
    query = Xlate.get( annotation, "query", Xlate.childGet( annotation, "query", "."));
    validate( query);
  }
  
  /**
   * Parse the retry delay spec.
   * @param spec Comma-separated list of retry delays.
   * @return Returns an array of the retry delays.
   */
  private int[] parseRetryDelay( String spec)
  {
    List<Integer> list = new ArrayList<Integer>( 5);
    String[] items = spec.split(  "\\s*+,\\s*+");
    for( int i=0; i<items.length; i++)
    {
      try
      {
        list.add( Integer.parseInt( items[ i]));
      }
      catch( NumberFormatException e)
      {
        SLog.errorf( this, "Problem parsing retry delay spec, %s", spec);
      }
    }
    
    int[] array = new int[ list.size()];
    for( int i=0; i<array.length; i++)
      array[ i] = list.get( i);
    return array;
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
  
  /**
   * Set the remote network identifier for this caching policy. 
   * @param netID The network identifier.
   */
  public void setRemoteNetID( int netID)
  {
    this.netID = netID;
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
    
    try
    {
      if ( client == null || !client.isConnected())
      {
        StatefulContext context = new StatefulContext();
        context.getModel();
        client = new XioClient( context);
        client.connect( host, port, retryCount, retryDelays).await();
      }

      client.bind( reference, readonly, query, timeout);
    }
    catch( Exception e)
    {
      throw new CachingException( String.format( 
          "Unable to attach to xpath, '%s'", query), e);
    }
  }

  private XioClient client;
  private String host;
  private int port;
  private boolean readonly;
  private String query;
  private int timeout;
  private int retryCount;
  private int[] retryDelays;
  @SuppressWarnings("unused")
  private int netID;
}
