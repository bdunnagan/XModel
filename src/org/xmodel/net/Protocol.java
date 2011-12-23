package org.xmodel.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.xmodel.DepthFirstIterator;
import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.Xlate;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;
import org.xmodel.external.CachingException;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.log.Log;
import org.xmodel.util.Identifier;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * The protocol class for the NetworkCachingPolicy protocol.
 */
public class Protocol implements ILink.IListener
{
  public final static int version = 1;
  
  public enum Type
  {
    version,
    error,
    attachRequest,
    attachResponse,
    detachRequest,
    syncRequest,
    syncResponse,
    addChild,
    removeChild,
    changeAttribute,
    clearAttribute,
    changeDirty,
    queryRequest,
    queryResponse,
    executeRequest,
    executeResponse,
    debugStepIn,
    debugStepOver,
    debugStepOut
  }

  /**
   * Create an instance of the protocol.
   * @param timeout The timeout for network operations in milliseconds.
   */
  public Protocol( int timeout)
  {
    this.context = new StatefulContext();
    
    this.timeout = timeout;
    this.responseQueue = new SynchronousQueue<byte[]>();
    this.random = new Random();
    this.keys = new WeakHashMap<IModelObject, KeyRecord>();
    this.map = new HashMap<ILink, ConnectionInfo>();
    
    // allocate less than standard mtu
    buffer = ByteBuffer.allocate( 4096);
    buffer.order( ByteOrder.BIG_ENDIAN);
    
    dispatchers = new Stack<IDispatcher>();
  }
  
  /**
   * Set the context in which remote xpath expressions will be bound.
   * @param context The context.
   */
  public void setContext( IContext context)
  {
    this.context = context;
    if ( dispatchers.empty()) pushDispatcher( context.getModel().getDispatcher());
  }
  
  /**
   * Push the specified dispatcher on the stack.
   * @param dispatcher The dispatcher.
   */
  public void pushDispatcher( IDispatcher dispatcher)
  {
    dispatchers.push( dispatcher);
  }
  
  /**
   * Pop the last dispatcher off the stack.
   */
  public void popDispatcher()
  {
    dispatchers.pop();
  }
  
