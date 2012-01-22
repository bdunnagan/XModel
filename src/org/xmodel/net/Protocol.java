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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.xmodel.DepthFirstIterator;
import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ImmediateDispatcher;
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
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * The protocol class for the NetworkCachingPolicy protocol.
 */
public class Protocol implements ILink.IListener
{
  public final static short version = 1;
  
  public enum Type
  {
    sessionOpenRequest,
    sessionOpenResponse,
    sessionCloseRequest,
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
    debugGo,
    debugStop,
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
    this.sessions = Collections.synchronizedMap( new HashMap<ILink, List<SessionInfo>>());
    this.sessionInitQueues = Collections.synchronizedMap( new HashMap<String, BlockingQueue<byte[]>>());
    this.keys = Collections.synchronizedMap( new WeakHashMap<IModelObject, KeyRecord>());      
    
    this.timeout = timeout;
    this.random = new Random();
    
    // allocate less than standard mtu
    buffer = ByteBuffer.allocate( 4096);
    buffer.order( ByteOrder.BIG_ENDIAN);
    
    dispatcher = new ImmediateDispatcher();
    packageNames = new ArrayList<String>();
  }
  
  /**
   * Set the context in which remote xpath expressions will be bound.
   * @param context The context.
   */
  public void setServerContext( IContext context)
  {
    if ( debugEnabled) 
    {
      this.context.getScope().clear( "debug");
      context.set( "debug", new ModelObject( "debug"));
    }
    
    this.context = context;
    dispatcher = context.getModel().getDispatcher();
  }
  
  /**
   * Enable or disable debugging via this server instance.
   * @param enable True if debugging should be enabled.
   */
  public void setEnableDebugging( boolean enable)
  {
    debugEnabled = enable;
    
    if ( debugEnabled && context != null) 
    {
      context.set( "debug", new ModelObject( "debug"));
    }
  }
  
  /**
   * Set the dispatcher. It is not necessary to set the dispatcher using this method if either the
   * <code>setServerContext</code> method or <code>attach</code> method is called. 
   * @param dispatcher The dispatcher.
   */
  public void setDispatcher( IDispatcher dispatcher)
  {
    this.dispatcher = dispatcher;
  }

  /**
   * Add an XAction package to the packages that will be searched when a remote XAction invocation request is received.
   * This method is not thread-safe and should be called on the server-side before the server is started.
   * @param packageName The package name.
   */
  public void addPackage( String packageName)
  {
    packageNames.add( packageName);
  }
  
  /**
   * Open a session over the specified link and return the session.
   * @param link The link.
   * @return Returns the session.
   */
  public Session openSession( ILink link) throws IOException
  {
    int id = sendSessionOpenRequest( link, version);
    
    SessionInfo info = new SessionInfo();
    List<SessionInfo> list = sessions.get( link);
    if ( list == null)
    {
      list = new ArrayList<SessionInfo>( 1);
      sessions.put( link, list);
    }
    
    for( int i=list.size(); i<=id; i++) list.add( null);
    list.set( id, info);
    
    return new Session( this, link, id);
  }
  
  /**
   * Close a session over the specified link and return session.
   * @param link The link.
   * @param session The session number.
   */
  public void closeSession( ILink link, int session) throws IOException
  {
    sendSessionCloseRequest( link, session);
  }
  
  /**
   * Attach to the element on the specified xpath.
   * @param link The link.
   * @param session The session number.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( ILink link, int session, String xpath, IExternalReference reference) throws IOException
  {
    if ( link != null && link.isOpen())
    {
      SessionInfo info = getSession( link, session);
      if ( info.xpath != null) throw new IOException( "Protocol only supports one concurrent attach operation per session.");
      
      info.xpath = xpath;
      info.element = reference;
      info.isAttachClient = true;
      info.dispatcher = reference.getModel().getDispatcher();
      
      sendAttachRequest( link, session, xpath);
      
      info.listener = new Listener( link, session, xpath, reference);
      info.listener.install( reference);
    }
    else
    {
      throw new IOException( "Link not open.");
    }
  }

  /**
   * Detach from the element on the specified path.
   * @param link The link.
   * @param session The session number.
   */
  public void detach( ILink link, int session) throws IOException
  {
    sendDetachRequest( link, session);
    
    SessionInfo info = getSession( link, session);
    if ( info != null)
    {
      info.xpath = null;
      info.element = null;
      info.dispatcher = null;
    }
  }

  /**
   * Perform a remote query.
   * @param link The link.
   * @param session The session number.
   * @param context The local context.
   * @param query The query string.
   * @param timeout The timeout to wait for a response.
   * @return Returns null or the response.
   */
  public Object query( ILink link, int session, IContext context, String query, int timeout) throws IOException
  {
    if ( link != null && link.isOpen())
    {
      return sendQueryRequest( link, session, context, query, timeout);
    }
    else
    {
      throw new IOException( "Link not open.");
    }
  }

  /**
   * Perform a remote invocation of the specified script.
   * @param link The link.
   * @param session The session number.
   * @param context The local execution context.
   * @param variables The variables to be passed.
   * @param script The script to be executed.
   * @param timeout The timeout to wait for a response.
   * @return Returns null or the response.
   */
  public Object[] execute( ILink link, int session, StatefulContext context, String[] variables, IModelObject script, int timeout) throws IOException
  {
    if ( link != null && link.isOpen())
    {
      return sendExecuteRequest( link, session, context, variables, script, timeout);
    }
    else
    {
      throw new IOException( "Link not open.");
    }
  }

  /**
   * Find the element on the specified xpath and attach listeners.
   * @param sender The sender.
   * @param session The session number.
   * @param xpath The xpath expression.
   */
  protected void doAttach( ILink sender, int session, String xpath) throws IOException
  {
    try
    {
      IExpression expr = XPath.compileExpression( xpath);
      IModelObject target = expr.queryFirst( context);
      IModelObject copy = null;
      if ( target != null)
      {
        SessionInfo info = getSession( sender, session);
        info.xpath = xpath;
        info.element = target;
        
        // make sure target is synced since that is what we are requesting
        target.getChildren();
        copy = encode( sender, session, target, true);
        
        info.listener = new Listener( sender, session, xpath, target);
        info.listener.install( target);
      }
      sendAttachResponse( sender, session, copy);
    }
    catch( Exception e)
    {
      sendError( sender, session, e.getMessage());
    }
  }
  
