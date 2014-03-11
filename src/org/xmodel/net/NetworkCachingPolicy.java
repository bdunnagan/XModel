package org.xmodel.net;

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
import org.xmodel.xpath.expression.IExpression;

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
  
  /**
   * @deprecated Close the backing client, instead.
   */
  public void close()
  {
    throw new UnsupportedOperationException();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    // save context to access executor
    this.context = context;
    
    clientExpr = Xlate.get( annotation, "client", Xlate.childGet( annotation, "client", (IExpression)null));
    if ( clientExpr == null) throw new CachingException( "Client expression not defined.");

    timeoutExpr = Xlate.get( annotation, "timeout", Xlate.childGet( annotation, "timeout", (IExpression)null));
    readonlyExpr = Xlate.get( annotation, "readonly", Xlate.childGet( annotation, "readonly", (IExpression)null));
    queryExpr = Xlate.get( annotation, "query", Xlate.childGet( annotation, "query", (IExpression)null));
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
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String query = queryExpr.evaluateString( context);
    int timeout = (timeoutExpr != null)? (int)timeoutExpr.evaluateNumber( context): 30000;
    boolean readonly = (readonlyExpr != null)? readonlyExpr.evaluateBoolean( context): true;
    
    if ( query == null)
    {
      query = Xlate.get( reference, "query", (String)null);
      if ( query == null) throw new CachingException( "Query not defined.");
    }
    
    validate( query);
    
    IModelObject clientElement = clientExpr.queryFirst( context);
    XioPeer client = (XioPeer)clientElement.getValue();
    
    SLog.debugf( this, "sync: %s, %s", client, reference);
    
    try
    {
      client.bind( reference, readonly, query, timeout);
    }
    catch( Exception e)
    {
      throw new CachingException( String.format( 
          "Unable to attach to xpath, '%s'", query), e);
    }
  }

  private IContext context;
  private IExpression clientExpr;
  private IExpression queryExpr;
  private IExpression readonlyExpr;
  private IExpression timeoutExpr;
}