  /**
   * Attach to the element on the specified xpath.
   * @param link The remote link.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( ILink link, String xpath, IExternalReference reference) throws IOException
  {
    if ( link != null && link.isOpen())
    {
      sendVersion( link, (byte)1);
      if ( dispatchers.empty()) pushDispatcher( reference.getModel().getDispatcher());
      
      ConnectionInfo info = new ConnectionInfo();
      info.xpath = xpath;
      info.element = reference;
      map.put( link, info);
      
      sendAttachRequest( link, xpath);
      
      info.listener = new Listener( link, xpath, reference);
      info.listener.install( reference);
    }
    else
    {
      throw new CachingException( "Unable to connect to server.");
    }
  }

  /**
   * Detach from the element on the specified path.
   * @param link The remote link.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void detach( ILink link, String xpath, IExternalReference reference) throws IOException
  {
    try
    {
      sendDetachRequest( link, xpath);
    }
    finally
    {
      popDispatcher();
    }
  }
  
  /**
   * Find the element on the specified xpath and attach listeners.
   * @param sender The sender.
   * @param xpath The xpath expression.
   */
  protected void doAttach( ILink sender, String xpath) throws IOException
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      IModelObject copy = null;
      if ( target != null)
      {
        ConnectionInfo info = new ConnectionInfo();
        info.xpath = xpath;
        info.element = target;
        map.put( sender, info);
        
        // make sure target is synced since that is what we are requesting
        target.getChildren();
        copy = encode( sender, target, true);
        
        info.listener = new Listener( sender, xpath, target);
        info.listener.install( target);
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
  protected void doDetach( ILink sender, String xpath)
  {
    map.get( sender).index.clear();
    
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
   * Execute the specified query and return the result.
   * @param sender The sender.
   * @param xpath The query.
   */
  protected void doQuery( ILink sender, String xpath) throws IOException
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
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IConnection.IListener#onClose(org.xmodel.net.IConnection)
   */
  @Override
  public void onClose( ILink link)
  {
    ConnectionInfo info = map.get( link);
    info.listener.uninstall();
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.IConnection.IListener#onReceive(org.xmodel.net.IConnection, java.nio.ByteBuffer)
   */
  @Override
  public void onReceive( ILink link, ByteBuffer buffer)
  {
    buffer.mark();
    while( handleMessage( link, buffer)) buffer.mark();
    buffer.reset();
  }
  
  /**
   * Parse and handle one message from the specified buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @return Returns true if a message was handled.
   */
  private final boolean handleMessage( ILink link, ByteBuffer buffer)
  {
    try
    {
      Type type = readMessageType( buffer);
      
      int length = readMessageLength( buffer);
      if ( length > buffer.remaining()) return false;
      
      switch( type)
      {
        case version:         handleVersion( link, buffer, length); return true;
        case error:           handleError( link, buffer, length); return true;
        case attachRequest:   handleAttachRequest( link, buffer, length); return true;
        case attachResponse:  handleAttachResponse( link, buffer, length); return true;
        case detachRequest:   handleDetachRequest( link, buffer, length); return true;
        case syncRequest:     handleSyncRequest( link, buffer, length); return true;
        case syncResponse:    handleSyncResponse( link, buffer, length); return true;
        case addChild:        handleAddChild( link, buffer, length); return true;
        case removeChild:     handleRemoveChild( link, buffer, length); return true;
        case changeAttribute: handleChangeAttribute( link, buffer, length); return true;
        case clearAttribute:  handleClearAttribute( link, buffer, length); return true;
        case changeDirty:     handleChangeDirty( link, buffer, length); return true;
        case queryRequest:    handleQueryRequest( link, buffer, length); return true;
        case queryResponse:   handleQueryResponse( link, buffer, length); return true;
        case executeRequest:  handleExecuteRequest( link, buffer, length); return true;
        case executeResponse: handleExecuteResponse( link, buffer, length); return true;
        case debugStepIn:     handleDebugStepIn( link, buffer, length); return true;
        case debugStepOver:   handleDebugStepOver( link, buffer, length); return true;
        case debugStepOut:    handleDebugStepOut( link, buffer, length); return true;
      }
    }
    catch( BufferUnderflowException e)
    {
    }
    
    return false;
  }
  
  /**
   * Send the protocol version.
   * @param link The link.
   * @param version The version.
   */
  public final void sendVersion( ILink link, short version) throws IOException
  {
    log.debugf( "sendVersion: %d", version);
    initialize( buffer);
    buffer.putShort( version);
    finalize( buffer, Type.version, 2);
    link.send( buffer);
  }
  
   /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleVersion( ILink link, ByteBuffer buffer, int length)
  {
    int version = buffer.getShort();
    log.debugf( "handleVersion: %d", version);
    if ( version != Protocol.version)
    {
      link.close();
    }
  }
  
  /**
   * Send an error message.
   * @param link The link.
   * @param message The error message.
   */
  public final void sendError( ILink link, String message) throws IOException
  {
    log.debugf( "sendError: %s", message);
    initialize( buffer);
    byte[] bytes = message.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.error, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleError( ILink link, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    log.debugf( "handleError: %s", new String( bytes));
    handleError( link, new String( bytes));
  }
  
  /**
   * Handle an error message.
   * @param link The link.
   * @param message The message.
   */
  protected void handleError( ILink link, String message)
  {
    log.error( message);
  }
  
  /**
   * Send an attach request message.
   * @param link The link.
   * @param xpath The xpath.
   */
  public final void sendAttachRequest( ILink link, String xpath) throws IOException
  {
    log.debugf( "sendAttachRequest: %s", xpath);
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachRequest, bytes.length);

    // send and wait for response
    byte[] response = send( link, buffer, timeout);
    ICompressor compressor = map.get( link).compressor;
    IModelObject element = compressor.decompress( response, 0);
    log.debugf( "handleAttachResponse: %s\n", element.getType());
    handleAttachResponse( link, element);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAttachRequest( ILink link, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    log.debugf( "handleAttachRequest: %s", new String( bytes));
    handleAttachRequest( link, new String( bytes));
  }
  
  /**
   * Handle an attach request.
   * @param link The link.
   * @param xpath The xpath.
   */
  protected void handleAttachRequest( ILink link, String xpath)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new AttachRunnable( link, xpath));
  }

  /**
   * Send an attach response message.
   * @param link The link.
   * @param element The element.
   */
  public final void sendAttachResponse( ILink link, IModelObject element) throws IOException
  {
    log.debugf( "sendAttachResponse: %s", element.getType());
    initialize( buffer);
    ICompressor compressor = map.get( link).compressor;
    byte[] bytes = compressor.compress( element);
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachResponse, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAttachResponse( ILink link, ByteBuffer buffer, int length)
  {
    queueResponse( buffer, length);
  }
  
  /**
   * Handle an attach response.
   * @param link The link.
   * @param element The element.
   */
  protected void handleAttachResponse( ILink link, IModelObject element)
  {
    IExternalReference attached = (IExternalReference)map.get( link).element;
    if ( attached != null)
    {
      ICachingPolicy cachingPolicy = attached.getCachingPolicy();
      cachingPolicy.update( attached, decode( link, element));
    }
  }
  
  /**
   * Send an detach request message.
   * @param link The link.
   * @param xpath The xpath.
   */
  public final void sendDetachRequest( ILink link, String xpath) throws IOException
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.detachRequest, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDetachRequest( ILink link, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    log.debugf( "handleDetachRequest: %s", new String( bytes));
    handleDetachRequest( link, new String( bytes));
  }
  
  /**
   * Handle an attach request.
   * @param link The link.
   * @param xpath The xpath.
   */
  protected void handleDetachRequest( ILink link, String xpath)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new DetachRunnable( link, xpath));
  }
  
  /**
   * Send an sync request message.
   * @param reference The local reference.
   */
  public final void sendSyncRequest( IExternalReference reference) throws IOException
  {
    log.debugf( "sendSyncRequest: %s", reference.getType());
    
    KeyRecord key = keys.get( reference);
    
    initialize( buffer);
    byte[] bytes = key.key.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.syncRequest, bytes.length);
    
    // send and wait for response
    send( key.link, buffer, timeout);
    handleSyncResponse( key.link);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSyncRequest( ILink link, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    handleSyncRequest( link, new String( bytes));
  }
  
  /**
   * Handle a sync request.
   * @param link The link.
   * @param key The reference key.
   */
  protected void handleSyncRequest( ILink sender, String key)
  {
    log.debugf( "handleSyncRequest: %s", key);
    
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new SyncRunnable( sender, key));
  }
  
