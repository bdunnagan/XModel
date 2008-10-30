package org.xmodel.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Reference;
import org.xmodel.Xlate;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;
import org.xmodel.external.IExternalReference;
import org.xmodel.net.robust.IServerSession;
import org.xmodel.net.robust.ISession;
import org.xmodel.net.robust.RobustServerSession;
import org.xmodel.net.robust.Server;
import org.xmodel.net.robust.ServerHandler;
import org.xmodel.net.robust.ServerSession;
import org.xmodel.util.Fifo;
import org.xmodel.util.Radix;
import org.xmodel.xaction.XAction;
import org.xmodel.xaction.debug.GlobalDebugger;
import org.xmodel.xaction.debug.IDebugger.Frame;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


/**
 * A data-model server which provides remote query and update subscription.
 */
public class ModelServer extends Server
{
  public final static String defaultHost = "127.0.0.1";
  public final static int defaultPort = 17310;
  
  public ModelServer( IModel model)
  {
    this.model = model;
    
    states = new HashMap<Long, SessionState>();
    netIDs = new Hashtable<String, IModelObject>();
    debugger = new GlobalDebugger( this);
    XAction.debugger = debugger;
    
    addHandler( serverHandler);

    setSessionFactory( new SessionFactory() {
      public IServerSession createSession( Server server, InetSocketAddress address, long sid)
      {
        return new RobustServerSession( new ServerSession( server, address, sid), 100, 100);
      }
    });
  }

