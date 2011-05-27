
.package org.xmodel.net.nu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.compress.ICompressor;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.NonSyncingIterator;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.net.nu.stream.Connection;
import org.xmodel.net.nu.stream.IReceiver;
import org.xmodel.net.nu.stream.TcpClient;
import org.xmodel.net.nu.stream.TcpServer;
import org.xmodel.util.Identifier;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class that implements the server-side of the network caching policy protocol.
 */
public class XPathServer extends Protocol
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
  
  /**
   * Create a server bound to the specified local address and port.
   * @param host The local address.
   * @param port The local port.
   * @param model The model.
   */
  public XPathServer( String host, int port, IModel model) throws IOException
  {
    this.model = model;
    index = new WeakHashMap<String, IExternalReference>();
    orphans = new ArrayList<Listener>();
    random = new Random();
    
    server = new TcpServer( host, port, this);
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
   * Find the element on the specified xpath and attach listeners.
   * @param client The client that made the request.
   * @param xpath The xpath expression.
   */
  private void attach( TcpClient client, String xpath)
  {
    model.dispatch( new AttachRunnable( client, xpath));
  }
  
  /**
   * Find the element on the specified xpath and attach listeners.
   * @param client The client that made the request.
   * @param xpath The xpath expression.
   */
  private void doAttach( TcpClient client, String xpath)
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      IModelObject copy = null;
      if ( target != null)
      {
        copy = serialize( target);
        Listener listener = new Listener( client, xpath, target);
        listener.install( target);
      }
      sendAttachResponse( client, copy);
    }
    catch( PathSyntaxException e)
    {
      sendError( client, e.getMessage());
    }
  }
  
  /**
   * Remove listeners from the element on the specified xpath.
   * @param client The client that made the request.
   * @param xpath The xpath expression.
   */
  private void detach( TcpClient client, String xpath)
  {
    model.dispatch( new DetachRunnable( client, xpath));
  }

  /**
   * Remove listeners from the element on the specified xpath.
   * @param client The client that made the request.
   * @param xpath The xpath expression.
   */
  private void doDetach( TcpClient client, String xpath)
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      if ( target != null)
      {
        Listener listener = new Listener( client, xpath, target);
        listener.uninstall( target);
      }
    }
    catch( PathSyntaxException e)
    {
      orphans.add( new Listener( client, xpath, null));
      sendError( client, e.getMessage());
    }
  }

  /**
   * Request that the server IExternalReference represented by the specified key be sync'ed.
   * @param key The key.
   */
  private void sync( String key)
  {
    IExternalReference reference = index.get( key);
    if ( reference != null) model.dispatch( new SyncRunnable( reference));
  }

  /**
   * Manually sync the specified reference by accessing its children.
   * @param reference The reference.
   */
  private void doSync( IExternalReference reference)
  {
    reference.getChildren();
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
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleDetachRequest(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleDetachRequest( INetSender sender, String xpath)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleSyncRequest(org.xmodel.net.nu.INetSender, java.lang.String)
   */
  @Override
  protected void handleSyncRequest( INetSender sender, String key)
  {
  }

  /**
   * Send the response to an attach request.
   * @param client The client.
   * @param copy The content.
   */
  private void sendAttachResponse( TcpClient client, IModelObject copy)
  {
    byte[] bytes = compressor.compress( copy);
  }

  /**
   * Send an error response.
   * @param client The client.
   * @param message The error message.
   */
  private void sendError( TcpClient client, String message)
  {
  }

  /**
   * Send a child added update to the client.
   * @param client The client.
   * @param root The attached root.
   * @param parent The parent.
   * @param child The child.
   * @param index The index.
   */
  private void sendAddChild( TcpClient client, IModelObject root, IModelObject parent, IModelObject child, int index)
  {
    // generate key for parent
  }

  /**
   * Send a child removed update to the client.
   * @param client The client.
   * @param root The attached root.
   * @param parent The parent.
   * @param index The index.
   */
  private void sendRemoveChild( TcpClient client, IModelObject root, IModelObject parent, int index)
  {
    // generate key for parent
  }

  /**
   * Send an attribute change update to the client.
   * @param client The client.
   * @param root The attached root.
   * @param object The object.
   * @param attrName The attribute.
   * @param newValue The new value.
   */
  private void sendAttributeChange( TcpClient client, IModelObject root, IModelObject object, String attrName, Object newValue)
  {
    // generate key for object
  }

  /**
   * Send an attribute cleared update to the client.
   * @param client The client.
   * @param root The attached root.
   * @param object The object.
   * @param attrName The attribute.
   */
  private void sendAttributeClear( TcpClient client, IModelObject root, IModelObject object, String attrName)
  {
    // generate key for object
  }
  
  private final class AttachRunnable implements Runnable
  {
    public AttachRunnable( TcpClient client, String xpath)
    {
      this.client = client;
      this.xpath = xpath;
    }
    
    public void run()
    {
      doAttach( client, xpath);
    }

    private TcpClient client;
    private String xpath;
  }
  
  private final class DetachRunnable implements Runnable
  {
    public DetachRunnable( TcpClient client, String xpath)
    {
      this.client = client;
      this.xpath = xpath;
    }
    
    public void run()
    {
      doDetach( client, xpath);
    }

    private TcpClient client;
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
      doSync( reference);
    }
    
    private IExternalReference reference;
  }
  
  private class Listener extends NonSyncingListener
  {
    public Listener( TcpClient client, String xpath, IModelObject root)
    {
      this.client = client;
      this.xpath = xpath;
      this.root = root;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyAddChild( parent, child, index);
      sendAddChild( client, root, parent, child, index);
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      sendRemoveChild( client, root, parent, index);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      sendAttributeChange( client, root, object, attrName, newValue);
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
     */
    @Override
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      sendAttributeClear( client, root, object, attrName);
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
        return other.client == client && other.xpath.equals( xpath);
      }
      return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
      return client.hashCode() + xpath.hashCode();
    }

    private TcpClient client;
    private String xpath;
    private IModelObject root;
  };
  
  private TcpServer server;
  private Map<String, IExternalReference> index;
  private List<Listener> orphans;
  private IModel model;
  private IContext context;
  private Random random;
  private ICompressor compressor;
}