  /**
   * Send an sync response message.
   * @param link The link.
   */
  public final void sendSyncResponse( ILink link) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.syncResponse, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSyncResponse( ILink link, ByteBuffer buffer, int length)
  {
    queueResponse( buffer, length);
  }
  
  /**
   * Handle a sync response.
   * @param link The link.
   */
  protected void handleSyncResponse( ILink link)
  {
    // Nothing to do here
  }
  
  /**
   * Send an add child message.
   * @param link The link.
   * @param xpath The xpath.
   * @param element The element.
   * @param index The insertion index.
   */
  public final void sendAddChild( ILink link, String xpath, IModelObject element, int index) throws IOException
  {
    log.debugf( "sendAddChild: %s, %s", xpath, element.getType());    
    
    initialize( buffer);
    int length = writeString( xpath);
    length += writeElement( map.get( link).compressor, element);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.addChild, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAddChild( ILink link, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    byte[] bytes = readBytes( buffer, false);
    int index = buffer.getInt();
    handleAddChild( link, xpath, bytes, index);
  }
  
  /**
   * Handle an add child message.
   * @param link The link.
   * @param xpath The path from the root to the parent.
   * @param child The child that was added as a compressed byte array.
   * @param index The insertion index.
   */
  protected void handleAddChild( ILink link, String xpath, byte[] child, int index)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new AddChildEvent( link, xpath, child, index));
  }
  
  /**
   * Process an add child event in the appropriate thread.
   * @param link The link.
   * @param xpath The xpath of the parent.
   * @param bytes The child that was added.
   * @param index The index of insertion.
   */
  private void processAddChild( ILink link, String xpath, byte[] bytes, int index)
  {
    try
    {
      updating = link;
      
      IModelObject attached = map.get( link).element;
      if ( attached == null) return;
      
      IExpression parentExpr = XPath.createExpression( xpath);
      if ( parentExpr != null)
      {
        IModelObject parent = parentExpr.queryFirst( attached);
        IModelObject child = map.get( link).compressor.decompress( bytes, 0);
        log.debugf( "processAddChild: %s, %s", xpath, child.getType());            
        if ( parent != null) 
        {
          IModelObject childElement = decode( link, child);
          parent.addChild( childElement, index);
        }
      }
    }
    finally
    {
      updating = null;
    }
  }
  
  /**
   * Send an add child message.
   * @param link The link.
   * @param xpath The xpath.
   * @param index The insertion index.
   */
  public final void sendRemoveChild( ILink link, String xpath, int index) throws IOException
  {
    log.debugf( "sendRemoveChild: %s, %d", xpath, index);    
    
    initialize( buffer);
    int length = writeString( xpath);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.removeChild, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleRemoveChild( ILink link, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    int index = buffer.getInt();
    handleRemoveChild( link, xpath, index);
  }
  
  /**
   * Handle an remove child message.
   * @param link The link.
   * @param xpath The path from the root to the parent.
   * @param index The insertion index.
   */
  protected void handleRemoveChild( ILink link, String xpath, int index)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new RemoveChildEvent( link, xpath, index));
  }
  
  /**
   * Process an remove child event in the appropriate thread.
   * @param link The link.
   * @param xpath The xpath of the parent.
   * @param index The index of insertion.
   */
  private void processRemoveChild( ILink link, String xpath, int index)
  {
    try
    {
      updating = link;
      
      IModelObject attached = map.get( link).element;
      if ( attached == null) return;
      
      IExpression parentExpr = XPath.createExpression( xpath);
      if ( parentExpr != null)
      {
        IModelObject parent = parentExpr.queryFirst( attached);
        if ( parent != null) 
        {
          IModelObject removed = parent.removeChild( index);
          log.debugf( "processRemoveChild: %s, %s", xpath, (removed != null)? removed.getType(): "null");          
          if ( removed instanceof IExternalReference) keys.remove( removed);
        }
      }
    }
    finally
    {
      updating = null;
    }    
  }
  
  /**
   * Send an change attribute message.
   * @param link The link.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  public final void sendChangeAttribute( ILink link, String xpath, String attrName, Object value) throws IOException
  {
    byte[] bytes = serialize( value);
    
    initialize( buffer);
    int length = writeString( xpath);
    length += writeString( attrName);
    length += writeBytes( bytes, 0, bytes.length, true);
    finalize( buffer, Type.changeAttribute, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleChangeAttribute( ILink link, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    byte[] attrValue = readBytes( buffer, true);
    handleChangeAttribute( link, xpath, attrName, deserialize( attrValue));
  }
  
  /**
   * Handle an attribute change message.
   * @param link The link.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   * @param attrValue The attribute value.
   */
  protected void handleChangeAttribute( ILink link, String xpath, String attrName, Object attrValue)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new ChangeAttributeEvent( link, xpath, attrName, attrValue));
  }
  
  /**
   * Process an change attribute event in the appropriate thread.
   * @param link The link.
   * @param xpath The xpath of the element.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  private void processChangeAttribute( ILink link, String xpath, String attrName, Object attrValue)
  {
    try
    {
      updating = link;
      
      IModelObject attached = map.get( link).element;
      if ( attached == null) return;
      
      // the empty string and null string both serialize as byte[ 0]
      if ( attrValue == null) attrValue = "";
      
      IExpression elementExpr = XPath.createExpression( xpath);
      if ( elementExpr != null)
      {
        IModelObject element = elementExpr.queryFirst( attached);
        if ( element != null) element.setAttribute( attrName, attrValue);
      }
    }
    finally
    {
      updating = null;
    }        
  }
  
  /**
   * Send a clear attribute message.
   * @param link The link.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   */
  public final void sendClearAttribute( ILink link, String xpath, String attrName) throws IOException
  {
    initialize( buffer);
    int length = writeString( xpath);
    length += writeString( attrName);
    finalize( buffer, Type.clearAttribute, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleClearAttribute( ILink link, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    handleClearAttribute( link, xpath, attrName);
  }
  
  /**
   * Handle an attribute change message.
   * @param link The link.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   */
  protected void handleClearAttribute( ILink link, String xpath, String attrName)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new ClearAttributeEvent( link, xpath, attrName));
  }

  /**
   * Process a clear attribute event in the appropriate thread.
   * @param link The link.
   * @param xpath The xpath of the element.
   * @param attrName The name of the attribute.
   */
  private void processClearAttribute( ILink link, String xpath, String attrName)
  {
    try
    {
      updating = link;
      
      IModelObject attached = map.get( link).element;
      if ( attached == null) return;
      
      IExpression elementExpr = XPath.createExpression( xpath);
      if ( elementExpr != null)
      {
        IModelObject element = elementExpr.queryFirst( attached);
        if ( element != null) element.removeAttribute( attrName);
      }
    }
    finally
    {
      updating = null;
    }    
  }
  
  /**
   * Send a change dirty message.
   * @param link The link.
   * @param xpath The xpath.
   * @param dirty The dirty state.
   */
  public final void sendChangeDirty( ILink link, String xpath, boolean dirty) throws IOException
  {
    initialize( buffer);
    int length = writeString( xpath);
    buffer.put( dirty? (byte)1: 0); length++;
    finalize( buffer, Type.changeDirty, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleChangeDirty( ILink link, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    boolean dirty = buffer.get() != 0;
    handleChangeDirty( link, xpath, dirty);
  }
  
  /**
   * Handle a change dirty message.
   * @param link The link.
   * @param xpath The xpath from the root to the element.
   * @param dirty The dirty state.
   */
  protected void handleChangeDirty( ILink link, String xpath, boolean dirty)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new ChangeDirtyEvent( link, xpath, dirty));
  }

  /**
   * Process a change dirty event in the appropriate thread.
   * @param link The link.
   * @param xpath The xpath of the element.
   * @param dirty The new dirty state.
   */
  private void processChangeDirty( ILink link, String xpath, boolean dirty)
  {
    try
    {
      updating = link;
      
      IModelObject attached = map.get( link).element;
      if ( attached == null) return;
      
      IExpression elementExpr = XPath.createExpression( xpath);
      if ( elementExpr != null)
      {
        IModelObject element = elementExpr.queryFirst( attached);
        if ( element != null)
        {
          IExternalReference reference = (IExternalReference)element;
          reference.setDirty( dirty);
        }
      }
    }
    finally
    {
      updating = null;
    }    
  }

  /**
   * Send a query request message.
   * @param link The link.
   * @param xpath The xpath.
   */
  public final void sendQueryRequest( ILink link, String xpath) throws IOException
  {
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.queryRequest, bytes.length);
    send( link, buffer, timeout);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleQueryRequest( ILink link, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    handleQueryRequest( link, new String( bytes));
  }
  
  /**
   * Handle a query requset.
   * @param link The link.
   * @param xpath The query.
   */
  protected void handleQueryRequest( ILink link, String xpath)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new QueryRunnable( link, xpath));
  }
  
  /**
   * Send a query response message.
   * @param link The link.
   * @param result The result.
   */
  public final void sendQueryResponse( ILink link, Object result) throws IOException
  {
    initialize( buffer);
    byte[] bytes = serialize( result);
    buffer.put( bytes);
    finalize( buffer, Type.queryResponse, bytes.length);

    // wait for response
    byte[] response = send( link, buffer, timeout);
    handleQueryResponse( link, response);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleQueryResponse( ILink link, ByteBuffer buffer, int length)
  {
    queueResponse( buffer, length);
  }
  
  /**
   * Handle a query requset.
   * @param link The link.
   * @param bytes The serialized query result.
   */
  protected void handleQueryResponse( ILink link, byte[] bytes)
  {
    // TODO: finish this
  }
  
  /**
   * Send an execute request message.
   * @param link The link.
   * @param script The script to execute.
   */
  public final void sendExecuteRequest( ILink link, IModelObject script) throws IOException
  {
    log.debugf( "sendExecuteRequest: %s", script.getID());
    
    initialize( buffer);
    ICompressor compressor = map.get( link).compressor;
    byte[] bytes = compressor.compress( script);
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.executeRequest, bytes.length);

    // send and wait for response
    byte[] response = send( link, buffer, timeout);
    IModelObject element = compressor.decompress( response, 0);
    log.debugf( "handleExecuteResponse: %s\n", element.getType());
    handleExecuteResponse( link, element);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleExecuteRequest( ILink link, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    ICompressor compressor = map.get( link).compressor;
    IModelObject script = compressor.decompress( bytes, 0);
    log.debugf( "handleExecuteRequest: %s", script.getID());
    handleExecuteRequest( link, script);
  }
  
  /**
   * Handle an execute request.
   * @param link The link.
   * @param script The script to execute.
   */
  @SuppressWarnings("unchecked")
  protected void handleExecuteRequest( ILink link, IModelObject script)
  {
    ModelObject response = null;
    
    try
    {
      XActionDocument doc = new XActionDocument( script);
      ScriptAction action = doc.createScript();
      Object[] results = action.run( context);
      
      response = new ModelObject( "response");
      if ( results != null && results.length > 0)
      {
        Object result = results[ 0];
        if ( result instanceof List) 
        {
          Xlate.set( response, "type", IModelObject.class.getName());
          for( IModelObject node: (List<IModelObject>)result)
            response.addChild( node.cloneTree());
        }
        else
        {
          Xlate.set( response, "type", result.getClass().getName());
          response.setValue( result);
        }
      }
    }
    catch( Throwable t)
    {
      log.exception( t);
      response = new ModelObject( "exception");
      Xlate.set( response, "class", t.getClass().getName());
      Xlate.set( response, t.getMessage());
    }
    
    try
    {
      sendExecuteResponse( link, response);
    } 
    catch( IOException e)
    {
      log.exception( e);
    }
  }

  /**
   * Send an attach response message.
   * @param link The link.
   * @param element The element.
   */
  public final void sendExecuteResponse( ILink link, IModelObject element) throws IOException
  {
    log.debugf( "sendExecuteResponse: %s", element.getType());
    initialize( buffer);
    ICompressor compressor = map.get( link).compressor;
    byte[] bytes = compressor.compress( element);
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.executeResponse, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleExecuteResponse( ILink link, ByteBuffer buffer, int length)
  {
    queueResponse( buffer, length);
  }
  
  /**
   * Handle an attach response.
   * @param link The link.
   * @param element The element.
   */
  protected void handleExecuteResponse( ILink link, IModelObject element)
  {
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   */
  public final void sendDebugStepIn( ILink link) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepIn, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepIn( ILink link, ByteBuffer buffer, int length)
  {
    handleDebugStepIn( link);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   */
  protected void handleDebugStepIn( ILink link)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepIn();
      }
    });
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   */
  public final void sendDebugStepOver( ILink link) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOver, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepOver( ILink link, ByteBuffer buffer, int length)
  {
    handleDebugStepOver( link);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   */
  protected void handleDebugStepOver( ILink link)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepOver();
      }
    });
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   */
  public final void sendDebugStepOut( ILink link) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOut, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepOut( ILink link, ByteBuffer buffer, int length)
  {
    handleDebugStepOut( link);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   */
  protected void handleDebugStepOut( ILink link)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepOut();
      }
    });
  }

  /**
   * Send and wait for a response.
   * @param link The link.
   * @param buffer The buffer to send.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response buffer.
   */
  private byte[] send( ILink link, ByteBuffer buffer, int timeout) throws IOException
  {
    try
    {
      link.send( buffer);
      byte[] response = responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
      if ( response != null) return response;
    }
    catch( InterruptedException e)
    {
      return null;
    }
    
    throw new IOException( "Network request timeout.");
  }
  
  /**
   * Queue a synchronous response.
   * @param buffer The buffer containing the response.
   * @param length The length of the response.
   */
  private void queueResponse( ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    try { responseQueue.put( bytes);} catch( InterruptedException e) {}
  }
  
  /**
   * Returns the serialized attribute value.
   * @param object The attribute value.
   * @return Returns the serialized attribute value.
   */
  private byte[] serialize( Object object)
  {
    if ( object == null) return new byte[ 0];
    
    // use java serialization
    if ( object instanceof Serializable)
    {
      try
      {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream( bs);
        os.writeObject( object);
        os.close();
        return bs.toByteArray();
      }
      catch( Exception e)
      {
        log.exception( e);
      }
    }
    
    return new byte[ 0];
  }
  
  /**
   * Returns the deserialized attribute value.
   * @param bytes The serialized attribute value.
   * @return Returns the deserialized attribute value.
   */
  private Object deserialize( byte[] bytes)
  {
    if ( bytes.length == 0) return null;
    
    // use java serialization
    try
    {
      ByteArrayInputStream bs = new ByteArrayInputStream( bytes);
      ObjectInputStream os = new ObjectInputStream( bs);
      return os.readObject();
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to deserialize object.", e);
    }
  }
  
  /**
   * Reserve the maximum amount of space for the header.
   * @param buffer The buffer.
   */
  public static void initialize( ByteBuffer buffer)
  {
    buffer.clear();
    buffer.position( 5);
  }
  
  /**
   * Read the message type from the buffer.
   * @param buffer The buffer.
   * @return Returns the message type.
   */
  public static Type readMessageType( ByteBuffer buffer)
  {
    int index = buffer.get() & 0x7f;
    return Type.values()[ index];
  }
  
  /**
   * Read the message length from the buffer.
   * @param buffer The buffer.
   * @return Returns the message length.
   */
  public static int readMessageLength( ByteBuffer buffer)
  {
    int mask = buffer.get( buffer.position() - 1) & 0x80;
    if ( mask == 0) return buffer.get();
    return buffer.getInt();
  }
  
  /**
   * Finalize the message by writing the header and preparing the buffer to be read.
   * @param buffer The buffer.
   * @param type The message type.
   * @param length The message length.
   */
  public static void finalize( ByteBuffer buffer, Type type, int length)
  {
    buffer.limit( buffer.position());
    if ( length < 128)
    {
      buffer.put( 3, (byte)type.ordinal());
      buffer.put( 4, (byte)length);
      buffer.position( 3);
    }
    else
    {
      buffer.put( 0, (byte)(type.ordinal() | 0x80));
      buffer.putInt( 1, length);
      buffer.position( 0);
    }
  }
  
  /**
   * Read a length from the buffer.
   * @param buffer The buffer.
   * @param small True means 1 or 4 bytes. False means 2 or 4 bytes.
   * @return Returns the length.
   */
  private static int readLength( ByteBuffer buffer, boolean small)
  {
    buffer.mark();
    if ( small)
    {
      int length = buffer.get();
      if ( length >= 0) return length;
    }
    else
    {
      int length = buffer.getShort();
      if ( length >= 0) return length;
    }
    
    buffer.reset();
    return -buffer.getInt();
  }
  
  /**
   * Write a length into the buffer in either one, two or four bytes.
   * @param buffer The buffer.
   * @param length The length.
   * @param small True means 1 or 4 bytes. False means 2 or 4 bytes.
   */
  private static int writeLength( ByteBuffer buffer, int length, boolean small)
  {
    if ( small)
    {
      if ( length < 128)
      {
        buffer.put( (byte)length);
        return 1;
      }
    }
    else
    {
      if ( length < 32768)
      {
        buffer.putShort( (short)length);
        return 2;
      }
    }
    
    buffer.putInt( -length);
    return 4;
  }
  
  /**
   * Read a block of bytes from the message.
   * @param buffer The buffer.
   * @return Returns the bytes read.
   */
  public static byte[] readBytes( ByteBuffer buffer, boolean small)
  {
    int length = readLength( buffer, small);
    byte[] bytes = new byte[ length];
    buffer.get( bytes, 0, length);
    return bytes;
  }
  
  /**
   * Write a block of bytes to the message.
   * @param buffer The buffer.
   * @param bytes The bytes.
   * @param offset The offset.
   * @param length The length.
   */
  public int writeBytes( byte[] bytes, int offset, int length, boolean small)
  {
    int required = buffer.position() + length;
    if ( required >= buffer.limit())
    {
      buffer.flip();
      ByteBuffer larger = ByteBuffer.allocateDirect( (int)(required * 1.5));
      larger.put( buffer);
      buffer = larger;
    }
    
    int prefix = writeLength( buffer, length, small);
    buffer.put( bytes, offset, length);
    return prefix + length;
  }
  
  /**
   * Read an String from the message.
   * @param buffer The buffer.
   * @return Returns the string.
   */
  public static String readString( ByteBuffer buffer)
  {
    return new String( readBytes( buffer, true));
  }
  
  /**
   * Write a String into the message.
   * @param buffer The buffer.
   * @param string The string.
   */
  public int writeString( String string)
  {
    byte[] bytes = string.getBytes();
    return writeBytes( bytes, 0, bytes.length, true);
  }
  
  /**
   * Read an IModelObject from the message.
   * @param compressor The compressor.
   * @param buffer The buffer.
   * @return Returns the element.
   */
  public IModelObject readElement( ICompressor compressor, ByteBuffer buffer)
  {
    byte[] bytes = readBytes( buffer, false);
    IModelObject object = compressor.decompress( bytes, 0);
    return object;
  }

  /**
   * Write an IModelObject to the message.
   * @param compressor The compressor;
   * @param element The element.
   */
  public int writeElement( ICompressor compressor, IModelObject element)
  {
    byte[] bytes = compressor.compress( element);
    return writeBytes( bytes, 0, bytes.length, false);
  }

  /**
   * Encode the specified element.
   * @param link The link.
   * @param root True if the element is the root of the attachment.
   * @param element The element to be copied.
   * @return Returns the copy.
   */
  protected IModelObject encode( ILink link, IModelObject element, boolean root)
  {
    IModelObject encoded = null;
    
    if ( element instanceof IExternalReference)
    {
      IExternalReference lRef = (IExternalReference)element;
      encoded = new ModelObject( lRef.getType());
      
      // index reference so it can be synced remotely
      if ( !root) encoded.setAttribute( "net:key", index( link, lRef));
      
      // enumerate static attributes for client
      for( String attrName: lRef.getStaticAttributes())
      {
        IModelObject entry = new ModelObject( "net:static");
        entry.setValue( attrName);
        encoded.addChild( entry);
      }
      
      // copy only static attributes if reference is dirty
      if ( element.isDirty())
      {
        Xlate.set( encoded, "net:dirty", true);
        
        // copy static attributes
        for( String attrName: lRef.getStaticAttributes())
        {
          Object attrValue = element.getAttribute( attrName);
          if ( attrValue != null) encoded.setAttribute( attrName, attrValue);
        }
      }
    }
    else
    {
      encoded = new ModelObject( element.getType());
    }
    
    // copy all attributes and children
    if ( !(element instanceof IExternalReference) || !element.isDirty())
    {
      // copy attributes
      ModelAlgorithms.copyAttributes( element, encoded);
      
      // copy children
      for( IModelObject child: element.getChildren())
      {
        encoded.addChild( encode( link, child, false));
      }
    }
    
    return encoded;
  }
    
  /**
   * Interpret the content of the specified server encoded subtree.
   * @param link The link.
   * @param root The root of the encoded subtree.
   * @return Returns the decoded element.
   */
  protected IModelObject decode( ILink link, IModelObject root)
  {
    //System.out.println( ((ModelObject)root).toXml());
    
    Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
    DepthFirstIterator iter = new DepthFirstIterator( root);
    while( iter.hasNext())
    {
      IModelObject lNode = iter.next();
      if ( lNode.isType( "net:static")) continue;
      
      IModelObject rNode = map.get( lNode);
      IModelObject rParent = map.get( lNode.getParent());
      
      String key = Xlate.get( lNode, "net:key", (String)null);
      if ( key != null)
      {
        ExternalReference reference = new ExternalReference( lNode.getType());
        ModelAlgorithms.copyAttributes( lNode, reference);
        reference.removeAttribute( "net:key");
        reference.removeChildren( "net:static");
        
        NetKeyCachingPolicy cachingPolicy = new NetKeyCachingPolicy( this);
        cachingPolicy.setStaticAttributes( getStaticAttributes( lNode));
        reference.setCachingPolicy( cachingPolicy);
        
        boolean dirty = Xlate.get( reference, "net:dirty", false);
        reference.removeAttribute( "net:dirty");
        reference.setDirty( dirty);
        
        KeyRecord keyRecord = new KeyRecord();
        keyRecord.key = key;
        keyRecord.link = link;
        keys.put( reference, keyRecord);
        
        rNode = reference;
      }
      else if ( lNode == root)
      {
        rNode = new ModelObject( root.getType());
        ModelAlgorithms.copyAttributes( lNode, rNode);
      }
      else
      {
        rNode = lNode;
      }
      
      map.put( lNode, rNode);
      if ( rParent != null) rParent.addChild( rNode);
    }
    
    return map.get( root);
  }
  
  /**
   * Index the specified node.
   * @param link The link.
   * @param node The node.
   * @return Returns the key.
   */
  private String index( ILink link, IModelObject node)
  {
    String key = Identifier.generate( random, 13);
    map.get( link).index.put( key, node);
    return key;
  }
  
  /**
   * Returns a list of the static attributes encoded for the specified element.
   * @param element The encoded element.
   */
  private List<String> getStaticAttributes( IModelObject element)
  {
    List<String> statics = new ArrayList<String>();
    List<IModelObject> children = element.getChildren( "net:static");
    for( IModelObject child: children) statics.add( Xlate.get( child, ""));
    return statics;
  }
  
  private final class SyncRunnable implements Runnable
  {
    public SyncRunnable( ILink sender, String key)
    {
      this.sender = sender;
      this.key = key;
    }
    
    public void run()
    {
      IModelObject reference = map.get( sender).index.get( key);
      if ( reference != null)
      {
        try
        {
          reference.getChildren();
          sendSyncResponse( sender);
        }
        catch( IOException e)
        {
          log.exception( e);
        }
      }
    }
    
    private ILink sender;
    private String key;
  }
  
  private final class AddChildEvent implements Runnable
  {
    public AddChildEvent( ILink link, String xpath, byte[] child, int index)
    {
      this.link = link;
      this.xpath = xpath;
      this.child = child;
      this.index = index;
    }
    
    public void run()
    {
      processAddChild( link, xpath, child, index);
    }
    
    private ILink link;
    private String xpath;
    private byte[] child;
    private int index;
  }
  
  private final class RemoveChildEvent implements Runnable
  {
    public RemoveChildEvent( ILink link, String xpath, int index)
    {
      this.link = link;
      this.xpath = xpath;
      this.index = index;
    }
    
    public void run()
    {
      processRemoveChild( link, xpath, index);
    }
    
    private ILink link;
    private String xpath;
    private int index;
  }
  
  private final class ChangeAttributeEvent implements Runnable
  {
    public ChangeAttributeEvent( ILink link, String xpath, String attrName, Object attrValue)
    {
      this.link = link;
      this.xpath = xpath;
      this.attrName = attrName;
      this.attrValue = attrValue;
    }
    
    public void run()
    {
      processChangeAttribute( link, xpath, attrName, attrValue);
    }
    
    private ILink link;
    private String xpath;
    private String attrName;
    private Object attrValue;
  }
  
  private final class ClearAttributeEvent implements Runnable
  {
    public ClearAttributeEvent( ILink link, String xpath, String attrName)
    {
      this.link = link;
      this.xpath = xpath;
      this.attrName = attrName;
    }
    
    public void run()
    {
      processClearAttribute( link, xpath, attrName);
    }
    
    private ILink link;
    private String xpath;
    private String attrName;
  }
  
  private final class ChangeDirtyEvent implements Runnable
  {
    public ChangeDirtyEvent( ILink link, String xpath, boolean dirty)
    {
      this.link = link;
      this.xpath = xpath;
      this.dirty = dirty;
    }
    
    public void run()
    {
      processChangeDirty( link, xpath, dirty);
    }
    
    private ILink link;
    private String xpath;
    private boolean dirty;
  }
  
  private final class AttachRunnable implements Runnable
  {
    public AttachRunnable( ILink sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      try
      {
        doAttach( sender, xpath);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }

    private ILink sender;
    private String xpath;
  }
  
  private final class DetachRunnable implements Runnable
  {
    public DetachRunnable( ILink sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      doDetach( sender, xpath);
    }

    private ILink sender;
    private String xpath;
  }
  
  private final class QueryRunnable implements Runnable
  {
    public QueryRunnable( ILink sender, String xpath)
    {
      this.sender = sender;
      this.xpath = xpath;
    }
    
    public void run()
    {
      try
      {
        doQuery( sender, xpath);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }
    
    private ILink sender;
    private String xpath;
  }
  
  protected class Listener extends NonSyncingListener
  {
    public Listener( ILink sender, String xpath, IModelObject root)
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
      
      if ( updating == sender) return;
      
      try
      {
        IModelObject clone = encode( sender, child, false);
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
      
      if ( updating == sender) return;
      
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
      if ( updating == sender) return;

      // make sure relative path is constructed correctly
      boolean revert = attrName.equals( "id");
      if ( revert) object.getModel().revert();
      String xpath = createPath( root, object);
      if ( revert) object.getModel().restore();
      
      try
      {
        sendChangeAttribute( sender, xpath, attrName, newValue);
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
      if ( updating == sender) return;
      
      // make sure relative path is constructed correctly
      boolean revert = attrName.equals( "id");
      if ( revert) object.getModel().revert();
      String xpath = createPath( root, object);
      if ( revert) object.getModel().restore();
      
      try
      {
        sendClearAttribute( sender, xpath, attrName);
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
      //
      // Do not send notifications for network external references, otherwise the remote reference will
      // be marked not-dirty before a sync request is sent and the sync request will have no effect.
      //
      if ( updating == sender || keys.get( object) != null) return;
      
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

    private ILink sender;
    private String xpath;
    private IModelObject root;
  };
  
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
  
  private class KeyRecord
  {
    String key;
    ILink link;
  }
  
  protected class ConnectionInfo
  {
    public ConnectionInfo()
    {
      index = new HashMap<String, IModelObject>();
      compressor = new TabularCompressor( PostCompression.zip);
    }
    
    public String xpath;
    public IModelObject element;
    public Map<String, IModelObject> index;
    public TabularCompressor compressor;
    public Listener listener;
  }
  
  private static Log log = Log.getLog( "org.xmodel.net");

  protected IContext context;
  private ByteBuffer buffer;
  private BlockingQueue<byte[]> responseQueue;
  private int timeout;
  private Random random;
  protected Map<IModelObject, KeyRecord> keys;
  protected Stack<IDispatcher> dispatchers;
  protected Map<ILink, ConnectionInfo> map;
  private ILink updating;
}
