package org.xmodel.net.nu;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.xmodel.IModel;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class that implements the server-side of the network caching policy protocol.
 */
public class CachingServer
{
  /**
   * Create a server bound to the specified local address and port.
   * @param host The local address.
   * @param port The local port.
   * @param model The model.
   */
  public CachingServer( String host, String port, IModel model)
  {
    this.model = model;
    map = new WeakHashMap<String, IExternalReference>();
    exprMap = new HashMap<String, IExpression>();
  }
  
  /**
   * Set the context in which remote xpath expressions will be bound.
   * @param context The context.
   */
  public void setContext( IContext context)
  {
    this.context = context;
  }

  /**
   * Bind the specified XPath expression.
   * @param xpath The XPath expression.
   */
  private void bind( String xpath)
  {
    model.dispatch( new BindRunnable( xpath));
  }
  
  /**
   * Unbind the specified XPath expression.
   * @param xpath The XPath expression.
   */
  private void unbind( String xpath)
  {
    model.dispatch( new UnbindRunnable( xpath));
  }

  /**
   * Request that the server IExternalReference represented by the specified key be sync'ed.
   * @param key The key.
   */
  private void sync( String key)
  {
    IExternalReference reference = map.get( key);
    if ( reference != null) 
    {
      model.dispatch( new SyncRunnable( reference));
      reference.getChildren();
    }
  }

  private final class BindRunnable implements Runnable
  {
    public BindRunnable( String xpath)
    {
      this.xpath = xpath;
    }
    
    public void run()
    {
      if ( exprMap.containsKey( xpath)) return;
      
      IExpression expr = XPath.createExpression( xpath);
      //expr.addNotifyListener( context, listener);
      exprMap.put(  xpath, expr);
    }
    
    private String xpath;
  }
  
  private final class UnbindRunnable implements Runnable
  {
    public UnbindRunnable( String xpath)
    {
      this.xpath = xpath;
    }
    
    public void run()
    {
      IExpression expr = exprMap.remove( xpath);
      //expr.removeListener( context, listener);
    }
    
    private String xpath;
  }
  
  private final static class SyncRunnable implements Runnable
  {
    public SyncRunnable( IExternalReference reference)
    {
      this.reference = reference;
    }
    
    public void run()
    {
      reference.getChildren();
    }
    
    private IExternalReference reference;
  }

  private Map<String, IExternalReference> map;
  private Map<String, IExpression> exprMap;
  private IModel model;
  private IContext context;
}