  /**
   * Remove listeners from the element on the specified xpath.
   * @param sender The sender.
   * @param session The session number.
   */
  protected void doDetach( ILink sender, int session)
  {
    SessionInfo info = getSession( sender, session);
    if ( info != null)
    {
      info.index.clear();
      
      if ( info.listener != null)
      {
        info.listener.uninstall();
        info.listener = null;
      }
      
      info.xpath = null;
      info.element = null;
    }
  }

  /**
   * Execute the specified query and return the result.
   * @param sender The sender.
   * @param session The session number.
   * @param request The query request.
   */
  protected void doQuery( ILink sender, int session, IModelObject request) throws IOException
  {
    try
    {
      IExpression query = QueryProtocol.readRequest( request, context);
      
      Object result = null;
      switch( query.getType( context))
      {
        case NODES:   result = query.evaluateNodes( context); break;
        case STRING:  result = query.evaluateString( context); break;
        case NUMBER:  result = query.evaluateNumber( context); break;
        case BOOLEAN: result = query.evaluateBoolean( context); break;
      }

      sendQueryResponse( sender, session, result);
    }
    catch( PathSyntaxException e)
    {
      try { sendError( sender, session, e.getMessage());} catch( IOException e2) {}
    }
  }
  
  /**
   * Execute the specified script.
   * @param sender The sender.
   * @param session The session number.
   * @param context The execution context.
   * @param script The script.
   */
  protected void doExecute( ILink sender, int session, IContext context, IModelObject script)
  {
    XActionDocument doc = new XActionDocument( script);
    for( String packageName: packageNames)
      doc.addPackage( packageName);

    Object[] results = null;
    
    try
    {
      IXAction action = doc.getAction( script);
      if ( action == null) 
      {
        throw new XActionException( String.format(
          "Unable to resolve IXAction class: %s.", script.getType()));
      }
      
      results = action.run( context);
    }
    catch( Throwable t)
    {
      log.exception( t);
      
      try
      {
        sendError( sender, session, String.format( "%s: %s", t.getClass().getName(), t.getMessage()));
      }
      catch( IOException e)
      {
        log.exception( e);
      }
    }
    
    try
    {
      sendExecuteResponse( sender, session, context, results);
    } 
    catch( IOException e)
    {
      log.exception( e);
    }
  }
  
  /**
   * Close the specified session.
   * @param sender The sender.
   * @param session The session number.
   */
  protected void doSessionClose( ILink sender, int session)
  {
    deallocSession( sender, session);
  }
  
