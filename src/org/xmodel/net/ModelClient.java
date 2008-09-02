package org.xmodel.net;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.xmodel.*;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.net.robust.Client;
import org.xmodel.net.robust.ISession;
import org.xmodel.net.robust.RobustSession;
import org.xmodel.util.Radix;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;


/**
 * A client for the data-model server.
 */
public class ModelClient extends RobustSession
{
  /**
   * Create a client to the specified host and port.
   * @param host The host.
   * @param port The port.
   * @param cache The cache to use for external references.
   */
  public ModelClient( String host, int port, ICache cache)
  {
    this( host, port, cache, ModelRegistry.getInstance().getModel());
  }
  
  /**
   * Create a client to the specified host and port.
   * @param host The host.
   * @param port The port.
   * @param cache The cache to use for external references.
   * @param model The model.
   */
  public ModelClient( String host, int port, ICache cache, IModel model)
  {
    super( new Client( host, port), 100);
    
    this.model = model;

    timeout = Integer.parseInt( System.getProperty( "xmodel.network.timeout", "30000"));
    cachingPolicy = new NetIDCachingPolicy( cache, this);
    compressor = new TabularCompressor( PostCompression.none);
    decompressor = new TabularCompressor( PostCompression.none);
    netIDs = new HashMap<String, IModelObject>();
    block = new Semaphore( 0);
    nextID = System.nanoTime();
    limit = 10000;
    
    addListener( listener);
  }

  /**
   * Set the timeout for blocking messages.
   * @param timeout The timeout in milliseconds.
   */
  public void setTimeout( int timeout)
  {
    this.timeout = timeout;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.Client#open()
   */
  @Override
  public void open()
  {
    super.open();
    sendInit();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.Client#close()
   */
  @Override
  public void close()
  {
    sendClose( "Client closed.");
    super.close();
  }

  /**
   * Perform an arbitrary query and return the result.
   * @param query The query.
   * @param timeout The timeout.
   * @return Returns the query result.
   */
  public Object query( String query, int timeout) throws TimeoutException
  {
    IModelObject response = sendQuery( query, limit, timeout);
    String type = Xlate.get( response, "type", "");
    if ( type.equals( "NODES"))
    {
      return parseResult( response.getChildren());
    }
    else if ( type.equals( "STRING"))
    {
      return Xlate.get( response, "result", ""); 
    }
    else if ( type.equals( "NUMBER"))
    {
      return Xlate.get( response, "result", 0d); 
    }
    else if ( type.equals( "BOOLEAN"))
    {
      return Xlate.get( response, "result", false); 
    }
    return null;
  }

  /**
   * Set the maximum number of elements to be queried at one time. All root elements of 
   * a query will be returned even if the number exceeds the limit. Stubs representing
   * elements which could not be returned due to the limit will be converted into 
   * dirty external references.
   * @param limit The maximum number of elements per query.
   */
  public void setQueryLimit( int limit)
  {
    this.limit = limit;
  }
  
  /**
   * Perform a node-set query, bind it for updates, and limit the number of elements returned 
   * in the tree. If the number of descendants of all elements returned by the query exceeds 
   * the limit, then some elements will not have all of their children.  These elements will 
   * be created as dirty references. Root elements are always returned even if the count 
   * exceeds the limit.  All non-dirty elements will be kept up-to-date.
   * @param query The node-set query. 
   * @return Returns the root elements of the query.
   */
  public List<IModelObject> bind( String query) throws TimeoutException
  {
    // send
    IModelObject response = sendBind( query, limit, timeout);
    if ( response == null) return null;
    
    // parse response
    return parseResult( response.getChildren());
  }
  
  /**
   * Sync the remote object with the specified network id and limit the number of elements
   * returned in the tree. If the number of descendants of all elements returned by the query 
   * exceeds the limit, then some elements will not have all of their children.  These elements 
   * will be created as dirty references. Root elements are always returned even if the count 
   * exceeds the limit.  All non-dirty elements will be kept up-to-date.
   * @param reference The reference being synced.
   * @return Returns the result.
   */
  public IModelObject sync( IExternalReference reference) throws TimeoutException
  {
    // send
    String netID = Xlate.get( reference, "net:id", (String)null);
    IModelObject response = sendSync( netID, limit, timeout);
    if ( response == null) return null;
    
    // parse response
    IModelObject result = parseResult( response.getChild( 0));
    
    // write over the incorrect map entry made by parseResponse
    netIDs.put( netID, reference);
    
    return result;
  }
  
  /**
   * Unbind the remote element with the specified network id.
   * @param netID The network ID of the element.
   */
  public void unbind( String netID) throws TimeoutException
  {
    sendUnbind( netID, timeout);
    netIDs.remove( netID);
  }
  
  /**
   * Mark dirty the remote external reference which the specified object represents.
   * @param object An object representing a remote external reference.
   */
  public void markDirty( IModelObject object)
  {
    sendMarkDirty( Xlate.get( object, "net:id", (String)null));
  }
  
  /**
   * Parse a message result and return the result with reference substitutions.
   * @param result The result elements.
   * @return Returns the parsed result.
   */
  protected List<IModelObject> parseResult( List<IModelObject> result)
  {
    List<IModelObject> parsed = new ArrayList<IModelObject>( result.size());
    for( IModelObject root: result)
      parsed.add( parseResult( root));
    return parsed;
  }
  
  /**
   * Parse a message result and return the result with reference substitutions.
   * @param result The result elements.
   * @return Returns the parsed result.
   */
  protected IModelObject parseResult( IModelObject result)
  {
    List<IModelObject> elements = descendantOrSelfExpr.query( result, null);
    for( IModelObject element: elements)
    {
      String netID = Xlate.get( element, "net:id", "");
      
      if ( element.getAttribute( "net:stub") != null)
      {
        //System.out.println( "STUBBED: "+element);
        //element.removeAttribute( "net:stub");
        
        boolean dirty = Xlate.get( element, "net:dirty", true);
        //element.removeAttribute( "net:dirty");
        
        // create reference
        ExternalReference reference = new ExternalReference( element.getType());
        ModelAlgorithms.copyAttributes( element, reference);
        ModelAlgorithms.moveChildren( element, reference);
        reference.setCachingPolicy( cachingPolicy);
        reference.setDirty( dirty);
        
        // substitute
        IModelObject parent = element.getParent();
        int index = parent.getChildren().indexOf( element);
        element.removeFromParent();
        parent.addChild( reference, index);

        // update map
        netIDs.put( netID, reference);
        
        // change root
        if ( element == result) result = reference;
      }
      else
      {
        netIDs.put( netID, element);
      }
    }
    
    return result;
  }
  
  /**
   * Send init message to server. 
   * @return Returns the message correlation id.
   */
  protected String sendInit()
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "init");
    request.setID( messageID);
    send( request);
    return messageID;
  }
  
