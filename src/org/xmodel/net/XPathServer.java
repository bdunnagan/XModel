package org.xmodel.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ManualDispatcher;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.Xlate;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.NonSyncingIterator;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.log.Log;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.TcpServer;
import org.xmodel.net.stream.Connection.IListener;
import org.xmodel.util.HashMultiMap;
import org.xmodel.util.Identifier;
import org.xmodel.util.MultiMap;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class that implements the server-side of the network caching policy protocol.
 */
public class XPathServer extends Protocol implements Runnable, IListener
{
  public enum Message
  {
    version,
    attachRequest,
    attachResponse,
    detachRequest,
    syncRequest,
    addChild,
    removeChild,
    changeAttribute,
    clearAttribute,
    error
  }
  
  public static int defaultPort = 27613;
  
  /**
   * Create a server bound to the specified local address and port.
   * @param model The model.
   */
  public XPathServer()
  {
    random = new Random();
    index = new WeakHashMap<String, IExternalReference>();
    listeners = new HashMultiMap<INetSender, Listener>();
  }
  
  /**
   * Set the context in which remote xpath expressions will be bound.
   * @param context The context.
   */
  public void setContext( IContext context)
  {
    this.context = context;
    this.thread = Thread.currentThread();
  }

  /**
   * Start the server thread.
   * @param host The local address.
   * @param port The local port.
   */
  public void start( String host, int port) throws IOException
  {
    server = new TcpServer( host, port, this, this, this);
    thread = new Thread( this);
    thread.setDaemon( true);
    thread.start();
  }