  /**
   * Close the specified session.
   * @param link The link.
   * @param session The session.
   */
  protected void doClose( ILink link, int session)
  {
    IExternalReference reference = null;
    
    SessionInfo info = getSession( link, session);
    if ( info != null && info.isAttachClient) reference = (IExternalReference)info.element;
    
    doDetach( link, session);
    deallocSession( link, session);
    
    if ( reference != null) reference.setDirty( true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.IConnection.IListener#onClose(org.xmodel.net.IConnection)
   */
  @Override
  public void onClose( ILink link)
  {
    List<SessionInfo> list = getSessions( link);
    for( int i=0; i<list.size(); i++)
    {
      SessionInfo info = list.get( i);
      if ( info != null)
      {
        dispatch( info, new CloseRunnable( link, i));
      }
    }
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
      int byte0 = buffer.get();
      Type type = Type.values()[ byte0 & 0x3f];
      
      int session = readMessageSession( byte0, buffer);
      
      int length = readMessageLength( byte0, buffer);
      if ( length > buffer.remaining()) return false;
      
      switch( type)
      {
        case sessionOpenRequest:  handleSessionOpenRequest( link, buffer, length); return true;
        case sessionOpenResponse: handleSessionOpenResponse( link, session, buffer, length); return true;
        case sessionCloseRequest: handleSessionCloseRequest( link, session, buffer, length); return true;
        
        case error:           handleError( link, session, buffer, length); return true;
        case attachRequest:   handleAttachRequest( link, session, buffer, length); return true;
        case attachResponse:  handleAttachResponse( link, session, buffer, length); return true;
        case detachRequest:   handleDetachRequest( link, session, buffer, length); return true;
        case syncRequest:     handleSyncRequest( link, session, buffer, length); return true;
        case syncResponse:    handleSyncResponse( link, session, buffer, length); return true;
        case addChild:        handleAddChild( link, session, buffer, length); return true;
        case removeChild:     handleRemoveChild( link, session, buffer, length); return true;
        case changeAttribute: handleChangeAttribute( link, session, buffer, length); return true;
        case clearAttribute:  handleClearAttribute( link, session, buffer, length); return true;
        case changeDirty:     handleChangeDirty( link, session, buffer, length); return true;
        case queryRequest:    handleQueryRequest( link, session, buffer, length); return true;
        case queryResponse:   handleQueryResponse( link, session, buffer, length); return true;
        case executeRequest:  handleExecuteRequest( link, session, buffer, length); return true;
        case executeResponse: handleExecuteResponse( link, session, buffer, length); return true;
        case debugGo:         if ( debugEnabled) handleDebugGo( link, session, buffer, length); return true;
        case debugStop:       if ( debugEnabled) handleDebugStop( link, session, buffer, length); return true;
        case debugStepIn:     if ( debugEnabled) handleDebugStepIn( link, session, buffer, length); return true;
        case debugStepOver:   if ( debugEnabled) handleDebugStepOver( link, session, buffer, length); return true;
        case debugStepOut:    if ( debugEnabled) handleDebugStepOut( link, session, buffer, length); return true;
      }
    }
    catch( BufferUnderflowException e)
    {
    }
    
    return false;
  }
  
  /**
   * Send an session open request.
   * @param link The link.
   * @param version The version.
   * @return Returns the session number from the session response.
   */
  public final int sendSessionOpenRequest( ILink link, short version) throws IOException
  {
    //
    // Create large random client identifier for demultiplexing session open responses.
    //
    String client = Identifier.generate( random, 15);
    
    log.debugf( "sendSessionOpenRequest: %d, %s", version, client);
    initialize( buffer);
    buffer.putShort( version);
    writeString( client);
    finalize( buffer, Type.sessionOpenRequest, 0, 2);
    
    byte[] response = send( link, client, buffer, timeout);
    return ((int)response[ 0] << 24) + ((int)response[ 1] << 16) + ((int)response[ 2] << 8) + ((int)response[ 3]);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSessionOpenRequest( ILink link, ByteBuffer buffer, int length)
  {
    int version = buffer.getShort();
    String client = readString( buffer);
    log.debugf( "handleSessionOpenRequest: %d, %s", version, client);
    if ( version != Protocol.version)
    {
      link.close();
      return;
    }

    try
    {
      sendSessionOpenResponse( link, allocSession( link), client);
    }
    catch( IOException e)
    {
      log.exception( e);
    }
  }
  
  /**
   * Send the session open response.
   * @param link The link.
   * @param session The session id.
   * @param client The client identifier.
   */
  public final void sendSessionOpenResponse( ILink link, int session, String client) throws IOException
  {
    log.debugf( "sendSessionOpenResponse: %d, %s", session, client);
    initialize( buffer);
    writeString( client);
    buffer.putInt( session);
    finalize( buffer, Type.sessionOpenResponse, 0, 4);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSessionOpenResponse( ILink link, int session, ByteBuffer buffer, int length)
  {
    String client = readString( buffer);
    queueResponse( link, client, buffer, length);
  }
  
  /**
   * Send an session close request.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendSessionCloseRequest( ILink link, int session) throws IOException
  {
    log.debugf( "sendSessionCloseRequest(%d)", session);
    initialize( buffer);
    finalize( buffer, Type.sessionCloseRequest, session, 0);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSessionCloseRequest( ILink link, int session, ByteBuffer buffer, int length)
  {
    dispatch( getSession( link, session), new SessionCloseRunnable( link, session));
  }
  
  /**
   * Send an error message.
   * @param link The link.
   * @param session The session number.
   * @param message The error message.
   */
  public final void sendError( ILink link, int session, String message) throws IOException
  {
    log.debugf( "sendError: %s", message);
    initialize( buffer);
    byte[] bytes = message.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.error, session, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleError( ILink link, int session, ByteBuffer buffer, int length)
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
   * @param session The session number.
   * @param xpath The xpath.
   */
  public final void sendAttachRequest( ILink link, int session, String xpath) throws IOException
  {
    log.debugf( "sendAttachRequest(%d): %s", session, xpath);
    initialize( buffer);
    byte[] bytes = xpath.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.attachRequest, session, bytes.length);

    // send and wait for response
    byte[] response = send( link, session, buffer, timeout);
    ICompressor compressor = getSession( link, session).compressor;
    IModelObject element = compressor.decompress( new ByteArrayInputStream( response));
    log.debugf( "handleAttachResponse(%d): %s\n", session, element.getType());
    handleAttachResponse( link, session, element);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAttachRequest( ILink link, int session, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    log.debugf( "handleAttachRequest(%d): %s", session, new String( bytes));
    handleAttachRequest( link, session, new String( bytes));
  }
  
  /**
   * Handle an attach request.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath.
   */
  protected void handleAttachRequest( ILink link, int session, String xpath)
  {
    dispatch( getSession( link, session), new AttachRunnable( link, session, xpath));
  }

  /**
   * Send an attach response message.
   * @param link The link.
   * @param session The session number.
   * @param element The element.
   */
  public final void sendAttachResponse( ILink link, int session, IModelObject element) throws IOException
  {
    log.debugf( "sendAttachResponse(%d): %s", session, element.getType());
    initialize( buffer);
    
    ICompressor compressor = getSession( link, session).compressor;
    byte[] bytes = compressor.compress( element);
    ByteBuffer content = ByteBuffer.wrap( bytes);
    
    finalize( buffer, Type.attachResponse, session, bytes.length);
    link.send( buffer);
    link.send( content);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAttachResponse( ILink link, int session, ByteBuffer buffer, int length)
  {
    queueResponse( link, session, buffer, length);
  }
  
  /**
   * Handle an attach response.
   * @param link The link.
   * @param session The session number.
   * @param element The element.
   */
  protected void handleAttachResponse( ILink link, int session, IModelObject element)
  {
    IExternalReference attached = (IExternalReference)getSession( link, session).element;
    if ( attached != null)
    {
      ICachingPolicy cachingPolicy = attached.getCachingPolicy();
      cachingPolicy.update( attached, decode( link, session, element));
    }
  }
  
  /**
   * Send an detach request message.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendDetachRequest( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.detachRequest, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDetachRequest( ILink link, int session, ByteBuffer buffer, int length)
  {
    log.debugf( "handleDetachRequest(%d)", session);
    handleDetachRequest( link, session);
  }
  
  /**
   * Handle an attach request.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleDetachRequest( ILink link, int session)
  {
    dispatch( getSession( link, session), new DetachRunnable( link, session));
  }
  
  /**
   * Send an sync request message.
   * @param reference The local reference.
   */
  public final void sendSyncRequest( IExternalReference reference) throws IOException
  {
    KeyRecord key = keys.get( reference);
    
    log.debugf( "sendSyncRequest(%d): %s", key.session, reference.getType());
    
    initialize( buffer);
    byte[] bytes = key.key.getBytes();
    buffer.put( bytes, 0, bytes.length);
    finalize( buffer, Type.syncRequest, key.session, bytes.length);
    
    // send and wait for response
    send( key.link, key.session, buffer, timeout);
    handleSyncResponse( key.link, key.session);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSyncRequest( ILink link, int session, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    handleSyncRequest( link, session, new String( bytes));
  }
  
  /**
   * Handle a sync request.
   * @param link The link.
   * @param session The session number.
   * @param key The reference key.
   */
  protected void handleSyncRequest( ILink sender, int session, String key)
  {
    dispatch( getSession( sender, session), new SyncRunnable( sender, session, key));
  }
  
  /**
   * Send an sync response message.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendSyncResponse( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.syncResponse, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleSyncResponse( ILink link, int session, ByteBuffer buffer, int length)
  {
    queueResponse( link, session, buffer, length);
  }
  
  /**
   * Handle a sync response.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleSyncResponse( ILink link, int session)
  {
    // Nothing to do here
  }
  
  /**
   * Send an add child message.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath.
   * @param element The element.
   * @param index The insertion index.
   */
  public final void sendAddChild( ILink link, int session, String xpath, IModelObject element, int index) throws IOException
  {
    log.debugf( "sendAddChild(%d): %s, %s", session, xpath, element.getType());    
    
    initialize( buffer);
    int length = writeString( xpath);
    length += writeElement( getSession( link, session).compressor, element);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.addChild, session, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleAddChild( ILink link, int session, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    byte[] bytes = readBytes( buffer, false);
    int index = buffer.getInt();
    handleAddChild( link, session, xpath, bytes, index);
  }
  
  /**
   * Handle an add child message.
   * @param link The link.
   * @param session The session number.
   * @param xpath The path from the root to the parent.
   * @param child The child that was added as a compressed byte array.
   * @param index The insertion index.
   */
  protected void handleAddChild( ILink link, int session, String xpath, byte[] child, int index)
  {
    dispatch( getSession( link, session), new AddChildEvent( link, session, xpath, child, index));
  }
  
  /**
   * Process an add child event in the appropriate thread.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath of the parent.
   * @param bytes The child that was added.
   * @param index The index of insertion.
   */
  private void processAddChild( ILink link, int session, String xpath, byte[] bytes, int index)
  {
    try
    {
      updating = link;
      
      IModelObject attached = getSession( link, session).element;
      if ( attached == null) return;
      
      IExpression parentExpr = XPath.createExpression( xpath);
      if ( parentExpr != null)
      {
        IModelObject parent = parentExpr.queryFirst( attached);
        IModelObject child = getSession( link, session).compressor.decompress( bytes, 0);
        log.debugf( "processAddChild(%d): %s, %s", session, xpath, child.getType());            
        if ( parent != null) 
        {
          IModelObject childElement = decode( link, session, child);
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
   * @param session The session number.
   * @param xpath The xpath.
   * @param index The insertion index.
   */
  public final void sendRemoveChild( ILink link, int session, String xpath, int index) throws IOException
  {
    log.debugf( "sendRemoveChild(%d): %s, %d", session, xpath, index);    
    
    initialize( buffer);
    int length = writeString( xpath);
    buffer.putInt( index); length += 4;
    finalize( buffer, Type.removeChild, session, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleRemoveChild( ILink link, int session, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    int index = buffer.getInt();
    handleRemoveChild( link, session, xpath, index);
  }
  
  /**
   * Handle an remove child message.
   * @param link The link.
   * @param session The session number.
   * @param xpath The path from the root to the parent.
   * @param index The insertion index.
   */
  protected void handleRemoveChild( ILink link, int session, String xpath, int index)
  {
    dispatch( getSession( link, session), new RemoveChildEvent( link, session, xpath, index));
  }
  
  /**
   * Process an remove child event in the appropriate thread.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath of the parent.
   * @param index The index of insertion.
   */
  private void processRemoveChild( ILink link, int session, String xpath, int index)
  {
    try
    {
      updating = link;
      
      IModelObject attached = getSession( link, session).element;
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
   * @param session The session number.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  public final void sendChangeAttribute( ILink link, int session, String xpath, String attrName, Object value) throws IOException
  {
    byte[] bytes = serialize( value);
    
    initialize( buffer);
    int length = writeString( xpath);
    length += writeString( attrName);
    length += writeBytes( bytes, 0, bytes.length, true);
    finalize( buffer, Type.changeAttribute, session, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleChangeAttribute( ILink link, int session, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    byte[] attrValue = readBytes( buffer, true);
    handleChangeAttribute( link, session, xpath, attrName, deserialize( attrValue));
  }
  
  /**
   * Handle an attribute change message.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   * @param attrValue The attribute value.
   */
  protected void handleChangeAttribute( ILink link, int session, String xpath, String attrName, Object attrValue)
  {
    dispatch( getSession( link, session), new ChangeAttributeEvent( link, session, xpath, attrName, attrValue));
  }
  
  /**
   * Process an change attribute event in the appropriate thread.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath of the element.
   * @param attrName The name of the attribute.
   * @param attrValue The new value.
   */
  private void processChangeAttribute( ILink link, int session, String xpath, String attrName, Object attrValue)
  {
    try
    {
      updating = link;
      
      IModelObject attached = getSession( link, session).element;
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
   * @param session The session number.
   * @param xpath The xpath.
   * @param attrName The name of the attribute.
   */
  public final void sendClearAttribute( ILink link, int session, String xpath, String attrName) throws IOException
  {
    initialize( buffer);
    int length = writeString( xpath);
    length += writeString( attrName);
    finalize( buffer, Type.clearAttribute, session, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleClearAttribute( ILink link, int session, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    String attrName = readString( buffer);
    handleClearAttribute( link, session, xpath, attrName);
  }
  
  /**
   * Handle an attribute change message.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath from the root to the element.
   * @param attrName The name of the attribute.
   */
  protected void handleClearAttribute( ILink link, int session, String xpath, String attrName)
  {
    dispatch( getSession( link, session), new ClearAttributeEvent( link, session, xpath, attrName));
  }

  /**
   * Process a clear attribute event in the appropriate thread.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath of the element.
   * @param attrName The name of the attribute.
   */
  private void processClearAttribute( ILink link, int session, String xpath, String attrName)
  {
    try
    {
      updating = link;
      
      IModelObject attached = getSession( link, session).element;
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
   * @param session The session number.
   * @param xpath The xpath.
   * @param dirty The dirty state.
   */
  public final void sendChangeDirty( ILink link, int session, String xpath, boolean dirty) throws IOException
  {
    initialize( buffer);
    int length = writeString( xpath);
    buffer.put( dirty? (byte)1: 0); length++;
    finalize( buffer, Type.changeDirty, session, length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleChangeDirty( ILink link, int session, ByteBuffer buffer, int length)
  {
    String xpath = readString( buffer);
    boolean dirty = buffer.get() != 0;
    handleChangeDirty( link, session, xpath, dirty);
  }
  
  /**
   * Handle a change dirty message.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath from the root to the element.
   * @param dirty The dirty state.
   */
  protected void handleChangeDirty( ILink link, int session, String xpath, boolean dirty)
  {
    dispatch( getSession( link, session), new ChangeDirtyEvent( link, session, xpath, dirty));
  }

  /**
   * Process a change dirty event in the appropriate thread.
   * @param link The link.
   * @param session The session number.
   * @param xpath The xpath of the element.
   * @param dirty The new dirty state.
   */
  private void processChangeDirty( ILink link, int session, String xpath, boolean dirty)
  {
    try
    {
      updating = link;
      
      IModelObject attached = getSession( link, session).element;
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
   * @param session The session number.
   * @param context The local query context.
   * @param query The query string.
   * @param timeout The timeout in milliseconds.
   */
  public final Object sendQueryRequest( ILink link, int session, IContext context, String query, int timeout) throws IOException
  {
    initialize( buffer);

    IModelObject request = QueryProtocol.buildRequest( context, query);
    ICompressor compressor = getSession( link, session).compressor;
    byte[] bytes = compressor.compress( request);
    buffer.put( bytes);
    
    finalize( buffer, Type.queryRequest, session, bytes.length);
    
    byte[] content = send( link, session, buffer, timeout);
    ModelObject response = (ModelObject)compressor.decompress( content, 0);
    log.debugf( "handleQueryResponse(%d):%s\n", session, response.toXml());
    
    return QueryProtocol.readResponse( response);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleQueryRequest( ILink link, int session, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    
    ICompressor compressor = getSession( link, session).compressor;
    ModelObject request = (ModelObject)compressor.decompress( bytes, 0);
    
    handleQueryRequest( link, session, request);
  }
  
  /**
   * Handle a query requset.
   * @param link The link.
   * @param session The session number.
   * @param request The query request.
   */
  protected void handleQueryRequest( ILink link, int session, IModelObject request)
  {
    dispatch( getSession( link, session), new QueryRunnable( link, session, request));
  }
  
  /**
   * Send a query response message.
   * @param link The link.
   * @param session The session number.
   * @param result The query result.
   */
  public final void sendQueryResponse( ILink link, int session, Object object) throws IOException
  {
    initialize( buffer);

    ICompressor compressor = getSession( link, session).compressor;
    IModelObject response = QueryProtocol.buildResponse( object);
    byte[] bytes = compressor.compress( response);
    buffer.put( bytes);
    
    finalize( buffer, Type.queryResponse, session, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleQueryResponse( ILink link, int session, ByteBuffer buffer, int length)
  {
    queueResponse( link, session, buffer, length);
  }
  
  /**
   * Send an execute request message.
   * @param link The link.
   * @param session The session number.
   * @param context The local execution context.
   * @param variables The variables to be passed.
   * @param script The script to execute.
   * @param timeout The amount of time to wait for a response.
   * @return Returns null or the execution results.
   */
  public final Object[] sendExecuteRequest( ILink link, int session, StatefulContext context, String[] variables, IModelObject script, int timeout) throws IOException
  {
    initialize( buffer);
    
    ModelObject request = (ModelObject)ExecutionProtocol.buildRequest( context, variables, script);
    log.debugf( "sendExecuteRequest(%d):\n%s", session, request.toXml());
    ICompressor compressor = getSession( link, session).compressor;
    byte[] bytes = compressor.compress( request);
    buffer.put( bytes);
    
    finalize( buffer, Type.executeRequest, session, bytes.length);

    if ( timeout > 0)
    {
      byte[] content = send( link, session, buffer, timeout);
      ModelObject response = (ModelObject)compressor.decompress( content, 0);
      log.debugf( "handleExecuteResponse(%d):\n%s", session, response.toXml());
      return ExecutionProtocol.readResponse( response, context);
    }
    else
    {
      link.send( buffer);
      return null;
    }
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleExecuteRequest( ILink link, int session, ByteBuffer buffer, int length)
  {
    byte[] content = new byte[ length];
    buffer.get( content);
    
    ICompressor compressor = getSession( link, session).compressor;
    ModelObject request = (ModelObject)compressor.decompress( content, 0);
    log.debugf( "handleExecuteRequest(%d):\n%s", session, request.toXml());
    
    StatefulContext context = new StatefulContext( this.context);
    IModelObject script = ExecutionProtocol.readRequest( request, context);
    
    handleExecuteRequest( link, session, context, script);
  }
  
  /**
   * Handle an execute request.
   * @param link The link.
   * @param session The session number.
   * @param context The execution context.
   * @param script The script to execute.
   */
  protected void handleExecuteRequest( ILink link, int session, IContext context, IModelObject script)
  {
    dispatch( getSession( link, session), new ExecuteRunnable( link, session, context, script));
  }
  
  /**
   * Send an attach response message.
   * @param link The link.
   * @param session The session number.
   * @param element The element.
   */
  public final void sendExecuteResponse( ILink link, int session, IContext context, Object[] results) throws IOException
  {
    initialize( buffer);
    
    ModelObject response = (ModelObject)ExecutionProtocol.buildResponse( context, results);
    log.debugf( "sendExecuteResponse(%d):\n%s", session, response.toXml());
    
    ICompressor compressor = getSession( link, session).compressor;
    byte[] bytes = compressor.compress( response);
    buffer.put( bytes);
    
    finalize( buffer, Type.executeResponse, session, bytes.length);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleExecuteResponse( ILink link, int session, ByteBuffer buffer, int length)
  {
    queueResponse( link, session, buffer, length);
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendDebugGo( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugGo, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugGo( ILink link, int session, ByteBuffer buffer, int length)
  {
    handleDebugGo( link, session);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleDebugGo( ILink link, int session)
  {
    dispatch( getSession( link, session), new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepOut();
        XAction.setDebugger( null);
      }
    });
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendDebugStop( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStop, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStop( ILink link, int session, ByteBuffer buffer, int length)
  {
    handleDebugStop( link, session);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleDebugStop( ILink link, int session)
  {
    dispatch( getSession( link, session), new Runnable() {
      public void run()
      {
        XAction.setDebugger( new Debugger( Protocol.this, context));
      }
    });
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendDebugStepIn( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepIn, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepIn( ILink link, int session, ByteBuffer buffer, int length)
  {
    handleDebugStepIn( link, session);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleDebugStepIn( ILink link, int session)
  {
    dispatch( getSession( link, session), new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepIn();
      }
    });
  }
  
  /**
   * Send a debug step message.
   * @param link The link.
   * @param session The session number.
   */
  public final void sendDebugStepOver( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOver, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepOver( ILink link, int session, ByteBuffer buffer, int length)
  {
    handleDebugStepOver( link, session);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleDebugStepOver( ILink link, int session)
  {
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
   * @param session The session number.
   */
  public final void sendDebugStepOut( ILink link, int session) throws IOException
  {
    initialize( buffer);
    finalize( buffer, Type.debugStepOut, session, 0);
    link.send( buffer);
  }
  
  /**
   * Handle the specified message buffer.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer.
   * @param length The length of the message.
   */
  private final void handleDebugStepOut( ILink link, int session, ByteBuffer buffer, int length)
  {
    handleDebugStepOut( link, session);
  }
  
  /**
   * Handle a debug step.
   * @param link The link.
   * @param session The session number.
   */
  protected void handleDebugStepOut( ILink link, int session)
  {
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
   * @param session The session number.
   * @param buffer The buffer to send.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response buffer.
   */
  private byte[] send( ILink link, int session, ByteBuffer buffer, int timeout) throws IOException
  {
    try
    {
      link.send( buffer);
      byte[] response = getSession( link, session).responseQueue.poll( timeout, TimeUnit.MILLISECONDS);
      if ( response != null) return response;
    }
    catch( InterruptedException e)
    {
      return null;
    }
    
    throw new IOException( "Network request timeout.");
  }
  
  /**
   * Send and wait for a response.
   * @param link The link.
   * @param client The session initialization client identifier.
   * @param buffer The buffer to send.
   * @param timeout The timeout in milliseconds.
   * @return Returns null or the response buffer.
   */
  private byte[] send( ILink link, String client, ByteBuffer buffer, int timeout) throws IOException
  {
    try
    {
      sessionInitQueues.put( client, new SynchronousQueue<byte[]>());
      link.send( buffer);
      byte[] response = sessionInitQueues.get( client).poll( timeout, TimeUnit.MILLISECONDS);
      sessionInitQueues.remove( client);
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
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer containing the response.
   * @param length The length of the response.
   */
  private void queueResponse( ILink link, int session, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    try 
    {
      SessionInfo info = getSession( link, session);
      if ( info != null) info.responseQueue.put( bytes);
    } 
    catch( InterruptedException e) 
    {
    }
  }
  
  /**
   * Queue a synchronous response.
   * @param link The link.
   * @param session The session number.
   * @param buffer The buffer containing the response.
   * @param length The length of the response.
   */
  private void queueResponse( ILink link, String client, ByteBuffer buffer, int length)
  {
    byte[] bytes = new byte[ length];
    buffer.get( bytes);
    try 
    {
      BlockingQueue<byte[]> queue = sessionInitQueues.get( client);
      if ( queue != null) queue.put( bytes);
    } 
    catch( InterruptedException e) 
    {
    }
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
    buffer.position( 9);
  }
  
  /**
   * Read the session number from the buffer.
   * @param byte0 The first byte of the message.
   * @param buffer The buffer.
   * @return Returns the session number.
   */
  public static int readMessageSession( int byte0, ByteBuffer buffer)
  {
    int mask = byte0 & 0x80;
    if ( mask == 0) return buffer.get();
    return buffer.getInt();
  }
  
  /**
   * Read the message length from the buffer.
   * @param byte0 The first byte of the message.
   * @param buffer The buffer.
   * @return Returns the message length.
   */
  public static int readMessageLength( int byte0, ByteBuffer buffer)
  {
    int mask = byte0 & 0x40;
    if ( mask == 0) return buffer.get();
    return buffer.getInt();
  }
  
  /**
   * Finalize the message by writing the header and preparing the buffer to be read.
   * @param buffer The buffer.
   * @param type The message type.
   * @param session The session number.
   * @param length The message length.
   */
  public static void finalize( ByteBuffer buffer, Type type, int session, int length)
  {
    buffer.limit( buffer.position());
    
    int mask = 0;
    int position = 9;
    if ( length < 128)
    {
      position -= 1;
      buffer.put( position, (byte)length);
    }
    else
    {
      mask |= 0x40;
      position -= 4;
      buffer.putInt( position, length);
    }
    
    if ( session >= 0 && session < 128)
    {
      position -= 1;
      buffer.put( position, (byte)session);
    }
    else
    {
      mask |= 0x80;
      position -= 4;
      buffer.putInt( position, session);
    }
    
    position -= 1;
    buffer.put( position, (byte)(type.ordinal() | mask));
    
    buffer.position( position);
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
  static int writeLength( ByteBuffer buffer, int length, boolean small)
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
      ByteBuffer larger = ByteBuffer.allocate( (int)(required * 1.5));
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
   * @param session The session number.
   * @param root True if the element is the root of the attachment.
   * @param element The element to be copied.
   * @return Returns the copy.
   */
  protected IModelObject encode1( ILink link, int session, IModelObject element, boolean root)
  {
    IModelObject encoded = null;
    
    if ( element instanceof IExternalReference)
    {
      IExternalReference lRef = (IExternalReference)element;
      encoded = new ModelObject( lRef.getType());
      
      // index reference so it can be synced remotely
      if ( !root) encoded.setAttribute( "net:key", index( link, session, lRef));
      
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
        encoded.addChild( encode( link, session, child, false));
      }
    }
    
    return encoded;
  }
    
  /**
   * Encode the specified element.
   * @param link The link.
   * @param session The session number.
   * @param isRoot True if the element is the root of the attachment.
   * @param element The element to be copied.
   * @return Returns the copy.
   */
  protected IModelObject encode( ILink link, int session, IModelObject element, boolean isRoot)
  {
    element.getModel().setSyncLock( true);
    
    Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
    try
    {
      DepthFirstIterator iter = new DepthFirstIterator( element);
      while( iter.hasNext())
      {
        IModelObject lNode = iter.next();
        IModelObject rNode = map.get( lNode);
        IModelObject rParent = map.get( lNode.getParent());
        
        rNode = new ModelObject( lNode.getType());
        
        if ( lNode instanceof IExternalReference)
        {
          IExternalReference lRef = (IExternalReference)element;
          if ( !isRoot || rNode != element) Xlate.set( rNode, "net:key", index( link, session, lRef));
          
          // enumerate static attributes for client
          for( String attrName: lRef.getStaticAttributes())
          {
            IModelObject entry = new ModelObject( "net:static");
            entry.setValue( attrName);
            rNode.addChild( entry);
          }
          
          // copy only static attributes if reference is dirty
          if ( lNode.isDirty())
          {
            Xlate.set( rNode, "net:dirty", true);
            
            // copy static attributes
            for( String attrName: lRef.getStaticAttributes())
            {
              Object attrValue = element.getAttribute( attrName);
              if ( attrValue != null) rNode.setAttribute( attrName, attrValue);
            }
          }
        }
        else
        {
          ModelAlgorithms.copyAttributes( lNode, rNode);
        }
        
        map.put( lNode, rNode);
        if ( rParent != null) rParent.addChild( rNode);
      }
    }
    finally
    {
      element.getModel().setSyncLock( false);
    }
    
    return map.get( element);
  }
  
  /**
   * Interpret the content of the specified server encoded subtree.
   * @param link The link.
   * @param session The session number.
   * @param root The root of the encoded subtree.
   * @return Returns the decoded element.
   */
  protected IModelObject decode( ILink link, int session, IModelObject root)
  {
    root.getModel().setSyncLock( true);
    
    Map<IModelObject, IModelObject> map = new HashMap<IModelObject, IModelObject>();
    try
    {
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
          keyRecord.session = session;
          keys.put( reference, keyRecord);
          
          rNode = reference;
        }
        else
        {
          rNode = new ModelObject( lNode.getType());
          ModelAlgorithms.copyAttributes( lNode, rNode);
        }
        
        map.put( lNode, rNode);
        if ( rParent != null) rParent.addChild( rNode);
      }
    }
    finally
    {
      root.getModel().setSyncLock( false);
    }
    
    return map.get( root);
  }
  
  /**
   * Index the specified node.
   * @param link The link.
   * @param session The session number.
   * @param node The node.
   * @return Returns the key.
   */
  private String index( ILink link, int session, IModelObject node)
  {
    String key = Identifier.generate( random, 13);
    System.out.printf( "index: %d: %s -> %s\n", session, node.getType(), key);
    getSession( link, session).index.put( key, node);
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
    public SyncRunnable( ILink sender, int session, String key)
    {
      this.sender = sender;
      this.session = session;
      this.key = key;
    }
    
    public void run()
    {
      SessionInfo info = getSession( sender, session);
      IModelObject reference = info.index.get( key);
      if ( reference != null)
      {
        try
        {
          reference.getChildren();
          sendSyncResponse( sender, session);
        }
        catch( IOException e)
        {
          log.exception( e);
        }
      }
    }
    
    private ILink sender;
    private int session;
    private String key;
  }
  
  private final class AddChildEvent implements Runnable
  {
    public AddChildEvent( ILink link, int session, String xpath, byte[] child, int index)
    {
      this.link = link;
      this.session = session;
      this.xpath = xpath;
      this.child = child;
      this.index = index;
    }
    
    public void run()
    {
      processAddChild( link, session, xpath, child, index);
    }
    
    private ILink link;
    private int session;
    private String xpath;
    private byte[] child;
    private int index;
  }
  
  private final class RemoveChildEvent implements Runnable
  {
    public RemoveChildEvent( ILink link, int session, String xpath, int index)
    {
      this.link = link;
      this.session = session;
      this.xpath = xpath;
      this.index = index;
    }
    
    public void run()
    {
      processRemoveChild( link, session, xpath, index);
    }
    
    private ILink link;
    private int session;
    private String xpath;
    private int index;
  }
  
  private final class ChangeAttributeEvent implements Runnable
  {
    public ChangeAttributeEvent( ILink link, int session, String xpath, String attrName, Object attrValue)
    {
      this.link = link;
      this.session = session;
      this.xpath = xpath;
      this.attrName = attrName;
      this.attrValue = attrValue;
    }
    
    public void run()
    {
      processChangeAttribute( link, session, xpath, attrName, attrValue);
    }
    
    private ILink link;
    private int session;
    private String xpath;
    private String attrName;
    private Object attrValue;
  }
  
  private final class ClearAttributeEvent implements Runnable
  {
    public ClearAttributeEvent( ILink link, int session, String xpath, String attrName)
    {
      this.link = link;
      this.session = session;
      this.xpath = xpath;
      this.attrName = attrName;
    }
    
    public void run()
    {
      processClearAttribute( link, session, xpath, attrName);
    }
    
    private ILink link;
    private int session;
    private String xpath;
    private String attrName;
  }
  
  private final class ChangeDirtyEvent implements Runnable
  {
    public ChangeDirtyEvent( ILink link, int session, String xpath, boolean dirty)
    {
      this.link = link;
      this.session = session;
      this.xpath = xpath;
      this.dirty = dirty;
    }
    
    public void run()
    {
      processChangeDirty( link, session, xpath, dirty);
    }
    
    private ILink link;
    private int session;
    private String xpath;
    private boolean dirty;
  }
  
  private final class AttachRunnable implements Runnable
  {
    public AttachRunnable( ILink sender, int session, String xpath)
    {
      this.sender = sender;
      this.session = session;
      this.xpath = xpath;
    }
    
    public void run()
    {
      try
      {
        doAttach( sender, session, xpath);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }

    private ILink sender;
    private int session;
    private String xpath;
  }
  
  private final class DetachRunnable implements Runnable
  {
    public DetachRunnable( ILink sender, int session)
    {
      this.sender = sender;
      this.session = session;
    }
    
    public void run()
    {
      doDetach( sender, session);
    }

    private ILink sender;
    private int session;
  }
  
  private final class QueryRunnable implements Runnable
  {
    public QueryRunnable( ILink sender, int session, IModelObject request)
    {
      this.sender = sender;
      this.session = session;
      this.request = request;
    }
    
    public void run()
    {
      try
      {
        doQuery( sender, session, request);
      } 
      catch( IOException e)
      {
        log.exception( e);
      }
    }
    
    private ILink sender;
    private int session;
    private IModelObject request;
  }
  
  private final class ExecuteRunnable implements Runnable
  {
    public ExecuteRunnable( ILink sender, int session, IContext context, IModelObject script)
    {
      this.sender = sender;
      this.session = session;
      this.context = context;
      this.script = script;
    }
    
    public void run()
    {
      doExecute( sender, session, context, script);
    }
    
    private ILink sender;
    private int session;
    private IContext context;
    private IModelObject script;
  }
  
  private final class SessionCloseRunnable implements Runnable
  {
    public SessionCloseRunnable( ILink sender, int session)
    {
      this.sender = sender;
      this.session = session;
    }
    
    public void run()
    {
      doSessionClose( sender, session);
    }
    
    private ILink sender;
    private int session;
  }
  
  private final class CloseRunnable implements Runnable
  {
    public CloseRunnable( ILink sender, int session)
    {
      this.sender = sender;
      this.session = session;
    }
    
    public void run()
    {
      doClose( sender, session);
    }
    
    private ILink sender;
    private int session;
  }
  
  protected class Listener extends NonSyncingListener
  {
    public Listener( ILink sender, int session, String xpath, IModelObject root)
    {
      this.sender = sender;
      this.session = session;
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
        IModelObject clone = encode( sender, session, child, false);
        sendAddChild( sender, session, createPath( root, parent), clone, index);
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
        sendRemoveChild( sender, session, createPath( root, parent), index);
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
        sendChangeAttribute( sender, session, xpath, attrName, newValue);
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
        sendClearAttribute( sender, session, xpath, attrName);
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
        sendChangeDirty( sender, session, createPath( root, object), dirty);
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
    private int session;
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
  
  /**
   * Dispatch the specified Runnable using either the session dispatcher or the context dispatcher.
   * @param info The session state object.
   * @param runnable The runnable.
   */
  protected void dispatch( SessionInfo info, Runnable runnable)
  {
    if ( info.dispatcher != null)
    {
      info.dispatcher.execute( runnable);
    }
    else if ( dispatcher != null) 
    {
      dispatcher.execute( runnable);
    }
  }
  
  /**
   * Allocate a session.
   * @param link The link.
   * @return Returns the session number.
   */
  private final int allocSession( ILink link)
  {
    synchronized( sessions)
    {
      List<SessionInfo> list = sessions.get( link);
      if ( list == null)
      {
        list = new ArrayList<SessionInfo>();
        sessions.put( link, list);
      }
      
      for( int i=0; i<list.size(); i++)
      {
        if ( list.get( i) == null)
        {
          list.set( i, new SessionInfo());
          return i;
        }
      }
      
      list.add( new SessionInfo());
      return list.size() - 1;
    }
  }
  
  /**
   * Deallocate a session.
   * @param link The link.
   * @param session The session number.
   */
  private final void deallocSession( ILink link, int session)
  {
    synchronized( sessions)
    {
      List<SessionInfo> list = sessions.get( link);
      if ( list != null) list.set( session, null);
    }
  }
  
  /**
   * Returns the SessionInfo object for the specified session on the specified link.
   * @param link The link.
   * @param session The session.
   * @return Returns the SessionInfo object for the specified session on the specified link.
   */
  protected final SessionInfo getSession( ILink link, int session)
  {
    synchronized( sessions)
    {
      List<SessionInfo> list = sessions.get( link);
      if ( list != null) return list.get( session);
      return null;
    }
  }
  
  /**
   * Returns the SessionInfo objects on the specified link.
   * @param link The link.
   * @return Returns the SessionInfo objects on the specified link.
   */
  protected final List<SessionInfo> getSessions( ILink link)
  {
    synchronized( sessions)
    {
      List<SessionInfo> list = sessions.get( link);
      if ( list == null) return Collections.emptyList();
      
      List<SessionInfo> result = new ArrayList<SessionInfo>();
      for( SessionInfo info: list)
      {
        if ( info != null) result.add( info);
      }
      
      return result;
    }
  }
  
  private class KeyRecord
  {
    String key;
    ILink link;
    int session;
  }
  
  protected class SessionInfo
  {
    public SessionInfo()
    {
      responseQueue = new SynchronousQueue<byte[]>();
      index = new HashMap<String, IModelObject>();
      compressor = new TabularCompressor( PostCompression.zip);
    }
    
    public BlockingQueue<byte[]> responseQueue;
    public IDispatcher dispatcher;
    public String xpath;
    public IModelObject element;
    public boolean isAttachClient;
    public Map<String, IModelObject> index;
    public TabularCompressor compressor;
    public Listener listener;
  }
  
  private static Log log = Log.getLog( "org.xmodel.net");

  private Map<ILink, List<SessionInfo>> sessions;
  private Map<String, BlockingQueue<byte[]>> sessionInitQueues;
  private Map<IModelObject, KeyRecord> keys;
  private IContext context;
  private ByteBuffer buffer;
  private int timeout;
  private Random random;
  private IDispatcher dispatcher;
  private List<String> packageNames;
  private ILink updating;
  private boolean debugEnabled;
}
