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
import org.xmodel.util.HashMultiMap;
import org.xmodel.util.Identifier;
import org.xmodel.util.MultiMap;
import org.xmodel.xaction.XAction;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class that implements the server-side of the network caching policy protocol.
 */
public class Server extends Protocol
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
  public Server( String host, int port) throws IOException
  {
    random = new Random();
    index = new WeakHashMap<String, IExternalReference>();
    listeners = new HashMultiMap<Connection, Listener>();
    server = new TcpServer( host, port, this);
  }

  /**
   * Start the server.
   */
  public void start()
  {
    server.start();
  }
  
  /**
   * Stop the server.
   */
  public void stop()
  {
    server.stop();
  }
  
  /**
   * Set the context in which remote xpath expressions will be bound.
   * @param context The context.
   */
  public void setContext( IContext context)
  {
    this.context = context;
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#onClose(org.xmodel.net.stream.Connection)
   */
  @Override
  public void onClose( Connection connection)
  {
    super.onClose( connection);
    
    List<Listener> list = listeners.get( connection);
    if ( list != null)
    {
      for( Listener listener: list) listener.uninstall();
      listeners.removeAll( connection);
    }
  }

  /**
   * Find the element on the specified xpath and attach listeners.
   * @param sender The sender.
   * @param xpath The xpath expression.
   */
  private void attach( Connection sender, String xpath) throws IOException
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      IModelObject copy = null;
      if ( target != null)
      {
        copy = copy( target);
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
  private void detach( Connection sender, String xpath)
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
      try { sendError( sender, e.getMessage());} catch( IOException e2) {}
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
  private void query( Connection sender, String xpath) throws IOException
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
      try { sendError( sender, e.getMessage());} catch( IOException e2) {}
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
  private IModelObject copy( IModelObject root)
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
      if ( lNode instanceof IExternalReference)
      {
        if ( lNode.isDirty())
        {
          rNode = new ModelObject( lNode.getType());
          rNode.setAttribute( "net:dirty");
        }
        else
        {
          rNode = lNode.cloneObject();
        }
        
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
      }
      
      // add to stitch-up map
      map.put( lNode, rNode);
      
      // set result if not already set
      if ( result == null) result = rNode;
    } 
    
    return result;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAttachRequest(org.xmodel.net.nu.Connection, java.lang.String)
   */
  @Override
  protected void handleAttachRequest( Connection sender, String xpath)
  {
    context.getModel().dispatch( new AttachRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleDetachRequest(org.xmodel.net.nu.Connection, java.lang.String)
   */
  @Override
  protected void handleDetachRequest( Connection sender, String xpath)
  {
    context.getModel().dispatch( new DetachRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleSyncRequest(org.xmodel.net.nu.Connection, java.lang.String)
   */
  @Override
  protected void handleSyncRequest( Connection sender, String key)
  {
    context.getModel().dispatch( new SyncRunnable( sender, key));
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleQueryRequest(org.xmodel.net.Connection, java.lang.String)
   */
  @Override
  protected void handleQueryRequest( Connection sender, String xpath)
  {
    context.getModel().dispatch( new QueryRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleDebugStepIn(org.xmodel.net.Connection)
   */
  @Override
  protected void handleDebugStepIn( Connection sender)
  {
    XAction.getDebugger().stepIn();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleDebugStepOut(org.xmodel.net.Connection)
   */
  @Override
  protected void handleDebugStepOut( Connection sender)
  {
    XAction.getDebugger().stepOut();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleDebugStepOver(org.xmodel.net.Connection)
   */
  @Override
  protected void handleDebugStepOver( Connection sender)
  {
    XAction.getDebugger().stepOver();
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
    public AttachRunnable( Connection sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      try
      {
        attach( sender, xpath);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }

    private Connection sender;
    private String xpath;
  }
  
  private final class DetachRunnable implements Runnable
  {
    public DetachRunnable( Connection sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      detach( sender, xpath);
    }

    private Connection sender;
    private String xpath;
  }
  
  private final class SyncRunnable implements Runnable
  {
    public SyncRunnable( Connection sender, String key)
    {
      this.sender = sender;
      this.key = key;
    }
    
    public void run()
    {
      IExternalReference reference = index.get( key);
      if ( reference != null)
      {
        try
        {
          sync( reference);
          sendSyncResponse( sender);
        }
        catch( IOException e)
        {
          log.exception( e);
        }
      }
    }
    
    private Connection sender;
    private String key;
  }
  
  private final class QueryRunnable implements Runnable
  {
    public QueryRunnable( Connection sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      try
      {
        query( sender, xpath);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }
    
    private Connection sender;
    private String xpath;
  }
  
  private class Listener extends NonSyncingListener
  {
    public Listener( Connection sender, String xpath, IModelObject root)
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
      try
      {
        IModelObject clone = copy( child);
        sendAddChild( sender, createPath( root, parent), clone, index);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      try
      {
        sendRemoveChild( sender, createPath( root, parent), index);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      try
      {
        sendChangeAttribute( sender, createPath( root, object), attrName, newValue);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
     */
    @Override
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      try
      {
        sendClearAttribute( sender, createPath( root, object), attrName);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
     */
    @Override
    public void notifyDirty( IModelObject object, boolean dirty)
    {
      try
      {
        sendChangeDirty( sender, createPath( root, object), dirty);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
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

    private Connection sender;
    private String xpath;
    private IModelObject root;
  };
  
  private static Log log = Log.getLog( "org.xmodel.net");

  private TcpServer server;
  private Map<String, IExternalReference> index;
  private MultiMap<Connection, Listener> listeners;
  private IContext context;
  private Random random;
  
  public static void main( String[] args) throws Exception
  {
    IModelObject parent = new ModelObject( "parent");

    ManualDispatcher dispatcher = new ManualDispatcher();
    parent.getModel().setDispatcher( dispatcher);
    
    Server server = new Server( "0.0.0.0", 27613);
    server.setContext( new Context( parent));
    server.start();

    long stamp = System.currentTimeMillis();
    while( true)
    {
      long time = System.currentTimeMillis();
      long elapsed = (time - stamp);
      if ( false)
      {
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
      }
      
      dispatcher.process();
      Thread.sleep( 1000);
    }
  }
}