  /**
   * Send a close message to the server.
   * @param message The message.
   */
  protected void sendClose( String message)
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "close");
    request.setID( messageID);
    request.setValue( message);
    send( request);
  }
  
  /**
   * Send a query request to the server.
   * @param query The node-set query.
   * @param limit The maximum number of elements to return.
   * @return Returns the query result.
   */
  protected IModelObject sendQuery( String query, int limit, int timeout) throws TimeoutException
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "query");
    request.setID( messageID);
    request.setAttribute( "query", query);
    request.setAttribute( "limit", limit);
    return sendAndWait( request, timeout);
  }
  
  /**
   * Send a bind request to the server.
   * @param query The node-set query.
   * @param limit The maximum number of elements to return.
   * @return Returns the bind result.
   */
  protected IModelObject sendBind( String query, int limit, int timeout) throws TimeoutException
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "bind");
    request.setID( messageID);
    request.setAttribute( "query", query);
    request.setAttribute( "limit", limit);
    return sendAndWait( request, timeout);
  }
  
  /**
   * Send a sync request to the server.
   * @param netID The network ID of the element.
   * @param limit The maximum number of elements to return.
   * @param timeout The timeout.
   * @return Returns the sync response.
   */
  protected IModelObject sendSync( String netID, int limit, int timeout) throws TimeoutException
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "sync");
    request.setID( messageID);
    request.setAttribute( "net:id", netID);
    request.setAttribute( "limit", limit);
    return sendAndWait( request, timeout);
  }
  
  /**
   * Send an unbind request to the server.
   * @param netID The network ID of the element to be unbound.
   * @param timeout The timeout.
   */
  protected void sendUnbind( String netID, int timeout) throws TimeoutException
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "unbind");
    request.setID( messageID);
    request.setAttribute( "net:id", netID);
    sendAndWait( request, timeout);
  }    
  
  /**
   * Send a mark dirty request to the server.
   * @param netID The network ID of the element to be marked dirty.
   */
  protected void sendMarkDirty( String netID)
  {
    String messageID = Radix.convert( nextID++, 36);
    ModelObject request = new ModelObject( "dirty");
    request.setID( messageID);
    request.setAttribute( "net:id", netID);
    send( request);    
  }
  
  /**
   * Send a message to the server.
   * @param message The message.
   */
  protected void send( IModelObject message)
  {
    byte[] compressed = compressor.compress( message);
    write( compressed);
  }
  
  /**
   * Send a message to the server and wait for the response.
   * @param message The message.
   * @param timeout The timeout.
   * @return Returns the response.
   */
  protected IModelObject sendAndWait( IModelObject message, int timeout) throws TimeoutException
  {
    //System.out.println( "sendAndWait from thread: "+Thread.currentThread());
    // set message id to block on
    messageID = message.getID();
    
    // send message
    send( message);
    
    // block on semaphore
    try 
    { 
      if ( !block.tryAcquire( timeout, TimeUnit.MILLISECONDS))
        throw new TimeoutException( "Timeout waiting for response to: "+message);
    } 
    catch( InterruptedException e) 
    { 
      throw new TimeoutException( "Operation interrupted.", e);
    }
    
    // return message
    return response.cloneTree();
  }
    
  /**
   * Handle an asynchronous message from the server.
   * @param message The message.
   */
  protected void handle( IModelObject message)
  {
    if ( message.isType( "insert"))
    {
      handleInsert( message);
    }
    else if ( message.isType( "delete"))
    {
      handleDelete( message);
    }
    else if ( message.isType( "change"))
    {
      handleChange( message);
    }
    else if ( message.isType( "clear"))
    {
      handleClear( message);
    }
    else if ( message.isType( "close"))
    {
      handleClose( message);
    }
    else if ( message.isType( "dirty"))
    {
      handleDirty( message);
    }
    else if ( message.isType( "error"))
    {
      handleError( message);
    }
  }
  
  /**
   * Handle an asynchronous update message from the server.
   * @param message The message.
   */
  protected void handleInsert( IModelObject message)
  {
    String netID = Xlate.childGet( message, "parent", (String)null);
    IModelObject parent = netIDs.get( netID);
    if ( parent != null)
    {
      IModelObject child = parseResult( message.getFirstChild( "child").getChild( 0));
      int index = Xlate.childGet( message, "index", parent.getNumberOfChildren());
      parent.addChild( child, index);
    }
  }
  
  /**
   * Handle an asynchronous update message from the server.
   * @param message The message.
   */
  protected void handleDelete( IModelObject message)
  {
    String netID = Xlate.get( message, "net:id", (String)null);
    IModelObject object = netIDs.get( netID);
    if ( object != null) object.removeFromParent();
  }
  
  /**
   * Handle an asynchronous update message from the server.
   * @param message The message.
   */
  protected void handleChange( IModelObject message)
  {
    String netID = Xlate.get( message, "net:id", (String)null);
    IModelObject object = netIDs.get( netID);
    if ( object != null)
    {
      String attribute = Xlate.childGet( message, "attribute", (String)null);
      String value = Xlate.childGet( message, "value", (String)null);
      object.setAttribute( attribute, value);
    }
  }
  
  /**
   * Handle an asynchronous update message from the server.
   * @param message The message.
   */
  protected void handleClear( IModelObject message)
  {
    String netID = Xlate.get( message, "net:id", (String)null);
    IModelObject object = netIDs.get( netID);
    if ( object != null)
    {
      String attribute = Xlate.childGet( message, "attribute", (String)null);
      object.removeAttribute( attribute);
    }
  }
  
  /**
   * Handle an asynchronous close message.
   * @param message The message.
   */
  protected void handleClose( IModelObject message)
  {
    close();
  }

  /**
   * Handle an asynchronous dirty message.
   * @param message The message.
   */
  protected void handleDirty( IModelObject message)
  {
    String netID = Xlate.get( message, "net:id", (String)null);
    IModelObject object = netIDs.get( netID);
    if ( object != null)
    {
      boolean dirty = Xlate.get( message, "dirty", false);
      object.setAttribute( "net:dirty", dirty);
      
      // all remote external references are references locally
      if ( dirty) ((IExternalReference)object).clearCache();
    }
  }
  
  /**
   * Handle an asynchronous error message.
   * @param message The message.
   */
  protected void handleError( IModelObject message)
  {
    IModelObject errors = errorsExpr.queryFirst();
    errors.addChild( message);
  }
  
  /**
   * Client session listener.
   */
  private final Listener listener = new ISession.Listener() {
    public void notifyOpen( ISession session)
    {
    }
    public void notifyClose( ISession session)
    {
      exit = true;
    }
    public void notifyConnect( ISession session)
    {
      messageLoopThread = new Thread( messageLoop, "Session Loop ("+session.getShortSessionID()+")");
      messageLoopThread.setDaemon( true);
      messageLoopThread.start();
    }
    public void notifyDisconnect( ISession session)
    {
      exit = true;
      messageLoopThread.interrupt();
      messageLoopThread = null;
    }
  };
  
  /**
   * Message loop runnable.
   */
  private final Runnable messageLoop = new Runnable() {
    public void run()
    {
      InputStream stream = session.getInputStream();
      while( !exit)
      {
        IModelObject message = decompressor.decompress( stream);
        if ( messageID != null && message.getID().equals( messageID))
        {
          // synchronous message
          messageID = null;
          response = message;
          block.release();
        }
        else
        {
          // asynchronous message
          AsyncRunnable runnable = new AsyncRunnable();
          runnable.message = message;
          model.dispatch( runnable);
        }
      }
    }
  };
  
  /**
   * Asynchronous message runnable.
   */
  private class AsyncRunnable implements Runnable
  {
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      handle( message.cloneTree());
    }
    
    public IModelObject message;
  }
  
  private final IExpression descendantOrSelfExpr = XPath.createExpression(
    "nosync( descendant-or-self::*)");
  
  private final IExpression errorsExpr = XPath.createExpression(
   "collection('errors')");

  private IModel model;
  private Thread messageLoopThread;
  private NetIDCachingPolicy cachingPolicy;
  private Map<String, IModelObject> netIDs;
  private ICompressor compressor;
  private ICompressor decompressor;
  private Semaphore block;
  private String messageID;
  private IModelObject response;
  private int limit;
  private int timeout;
  private long nextID;
  private boolean exit;
}