  /**
   * Set the query context of the server.
   * @param context The context.
   */
  public void setQueryContext( IContext context)
  {
    this.context = context;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.Server#start(int)
   */
  @Override
  public void start( int port) throws IOException
  {
    super.start( port);

    // add shutdown hook
    Runtime.getRuntime().addShutdownHook( shutdownHook);
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.robust.Server#stop()
   */
  @Override
  public void stop()
  {
    // remove shutdown hook
    Runtime.getRuntime().removeShutdownHook( shutdownHook);
    
    // tell clients that sessions are exitting and reset init state
    for( ISession session: getSessions())
      close( session, "Server stopped.");
    
    // stop
    super.stop();
  }
  
  /**
   * Close the specified session.
   * @param session The session.
   * @param message The reason for closing.
   */
  protected void close( ISession session, String message)
  {
    sendClose( session, message);

    // delayed session closing
    CloseRunnable runnable = new CloseRunnable();
    runnable.session = session;
    model.dispatch( runnable);
  }

  /**
   * Send the ready message to client indicating successfull initialization.
   * @param session The session.
   * @param messageID The message id.
   */
  protected void sendReady( ISession session, String messageID)
  {
    ModelObject message = new ModelObject( "ready");
    message.setID( messageID);
    send( session, message);
  }
  
  /**
   * Send the query result to the client.
   * @param session The session.
   * @param messageID The message id.
   * @param result The result set.
   */
  protected void sendQueryResult( ISession session, String messageID, List<IModelObject> result)
  {
    ModelObject message = new ModelObject( "query");
    message.setID( messageID);
    message.setAttribute( "type", "NODES");
    for( IModelObject root: result) message.addChild( root);
    send( session, message);
  }
  
  /**
   * Send the query result to the client.
   * @param session The session.
   * @param messageID The message id.
   * @param result The result set.
   */
  protected void sendQueryResult( ISession session, String messageID, String result)
  {
    ModelObject message = new ModelObject( "query");
    message.setID( messageID);
    message.setAttribute( "type", "STRING");
    message.setAttribute( "result", result);
    send( session, message);
  }
  
  /**
   * Send the query result to the client.
   * @param session The session.
   * @param messageID The message id.
   * @param result The result set.
   */
  protected void sendQueryResult( ISession session, String messageID, double result)
  {
    ModelObject message = new ModelObject( "query");
    message.setID( messageID);
    message.setAttribute( "type", "NUMBER");
    message.setAttribute( "result", result);
    send( session, message);
  }
  
  /**
   * Send the query result to the client.
   * @param session The session.
   * @param messageID The message id.
   * @param result The result set.
   */
  protected void sendQueryResult( ISession session, String messageID, boolean result)
  {
    ModelObject message = new ModelObject( "query");
    message.setID( messageID);
    message.setAttribute( "type", "BOOLEAN");
    message.setAttribute( "result", result);
    send( session, message);
  }
  
  /**
   * Send the bind result to the client.
   * @param session The session.
   * @param messageID The message id.
   * @param result The result set.
   */
  protected void sendBindResult( ISession session, String messageID, String queryID, List<IModelObject> result)
  {
    ModelObject message = new ModelObject( "bind");
    message.setID( messageID);
    message.setAttribute( "net:qid", queryID);
    for( IModelObject root: result) message.addChild( root);
    send( session, message);
  }
  
  /**
   * Send the sync result to the client.
   * @param session The session.
   * @param messageID The message id.
   * @param result The result set.
   */
  protected void sendSyncResult( ISession session, String messageID, IModelObject result)
  {
    ModelObject message = new ModelObject( "sync");
    message.setID( messageID);
    if ( result != null) message.addChild( result);
    send( session, message);
  }
  
  /**
   * Send the unbind response to the client.
   * @param session The session.
   * @param messageID The message id.
   */
  protected void sendUnbindResult( ISession session, String messageID)
  {
    ModelObject message = new ModelObject( "unbind");
    message.setID( messageID);
    send( session, message);
  }
  
  /**
   * Send a hearbeat response.
   * @param session The session.
   */
  protected void sendHeartbeat( ISession session, String messageID)
  {
    ModelObject message = new ModelObject( "beat");
    message.setID( messageID);
    send( session, message);
  }
  
  /**
   * Send an insert notification to the client.
   * @param session The session.
   * @param parent The parent.
   * @param child The child. 
   * @param index The insert index.
   */
  protected void sendInsert( ISession session, IModelObject parent, IModelObject child, int index)
  {
    SessionState state = states.get( session.getSessionNumber());
    if ( state == null || state.syncing == parent) return;
    
    ModelObject message = new ModelObject( "insert");
    Xlate.childSet( message, "parent", getNetID( parent));
    Xlate.childSet( message, "index", index);
    message.getCreateChild( "child").addChild( cloneResult( session, child, maxInsertCount, false));
    send( session, message);
  }

  /**
   * Send a delete notification to the client.
   * @param session The session.
   * @param parent The parent of the object before it was deleted.
   * @param object The object that was deleted.
   */
  protected void sendDelete( ISession session, IModelObject object)
  {
    ModelObject message = new ModelObject( "delete");
    Xlate.set( message, "net:id", getNetID( object));
    send( session, message);
  }
  
  /**
   * Send an update notification to the client.
   * @param session The session.
   * @param object The object that was updated.
   * @param attrName The name of the attribute.
   * @param newValue The new value.
   */
  protected void sendChange( ISession session, IModelObject object, String attrName, Object newValue)
  {
    SessionState state = states.get( session.getSessionNumber());
    if ( state == null || state.syncing == object) return;
    
    ModelObject message = new ModelObject( "change");
    Xlate.set( message, "net:id", getNetID( object));
    Xlate.childSet( message, "attribute", attrName);
    Xlate.childSet( message, "value", newValue.toString());
    send( session, message);
  }
  
  /**
   * Send an update notification to the client.
   * @param session The session.
   * @param object The object that was updated.
   * @param attrName The name of the attribute.
   * @param newValue The new value.
   */
  protected void sendClear( ISession session, IModelObject object, String attrName)
  {
    SessionState state = states.get( session.getSessionNumber());
    if ( state == null || state.syncing == object) return;
    
    ModelObject message = new ModelObject( "clear");
    Xlate.set( message, "net:id", getNetID( object));
    Xlate.childSet( message, "attribute", attrName);
    send( session, message);
  }
  
  /**
   * Send an update notification to the client.
   * @param session The session.
   * @param object The object that was updated.
   * @param dirty The updated dirty state.
   */
  protected void sendDirty( ISession session, IModelObject object, boolean dirty)
  {
    ModelObject message = new ModelObject( "dirty");
    Xlate.set( message, "net:id", getNetID( object));
    Xlate.set( message, "dirty", dirty);
    send( session, message);
  }
  
  /**
   * Send an error notification to the client.
   * @param session The session.
   * @param text The error message.
   */
  protected void sendError( ISession session, String text)
  {
    ModelObject message = new ModelObject( "error");
    message.setValue( text);
    send( session, message);
  }
  
  /**
   * Send a close message to the client.
   * @param session The session.
   * @param note Additional information.
   */
  protected void sendClose( ISession session, String note)
  {
    ModelObject message = new ModelObject( "close");
    message.setValue( note);
    send( session, message);
  }
  
  /**
   * Send a debug status message to the client.
   * @param threadID The thread id.
   * @param threadName The thread name.
   * @param state The debug state.
   * @param action The debug action (or null).
   * @param stack The current xaction stack (or null).
   */
  @SuppressWarnings("unchecked")
  public void sendDebugMessage( String threadID, String threadName, String state, String action, Stack<Frame> stack)
  {
    ModelObject message = new ModelObject( "debug");
    message.setAttribute( "state", state);
    message.setAttribute( "action", action);
    
    ModelObject threadNode = new ModelObject( "thread", threadID);
    threadNode.setAttribute( "name", threadName);
    message.addChild( threadNode);
    
    if ( stack != null)
    {
      IModelObject stackNode = message.getCreateChild( "stack");
      for( int i = stack.size()-1; i >= 0; i--)
      {
        Frame frame = stack.get( i);
        
        ModelObject frameNode = new ModelObject( "frame", Integer.toString( frame.action.hashCode()));
        stackNode.addChild( frameNode);
        
        // frame context variables
        for( String variable: frame.context.getScope().getAll())
        {
          ModelObject variableNode = new ModelObject( "variable");
          variableNode.setID( variable);
          
          Object value = frame.context.getScope().get( variable);
          if ( value == null)
          {
            variableNode.setAttribute( "type", "null");
          }
          else if ( value instanceof List)
          {
            variableNode.setAttribute( "type", "nodes");
            XmlIO xmlIO = new XmlIO();
            xmlIO.setMaxLines( Xlate.get( message, "maxLines", 1000));
            for( IModelObject node: (List<IModelObject>)value)
            {
              String xml = xmlIO.write( node);
              ModelObject result = new ModelObject( "result");
              result.setValue( xml);
              variableNode.addChild( result);
            }
          }
          else if ( value instanceof Number)
          {
            variableNode.setAttribute( "type", "number");
            ModelObject result = new ModelObject( "result");
            result.setValue( value.toString());
            variableNode.addChild( result);
          }
          else if ( value instanceof Boolean)
          {
            variableNode.setAttribute( "type", "boolean");
            ModelObject result = new ModelObject( "result");
            result.setValue( value.toString());
            variableNode.addChild( result);
          }
          else if ( value instanceof IExpression)
          {
            variableNode.setAttribute( "type", "expression");
            ModelObject result = new ModelObject( "result");
            result.setValue( value.toString());
            variableNode.addChild( result);
          }
          else
          {
            variableNode.setAttribute( "type", "string");
            ModelObject result = new ModelObject( "result");
            result.setValue( value.toString());
            variableNode.addChild( result);
          }
          
          frameNode.addChild( variableNode);
        }
        
        // frame action name (either element name or class)
        IModelObject actionRoot = frame.action.getDocument().getRoot();
        if ( actionRoot != null)
        {
          frameNode.setAttribute( "name", actionRoot.getType());
        }
        else
        {
          frameNode.setAttribute( "name", frame.action.getClass().getSimpleName());
        }
        
        // frame action file path and action xpath relative to file root
        IModelObject element = frame.action.getDocument().getRoot();
        if ( element != null)
        {
          IModelObject fileElement = sourcePathExpr.queryFirst( element);
          if ( fileElement != null) 
          {
            frameNode.setAttribute( "path", Xlate.get( fileElement, "path", ""));
            IPath actionPath = ModelAlgorithms.createRelativePath( fileElement, element);
            frameNode.setAttribute( "locus", actionPath.toString());
          }
        }
      }
    }

    for( ISession session: getSessions())
      send( session, message);
  }
  
  /**
   * Send a message to the server.
   * @param session The session.
   * @param message The message.
   */
  protected void send( ISession session, IModelObject message)
  {
    if ( !message.isType( "beat"))
    {
      System.out.println( "____________________________________");
      System.out.println( "SERVER SENT: \n"+((ModelObject)message).toXml());
    }
        
    SessionState state = states.get( session.getSessionNumber());
    if ( state == null) return;
    
    byte[] compressed = state.compressor.compress( message);
    session.write( compressed);
  }
  
  /**
   * Handle a message from the client.
   * @param session The session.
   * @param message The message.
   */
  protected void handle( ISession session, IModelObject message)
  {
    SessionState state = states.get( session.getSessionNumber());
    if ( state == null) return;
    
    if ( !message.isType( "init"))
    {
      if ( !state.init) sendClose( session, "Session not initialized."); 
    }
    
    try
    {
      if ( message.isType( "query"))
      {
        handleQuery( session, message);
      }
      else if ( message.isType( "bind"))
      {
        handleBind( session, message);
      }
      else if ( message.isType( "sync"))
      {
        handleSync( session, message);
      }
      else if ( message.isType( "unbind"))
      {
        handleUnbind( session, message);
      }
      else if ( message.isType( "beat"))
      {
        handleHeartbeat( session, message);
      }
      else if ( message.isType( "dirty"))
      {
        handleMarkDirty( session, message);
      }
      else if ( message.isType( "init"))
      {
        handleInit( session, message);
      }
      else if ( message.isType( "insert"))
      {
        handleInsert( session, message);
      }
      else if ( message.isType( "delete"))
      {
        handleDelete( session, message);
      }
      else if ( message.isType( "change"))
      {
        handleChange( session, message);
      }
      else if ( message.isType( "clear"))
      {
        handleClear( session, message);
      }
      else if ( message.isType( "close"))
      {
        handleClose( session, message);
      }
    }
    catch( Exception e)
    {
      close( session, e.getMessage());
    }
  }
  
  /**
   * Handle an init message.
   * @param session The session.
   * @param message The message.
   */
  protected void handleInit( ISession session, IModelObject message)
  {
    SessionState state = states.get( session.getSessionNumber());
    if ( !state.init)
    {
      state.init = true;
      sendReady( session, message.getID());
    }
    else
    {
      sendClose( session, "Session not initialized.");
    }
  }
  
  /**
   * Handle a close message.
   * @param session The session.
   * @param message The message.
   */
  protected void handleClose( ISession session, IModelObject message)
  {
    String text = Xlate.get( message, "");
    if ( text.length() > 0) System.out.println( "Session closed with message: "+text);
    session.close();
  }
  
  /**
   * Handle a query message.
   * @param session The session.
   * @param message The message.
   */
  protected void handleQuery( ISession session, IModelObject message)
  {
    // perform query
    String spec = Xlate.get( message, "query", "");
    IExpression query = XPath.createExpression( spec);
    switch( query.getType( context))
    {
      case NODES:
      {
        List<IModelObject> result = query.evaluateNodes( context);
        int limit = Xlate.get( message, "limit", maxQueryCount);
        result = cloneResult( session, result, limit, true);
        sendQueryResult( session, message.getID(), result);
      }
      break;
      
      case STRING:  sendQueryResult( session, message.getID(), query.evaluateString( context)); break;
      case NUMBER:  sendQueryResult( session, message.getID(), query.evaluateNumber( context)); break;
      case BOOLEAN: sendQueryResult( session, message.getID(), query.evaluateBoolean( context)); break;
    }
  }

  /**
   * Handle a bind message.
   * @param session The session.
   * @param message The message.
   */
  protected void handleBind( ISession session, IModelObject message)
  {
    // perform query
    String spec = Xlate.get( message, "query", "");
    IExpression query = XPath.createExpression( spec);
    List<IModelObject> result = query.evaluateNodes( context);
        
    // limit result
    int limit = Xlate.get( message, "limit", maxQueryCount);
    result = cloneResult( session, result, limit, true);
    
    // send result
    String messageID = message.getID();
    sendBindResult( session, messageID, Radix.convert( listener.hashCode(), 36), result);
  }

  /**
   * Handle a sync message.
   * @param session The session.
   * @param message The message.
   */
  private void handleSync( ISession session, IModelObject message)
  {
    SessionState state = states.get( session.getSessionNumber());
    
    String netID = Xlate.get( message, "net:id", "");
    IModelObject result = null;
    
    try
    {
      // lookup element by net:id
      IModelObject element = netIDs.get( netID);
      if ( element != null)
      {
        state.syncing = element;
        
        // limit result
        int limit = Xlate.get( message, "limit", 0);
        result = cloneResult( session, element, limit, true);
      }
  
      // send result
      String messageID = message.getID();
      sendSyncResult( session, messageID, result);
    }
    finally
    {
      state.syncing = null;
    }
  }
  
  /**
   * Handle an unbind message.
   * @param session The session.
   * @param message The message.
   */
  private void handleUnbind( ISession session, IModelObject message)
  {
    String netID = Xlate.get( message, "net:id", "");
    IModelObject object = netIDs.get( netID);
    if ( object != null)
    {
      SessionState state = states.get( session.getSessionNumber());
      state.listener.uninstall( object);
      state.listenees.remove( object);
    }
    
    // send result
    sendUnbindResult( session, message.getID());
  }
  
  /**
   * Handle a heartbeat message.
   * @param session The session.
   * @param message The heartbeat message.
   */
  private void handleHeartbeat( ISession session, IModelObject message)
  {
    sendHeartbeat( session, message.getID());
  }
  
  /**
   * Handle a mark dirty request.
   * @param session The session.
   * @param message The message.
   */
  private void handleMarkDirty( ISession session, IModelObject message)
  {
    IModelObject object = netIDs.get( Xlate.get( message, "net:id", (String)null));
    if ( object != null && object instanceof IExternalReference)
    {
      SessionState state = states.get( session.getSessionNumber());
      state.listener.uninstall( object);
      ((IExternalReference)object).clearCache();
      object.addModelListener( state.listener);
      sendDirty( session, object, object.isDirty());
    }
  }
  
  /**
   * Handle an insert request.
   * @param session The session.
   * @param message The message.
   */
  private void handleInsert( ISession session, IModelObject message)
  {
    IModelObject object = netIDs.get( Xlate.get( message, "net:id", (String)null));
    if ( object != null)
    {
      IModelObject child = message.getFirstChild( "child").getChild( 0);
      int index = Xlate.childGet( message, "index", -1);
      object.addChild( child, index);
    }    
  }
  
  /**
   * Handle an delete request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDelete( ISession session, IModelObject message)
  {
    IModelObject object = netIDs.get( Xlate.get( message, "net:id", (String)null));
    if ( object != null) object.removeFromParent();
  }
  
  /**
   * Handle an change attribute request.
   * @param session The session.
   * @param message The message.
   */
  private void handleChange( ISession session, IModelObject message)
  {
    IModelObject object = netIDs.get( Xlate.get( message, "net:id", (String)null));
    if ( object != null)
    {
      String attrName = Xlate.childGet( message, "attribute", "");
      String attrValue = Xlate.childGet( message, "value", "");
      object.setAttribute( attrName, attrValue);
    }
  }
  
  /**
   * Handle an clear attribute request.
   * @param session The session.
   * @param message The message.
   */
  private void handleClear( ISession session, IModelObject message)
  {
    IModelObject object = netIDs.get( Xlate.get( message, "net:id", (String)null));
    if ( object != null)
    {
      String attrName = Xlate.childGet( message, "attribute", "");
      object.removeAttribute( attrName);
    }
  }

  /**
   * Handle a debug request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebug( ISession session, IModelObject message)
  {
    String action = Xlate.get( message, "action", (String)null);
    if ( action.equals( "resume"))
    {
      handleDebugResume( session, message);
    }
    else if ( action.equals( "suspend"))
    {
      handleDebugSuspend( session, message);
    }
    else if ( action.equals( "stepInto"))
    {
      handleDebugStepInto( session, message);
    }
    else if ( action.equals( "stepOver"))
    {
      handleDebugStepOver( session, message);
    }
    else if ( action.equals( "stepReturn"))
    {
      handleDebugStepReturn( session, message);
    }
    else if ( action.equals( "createBreakpoint"))
    {
      handleDebugCreateBreakpoint( session, message);
    }
    else if ( action.equals( "removeBreakpoint"))
    {
      handleDebugRemoveBreakpoint( session, message);
    }
    else if ( action.equals( "getThreads"))
    {
      handleDebugGetThreads( session, message);
    }
  }
  
  /**
   * Handle a debug get threads request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugGetThreads( ISession session, IModelObject message)
  {
    debugger.setTargetThread( null);
    debugger.report();
  }
  
  /**
   * Handle a debug resume request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugResume( ISession session, IModelObject message)
  {
    debugger.setTargetThread( Xlate.get( message, "thread", (String)null));
    debugger.resume();
  }
  
  /**
   * Handle a debug suspend request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugSuspend( ISession session, IModelObject message)
  {
    debugger.setTargetThread( Xlate.get( message, "thread", (String)null));
    debugger.suspend();
  }
    
  /**
   * Handle a debug step-into request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugStepInto( ISession session, IModelObject message)
  {
    debugger.setTargetThread( Xlate.get( message, "thread", (String)null));
    debugger.stepInto();
  }
  
  /**
   * Handle a debug step-over request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugStepOver( ISession session, IModelObject message)
  {
    debugger.setTargetThread( Xlate.get( message, "thread", (String)null));
    debugger.stepOver();
  }
  
  /**
   * Handle a debug step-return request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugStepReturn( ISession session, IModelObject message)
  {
    debugger.setTargetThread( Xlate.get( message, "thread", (String)null));
    debugger.stepReturn();
  }
  
  /**
   * Handle a debug create-breakpoint request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugCreateBreakpoint( ISession session, IModelObject message)
  {
    String path = Xlate.get( message, "path", (String)null);
    String expression = Xlate.get( message, "expression", (String)null);
    debugger.createBreakpoint( path, expression);
  }
  
  /**
   * Handle a debug remove breakpoint request.
   * @param session The session.
   * @param message The message.
   */
  private void handleDebugRemoveBreakpoint( ISession session, IModelObject message)
  {
    String path = Xlate.get( message, "path", (String)null);
    String expression = Xlate.get( message, "expression", (String)null);
    debugger.removeBreakpoint( path, expression);
  }
    
  /**
   * Iterate the elements in the specified query roots breadth-first and begin making stubs
   * out of elements after the limit of elements is reached.
   * @param session The session.
   * @param result The result.
   * @param bind True if listener should be added.
   * @return Returns the cloned and limited result set.
   */
  private List<IModelObject> cloneResult( ISession session, List<IModelObject> result, int limit, boolean bind)
  {
    // adjust limit to accomodate all root elements
    if ( limit < result.size()) limit = result.size();

    // clone each root
    List<IModelObject> clones = new ArrayList<IModelObject>();
    for( IModelObject root: result) clones.add( cloneResult( session, root, limit, bind));
    
    return clones;
  }
  
  /**
   * Clone the specified tree making stubs out of elements where appropriate. References are not synced here.
   * @param session The session.
   * @param result The result.
   * @param bind True if listener should be added.
   * @return Returns the cloned and limited tree.
   */
  private IModelObject cloneResult( ISession session, IModelObject root, int limit, boolean bind)
  {
    SessionState state = states.get( session.getSessionNumber());
    
    // set of objects to be bound with a listener
    List<IModelObject> bound = new ArrayList<IModelObject>();

    try
    {
      // perform breadth-first search
      IModelObject clone = root.cloneObject();
      root.getChildren(); // sync
      
      // clone tree stubbing as necessary
      int count = 0;
      Fifo<IModelObject> fifo = new Fifo<IModelObject>();
      fifo.push( root);
      fifo.push( clone);
      while( !fifo.empty())
      {
        IModelObject source = fifo.pop();
        IModelObject target = fifo.pop();
        
        String netID = getNetID( source);
        target.setAttribute( "net:id", netID);
  
        // elements that will get server listeners
        if ( bind) { bound.add( source); target.setAttribute( "net:bound");}
        
  //      // stub all external references except root, which the client wants to sync
  //      if ( source != root && source instanceof IExternalReference)
        if ( source instanceof IExternalReference)
        {
          target.setAttribute( "net:stub");
          if ( !source.isDirty())
          {
            target.setAttribute( "net:dirty", false);
            
            // clone children and push on stack
            for( IModelObject child: source.getChildren())
            {
              IModelObject childClone = cloneObject( session, child);
              target.addChild( childClone);
              fifo.push( child);
              fifo.push( childClone);
            }
          }
        }
        
        // stub other objects with children after exceeding limit
        else if ( count++ >= limit && source.getNumberOfChildren() > 0)
        {
          target.setAttribute( "net:stub");
        }
  
        // clone children of all others and push on stack
        else
        {
          for( IModelObject child: source.getChildren())
          {
            IModelObject childClone = cloneObject( session, child);
            target.addChild( childClone);
            fifo.push( child);
            fifo.push( childClone);
          }
        }
      }
      
      // add listeners
      for( IModelObject object: bound)
      {
        object.addModelListener( state.listener);
        state.listenees.add( object);
      }

      return clone;
    }
    catch( Exception e)
    {
      IModelObject object = new ModelObject( "null");
      object.setValue( e.toString());
      return object;
    }
  }
  
  /**
   * Clone the specified object and send exceptions to client.
   * @param session The session (in case an error needs to be reported).
   * @param object The object to be cloned.
   * @return Returns the clone.
   */
  private IModelObject cloneObject( ISession session, IModelObject object)
  {
    if ( object.isDirty())
    {
      // clone only static attributes of dirty children
      IExternalReference reference = (IExternalReference)object;
      IModelObject clone = new ModelObject( reference.getType());
      for( String attrName: reference.getStaticAttributes())
        clone.setAttribute( attrName, reference.getAttribute( attrName));
      return clone;
    }
    else
    {
      return object.cloneObject();
    }
  }
  
  /**
   * Returns the net:id for the specified object.
   * @param object The object.
   * @return Returns the net:id for the specified object.
   */
  private String getNetID( IModelObject object)
  {
    int hashCode = (object == object.getReferent())? object.hashCode(): ((Reference)object).nativeHashCode();
    String netID = Radix.convert( hashCode, 36);
    netIDs.put( netID, object);
    return netID;
  }
  
  /**
   * The session listener.
   */
  private final ISession.Listener listener = new ISession.Listener() {
    public void notifyOpen( ISession session)
    {
    }
    public void notifyClose( ISession session)
    {
      SessionState state = states.remove( session.getSessionNumber());
      if ( state != null)
      {
        for( IModelObject object: state.listenees) state.listener.uninstall( object);
        state.exit = true;
      }
    }
    public void notifyConnect( ISession session)
    {
      System.out.println( "Session connected: "+session.getShortSessionID());
    }
    public void notifyDisconnect( ISession session)
    {
      System.out.println( "Session disconnected: "+session.getShortSessionID());
    }
  };

  /**
   * The server handler.
   */
  private final ServerHandler serverHandler = new ServerHandler( listener) {
    protected void run( IServerSession session)
    {
      SessionState state = new SessionState();
      state.init = false;
      state.exit = false;
      state.compressor = new TabularCompressor( PostCompression.none);
      state.decompressor = new TabularCompressor( PostCompression.none);
      state.listener = new ServerModelListener( session);
      state.listenees = new HashSet<IModelObject>();
      states.put( session.getSessionNumber(), state);
      
      try
      {
        InputStream stream = session.getInputStream();
        while( !state.exit)
        {
          IModelObject message = state.decompressor.decompress( stream);
          if ( !message.isType( "beat"))
          {
            System.out.println( "____________________________________");
            System.out.println( "SERVER RECEIVED: \n"+((ModelObject)message).toXml());
          }
          
          // handle xaction debug messages
          if ( message.isType( "debug"))
          {
            handleDebug( session, message);
          }
          else
          {
            // gui is running, dispatch to gui thread
            MessageRunnable runnable = new MessageRunnable();
            runnable.session = session;
            runnable.message = message;
            model.dispatch( runnable);
          }
        }

        System.out.println( "Session closed: "+session.getSessionNumber());
      }
      catch( CompressorException e)
      {
        session.close();
      }
    }
  };
  
  private final Thread shutdownHook = new Thread( "Shutdown") {
    public void run()
    {
      System.out.println( "Executing xmodel server shutdown hook...");
      for( ISession session: getSessions())
      {
        SessionState state = states.get( session.getSessionNumber());
        if ( state != null && state.init && !state.exit)
        {
          System.out.println( "Sending close message to session "+session.getSessionNumber());
          sendClose( session, "Server shutdown.");
        }
      }
    }
  };
  
  /**
   * Client message runnable.
   */
  private class MessageRunnable implements Runnable
  {
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      handle( session, message.cloneTree());
    }
    
    public ISession session;
    public IModelObject message;
  }
  
  /**
   * Delayed session closing runnable (allows time for close message to arrive at client).
   */
  private class CloseRunnable implements Runnable
  {
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      session.close();
    }
    
    public ISession session;
  }

  /**
   * Some state information about each session.
   */
  private class SessionState
  {
    boolean init;
    boolean exit;
    IModelObject syncing;
    ICompressor decompressor;
    ICompressor compressor;
    ServerModelListener listener;
    Set<IModelObject> listenees;
  }
  
  private final static IExpression sourcePathExpr = XPath.createExpression(
    "ancestor-or-self::*[ @path]");
  
  private final static int maxQueryCount = 3000;
  private final static int maxInsertCount = 3000;
  
  private IModel model;
  private IContext context;
  private Map<Long, SessionState> states;
  private Map<String, IModelObject> netIDs;
  private GlobalDebugger debugger;
}
