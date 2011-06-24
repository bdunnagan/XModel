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
    root = true;
    setStaticAttributes( new String[] { "id"});
    getDiffer().setMatcher( new DefaultXmlMatcher( true));
  }

  /**
   * @return Returns a NetworkCachingPolicy for nested references.
   */
  public NetworkCachingPolicy getNested()
  {
    NetworkCachingPolicy cachingPolicy = new NetworkCachingPolicy();
    cachingPolicy.root = false;
    cachingPolicy.client = client;
    return cachingPolicy;
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
    
    try
    {
      client = new Client( host, port);
    }
    catch( IOException e)
    {
      throw new CachingException( "Illegal host or port specification.", e);
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
    try
    {
      if ( root)
      {
        if ( xpath == null)
        {
          xpath = Xlate.get( reference, "xpath", (String)null);
          if ( xpath == null) return;
        }
        
        client.attach( xpath, reference);
      }
      else
      {
        client.sync( reference);
      }
      
      if ( error != null) throw new CachingException( error);
    }
    catch( IOException e)
    {
      throw new CachingException( "Unable to sync reference: "+reference, e);
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
  
  private boolean root;
  private Client client;
  private String xpath;
  private String error;
}