  /**
   * Stop the server thread.
   */
  public void stop()
  {
    exit = true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    exit = false;
    while( !exit)
    {
      try
      {
        server.process( 500);
        System.out.println( ".");
      }
      catch( Exception e)
      {
        log.warn( e.getMessage());
        break;
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.Connection.IListener#connected(org.xmodel.net.stream.Connection)
   */
  @Override
  public void connected( Connection connection)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.stream.Connection.IListener#disconnected(org.xmodel.net.stream.Connection)
   */
  @Override
  public void disconnected( Connection connection)
  {
    List<Listener> list = listeners.get( connection);
    for( Listener listener: list) listener.uninstall();
    listeners.removeAll( connection);
  }

  /**
   * Find the element on the specified xpath and attach listeners.
   * @param sender The sender.
   * @param xpath The xpath expression.
   */
  private void attach( INetSender sender, String xpath)
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      IModelObject copy = null;
      if ( target != null)
      {
        copy = serialize( target);
        Listener listener = new Listener( sender, xpath, target);
        listener.install( target);
        listeners.put( sender, listener);
      }
      sendAttachResponse( sender, copy);
    }
    catch( PathSyntaxException e)
    {
      sendError( sender, e.getMessage());
    }
  }
  
  /**
   * Remove listeners from the element on the specified xpath.
   * @param sender The sender.
   * @param xpath The xpath expression.
   */
  private void detach( INetSender sender, String xpath)
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      if ( target != null)
      {
        Listener listener = new Listener( sender, xpath, target);
        listener.uninstall( target);
      }
    }
    catch( PathSyntaxException e)
    {
      sendError( sender, e.getMessage());
    }
  }

  /**
   * Manually sync the specified reference by accessing its children.
   * @param reference The reference.
   */
  private void sync( IExternalReference reference)
  {
    reference.getChildren();
  }
  
  /**
   * Execute the specified query and return the result.
   * @param sender The sender.
   * @param xpath The query.
   */
  private void query( INetSender sender, String xpath)
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      Object result = null;
      switch( expr.getType( context))
      {
        case NODES:   result = expr.evaluateNodes( context); break;
        case STRING:  result = expr.evaluateString( context); break;
        case NUMBER:  result = expr.evaluateNumber( context); break;
        case BOOLEAN: result = expr.evaluateBoolean( context); break;
      }
      sendQueryResponse( sender, result);
    }
    catch( PathSyntaxException e)
    {
      sendError( sender, e.getMessage());
    }
  }
  
  /**
   * Index the specified reference.
   * @param reference The reference.
   * @return Returns the key.
   */
  private String index( IExternalReference reference)
  {
    String key = Identifier.generate( random, 13);
    index.put( key, reference);
    return key;
  }
  
  /**
   * Create a copy of the specified element putting IExternalReferences where there are dirty references.
   * @param root The element to be copied.
   * @return Returns the copy.
   */
  private IModelObject serialize( IModelObject root)
  {
    IModelObject result = null;
    Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
    
    // create copy
    NonSyncingIterator iter = new NonSyncingIterator( root);
    while( iter.hasNext())
    {
      IModelObject lNode = iter.next();
      IModelObject rNode = null;
      IModelObject rParent = map.get( lNode.getParent());
      if ( lNode.isDirty())
      {
        rNode = new ModelObject( lNode.getType());

        // copy static attributes
        IExternalReference lRef = (IExternalReference)lNode;
        for( String attrName: lRef.getStaticAttributes())
        {
          rNode.setAttribute( attrName, lNode.getAttribute( attrName));
        }

        // index reference so it can be synced remotely
        rNode.setAttribute( "net:key", index( lRef));
        
        // add new element to tree
        if ( rParent != null) rParent.addChild( rNode);
      }
      else
      {
        rNode = lNode.cloneObject();
        if ( rParent != null) rParent.addChild( rNode);
        
        // add to stitch-up map
        map.put( lNode, rNode);
      }
      
      // set result if not already set
      if ( result == null) result = rNode;
    } 
    
    return result;
  } 
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAttachRequest(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleAttachRequest( INetSender sender, String xpath)
  {
    context.getModel().dispatch( new AttachRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleDetachRequest(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleDetachRequest( INetSender sender, String xpath)
  {
    context.getModel().dispatch( new DetachRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleSyncRequest(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleSyncRequest( INetSender sender, String key)
  {
    IExternalReference reference = index.get( key);
    if ( reference != null) context.getModel().dispatch( new SyncRunnable( reference));
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleQueryRequest(org.xmodel.net.INetSender, java.lang.String)
   */
  @Override
  protected void handleQueryRequest( INetSender sender, String xpath)
  {
    context.getModel().dispatch( new QueryRunnable( sender, xpath));
  }

  /**
   * Create a relative xpath from the specified root to the specified element.
   * @param root The root.
   * @param element The element.
   * @return Returns the xpath string.
   */
  private final String createPath( IModelObject root, IModelObject element)
  {
    IPath path = ModelAlgorithms.createRelativePath( root, element);
    return path.toString();
  }
  
  private final class AttachRunnable implements Runnable
  {
    public AttachRunnable( INetSender sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      attach( sender, xpath);
    }

    private INetSender sender;
    private String xpath;
  }
  
  private final class DetachRunnable implements Runnable
  {
    public DetachRunnable( INetSender sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      detach( sender, xpath);
    }

    private INetSender sender;
    private String xpath;
  }
  
  private final class SyncRunnable implements Runnable
  {
    public SyncRunnable( IExternalReference reference)
    {
      this.reference = reference;
    }
    
    public void run()
    {
      sync( reference);
    }
    
    private IExternalReference reference;
  }
  
  private final class QueryRunnable implements Runnable
  {
    public QueryRunnable( INetSender sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      query( sender, xpath);
    }
    
    private INetSender sender;
    private String xpath;
  }
  
  private class Listener extends NonSyncingListener
  {
    public Listener( INetSender sender, String xpath, IModelObject root)
    {
      this.sender = sender;
      this.xpath = xpath;
      this.root = root;
    }
    
    public void uninstall()
    {
      uninstall( root);
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyAddChild( parent, child, index);
      sendAddChild( sender, createPath( root, parent), child, index);
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      sendRemoveChild( sender, createPath( root, parent), index);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      sendChangeAttribute( sender, createPath( root, object), attrName, newValue);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
     */
    @Override
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      sendClearAttribute( sender, createPath( root, object), attrName);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object object)
    {
      if ( object instanceof Listener)
      {
        Listener other = (Listener)object;
        return other.sender == sender && other.xpath.equals( xpath);
      }
      return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
      return sender.hashCode() + xpath.hashCode();
    }

    private INetSender sender;
    private String xpath;
    private IModelObject root;
  };
  
  private static Log log = Log.getLog( "org.xmodel.net");
  
  private TcpServer server;
  private Thread thread;
  private boolean exit;
  private Map<String, IExternalReference> index;
  private MultiMap<INetSender, Listener> listeners;
  private IContext context;
  private Random random;
  
  public static void main( String[] args) throws Exception
  {
    IModelObject parent = new ModelObject( "parent");

    ManualDispatcher dispatcher = new ManualDispatcher();
    parent.getModel().setDispatcher( dispatcher);
    
    XPathServer server = new XPathServer();
    server.setContext( new Context( parent));
    server.start( "0.0.0.0", 27613);

    long stamp = System.currentTimeMillis();
    while( true)
    {
      long time = System.currentTimeMillis();
      long elapsed = (time - stamp);
      if ( elapsed > 10000)
      {
        stamp = time;
        ModelObject child = new ModelObject( "child");
        child.setAttribute( "tstamp", time);
        parent.addChild( child);
      }
      else
      {
        for( IModelObject child: parent.getChildren())
        {
          long tstamp = Xlate.get( child, "tstamp", 0L);
          child.setValue( time - tstamp);
        }
      }
      
      dispatcher.process();
      Thread.sleep( 1000);
    }
  }
}
