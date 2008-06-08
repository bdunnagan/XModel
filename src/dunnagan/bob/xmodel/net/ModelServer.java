/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.io.*;
import java.net.InetSocketAddress;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.compress.ICompressor;
import dunnagan.bob.xmodel.compress.TabularCompressor;
import dunnagan.bob.xmodel.compress.TabularCompressor.PostCompression;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.net.message.*;
import dunnagan.bob.xmodel.net.message.Update;
import dunnagan.bob.xmodel.net.robust.*;
import dunnagan.bob.xmodel.net.robust.ISession.StreamFactory;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.expression.NullContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression.ResultType;

/**
 * A TCP-based server which provides remote access to xmodel collections.
 */
public class ModelServer extends Server
{
  public final static String defaultHost = "127.0.0.1";
  public final static int defaultPort = 17310;
  
  /**
   * Create a server with the specified model with the specified write timeout. The write timeout
   * indicates how long the server will block the datamodel before closing the session. When the
   * server is used for debugging purposes, this timeout should be short since the application 
   * shouldn't be blocked for debugging because the client is busy.
   * @param model The model.
   * @param writeTimeout The amount of time to wait before failing a write operation.
   */
  public ModelServer( IModel model, int writeTimeout)
  {
    this.model = model;
    
    context = NullContext.getInstance();
    identities = new IdentityTable();
    inCompressor = new TabularCompressor( PostCompression.zip);
    outCompressor = new TabularCompressor( PostCompression.zip);
    
    addHandler( sessionDispatcher);
    
    setStreamFactory( new StreamFactory() {
      public InputStream getInputStream( InputStream stream)
      {
        return new DataInputStream( stream);
      }
      public OutputStream getOutputStream( OutputStream stream)
      {
        return new DataOutputStream( stream);
      }
    });
    
    setSessionFactory( new SessionFactory() {
      public IServerSession createSession( Server server, InetSocketAddress address, long sid)
      {
        return new RobustServerSession( new ServerSession( server, address, sid), 10, 3000);
      }
    });
  }
  
  /**
   * Start server on default port (defaultPort)
   */
  public void start() throws IOException
  {
    start( defaultPort);
  }
  
  /**
   * Set the context of the Server.
   * @param context The context.
   */
  public void setContext( IContext context)
  {
    this.context = context;
    sendMessage( new ContextChange());
  }
  
  /**
   * Don't send updates when the specified attribute is updated.
   * @param attrName The name of the attribute to ignore.
   */
  public void ignoreAttribute( String attrName)
  {
    for( IServerSession session: getSessions())
    {
      ServerModelListener listener = ServerModelListener.getInstance( ModelServer.this, session);
      listener.ignoreAttribute( attrName);
    }
  }
  
  /**
   * Send updates when the specified attribute is updated.
   * @param attrName The name of the attribute to ignore.
   */
  public void regardAttribute( String attrName)
  {
    for( IServerSession session: getSessions())
    {
      ServerModelListener listener = ServerModelListener.getInstance( ModelServer.this, session);
      listener.regardAttribute( attrName);
    }
  }
  
  /**
   * Clone the specified element and add meta annotations.
   * @param session The server session.
   * @param element The element to be cloned.
   * @return Returns the cloned element.
   */
  private IModelObject createClone( ISession session, IModelObject element)
  {
    IExternalReference reference = null;
    
    // create clone without syncing dirty references
    ModelObject clone = new ModelObject( element.getType());
    
    // add meta data
    String remoteID = identities.get( element);
    if ( remoteID == null) remoteID = identities.insert( element);
    clone.setAttribute( "remote:id", remoteID);
    clone.setAttribute( "remote:class", element.getClass().getSimpleName());
    if ( element instanceof IExternalReference)
    {
      reference = (IExternalReference)element.getReferent();
      clone.setAttribute( "remote:policy", reference.getCachingPolicy().getClass().getSimpleName());
      clone.setAttribute( "remote:dirty", reference.isDirty());
      Xlate.set( clone, "remote:stub", true);
    }
    else 
    {
      // set remote:stub flag
      if ( element.getNumberOfChildren() > 0)
        Xlate.set( clone, "remote:stub", true);
    }
    
    // copy only static attributes of dirty references
    if ( element.isDirty())
    {
      // include static attributes
      String[] staticAttributes = reference.getStaticAttributes();
      for( int i=0; i<staticAttributes.length; i++)
        clone.setAttribute( staticAttributes[ i], element.getAttribute( staticAttributes[ i]));
    }
    else
    {
      ModelAlgorithms.copyAttributes( element, clone);
    }

    // add listener to element represented by any clone which is not partial because 
    // this means that the client reference will not be dirty.
    if ( !Xlate.get( clone, "remote:stub", false))
      ServerModelListener.getInstance( this, session).install( element);
    
    return clone;
  }
  
  /**
   * Clone the specified element and its children if the element is not dirty.
   * @param session The server session.
   * @param element The element to be cloned.
   * @return Returns clones of the specified elements and its children.
   */
  private IModelObject createCloneWithChildren( ISession session, IModelObject element)
  {
    IModelObject clone = createClone( session, element);

    // setting remote:stub to false so element needs listener
    Xlate.set( clone, "remote:stub", false);
    ServerModelListener.getInstance( this, session).install( element);
    
    for( IModelObject child: element.getChildren())
      clone.addChild( createClone( session, child));
    
    return clone;
  }
  
  /**
   * (XModel Thread)
   * Send an update to the client for the specified element.
   * @param session The session which requested updates.
   * @param parent The parent where the child was added.
   * @param child The child that was added.
   * @param index The index of insertion.
   */
  public void sendInsert( ISession session, IModelObject parent, IModelObject child, int index)
  {
    String remoteID = identities.get( parent);
    Insert insert = new Insert( remoteID, index);
    IModelObject clone = createCloneWithChildren( session, child);
    insert.setResult( clone);
    sendMessage( session, insert);
  }
  
  /**
   * (XModel Thread)
   * Send an update to the client for the specified element.
   * @param session The session which requested updates.
   * @param element The element which was updated.
   */
  public void sendUpdate( ISession session, IModelObject element)
  {
    Update update = new Update();
    update.setResult( createClone( session, element));
    sendMessage( session, update);
  }
  
  /**
   * (XModel Thread)
   * Send an update to the client for the specified element.
   * @param session The session which requested updates.
   * @param element The element which was updated.
   */
  public void sendDelete( ISession session, IModelObject element)
  {
    Delete delete = new Delete( identities.remove( element));
    sendMessage( session, delete);
  }
  
  /**
   * Broadcast a message to all sessions.
   * @param message The message.
   */
  public void sendMessage( Message message)
  {
    try
    {
      if ( debug) System.err.printf( "Sending:\n%s\n", message.toXml());
      
      byte[] bytes = outCompressor.compress( message.content);
      for( ISession session: getSessions())
      {
        DataOutputStream stream = (DataOutputStream)session.getOutputStream();
        stream.writeInt( bytes.length);
        stream.write( bytes);
        stream.flush();
      }

      if ( debug) System.err.printf( "Sent %2.1fK.\n", (bytes.length / 1000f));
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
  }

  /**
   * Send a message to a specified session.
   * @param session The session.
   * @param message The message.
   */
  public void sendMessage( ISession session, Message message)
  {
    try
    {
      if ( debug) System.err.printf( "Sending:\n%s\n", message.toXml());
      
      byte[] bytes = outCompressor.compress( message.content);
      DataOutputStream stream = (DataOutputStream)session.getOutputStream();
      stream.writeInt( bytes.length);
      stream.write( bytes);
      stream.flush();

      if ( debug) System.err.printf( "Sent %2.1fK.\n", (bytes.length / 1000f));
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      session.close();
    }
  }
  
  /**
   * (XModel Thread)
   * Handle the specified request.
   * @param session The session that sent the request.
   * @param buffer The request buffer.
   */
  public void handleRequest( ISession session, byte[] buffer)
  {
    try
    {
      if ( debug) System.err.printf( "Received %2.1fK:\n", buffer.length / 1000f);
      IModelObject content = inCompressor.decompress( buffer, 0);
      if ( debug) System.err.printf( "%s\n", (new XmlIO()).write( content));

      // examine request type
      String type = content.getType();
      if ( type.equals( "queryRequest"))
      {
        handleQueryRequest( session, content);
      }
      else if ( type.equals( "resultTypeRequest"))
      {
        ResultTypeResponse response = new ResultTypeResponse( content.getID());
        String query = Xlate.childGet( content, "query", "");
        IExpression expr = XPath.createExpression( query);
        ResultType resultType = (expr != null)? expr.getType( context): ResultType.UNDEFINED;
        response.content.getCreateChild( "type").setValue( resultType.name()); 
        sendMessage( session, response);
      }
      else if ( type.equals( "syncRequest"))
      {
        String remoteID = Xlate.get( content, "");
        SyncResponse response = new SyncResponse( content.getID());
        IModelObject element = identities.get( remoteID);
        if ( element != null) 
        {
          ServerModelListener listener = ServerModelListener.getInstance( this, session);
          listener.ignoreElement( element);
          response.setResult( createCloneWithChildren( session, element));
          listener.regardElement( element);
        }
        sendMessage( session, response);
      }
      else if ( type.equals( "dirtyRequest"))
      {
        for( IModelObject element: ElementRequest.getElements( content, identities))
          if ( element instanceof IExternalReference)
            ((IExternalReference)element).clearCache();
        
        //sendMessage( session, new Response( content.getID(), "dirtyResponse"));
      }
      else if ( type.equals( "closeRequest"))
      {
        session.close();
      }
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      session.close();
    }
  }
  
  /**
   * Handle a query request.
   * @param session The session that sent the request.
   * @param content The request.
   */
  private void handleQueryRequest( ISession session, IModelObject content)
  {
    QueryResponse response = new QueryResponse( content.getID());
    
    String query = Xlate.childGet( content, "query", "");
    IExpression expression = XPath.createExpression( query);
    if ( expression != null)
    {
      switch( expression.getType( context))
      {
        case STRING:
          response.setResult( expression.evaluateString( context));
          break;
          
        case NUMBER:
          response.setResult( expression.evaluateNumber( context));
          break;
          
        case BOOLEAN:
          response.setResult( expression.evaluateBoolean( context));
          break;
          
        case NODES:
          for( IModelObject element: expression.evaluateNodes( context))
          {
            ServerModelListener listener = ServerModelListener.getInstance( this, session);
            listener.ignoreElement( element);
            response.setResult( createCloneWithChildren( session, element));
            listener.regardElement( element);
          }
          break;
      }
    }
    else
    {
      response.setError( "Syntax error in expression: "+query);
    }

    sendMessage( session, response);
  }

  private ISession.Listener sessionListener = new ISession.Listener() {
    public void notifyOpen( ISession session)
    {
    }
    public void notifyClose( ISession session)
    {
      model.dispatch( new CleanupRunnable( session));
    }
    public void notifyConnect( ISession session)
    {
    }
    public void notifyDisconnect( ISession session)
    {
    }
  };
  
  private ServerHandler sessionDispatcher = new ServerHandler( sessionListener) {
    protected void run( ISession session)
    {
      DataInputStream input = (DataInputStream)session.getInputStream();
      while( true)
      {
        try
        {
          int length = input.readInt();
          if ( length < 0) return;
          
          byte[] buffer = new byte[ length];
          input.readFully( buffer, 0, length);
        
          RequestRunnable runnable = new RequestRunnable( session, buffer);
          model.dispatch( runnable);
        }
        catch( EOFException e)
        {
          if ( debug) e.printStackTrace( System.err);
          return;
        }
        catch( Exception e)
        {
          // should never get here
          e.printStackTrace( System.err);
          return;
        }
      }
    }
  };
  
  /**
   * (XModel Thread)
   * A class posted to the user-supplied dispatcher to execute a query.
   */
  private class RequestRunnable implements Runnable
  {
    public RequestRunnable( ISession session, byte[] buffer)
    {
      this.session = session;
      this.buffer = buffer;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      handleRequest( session, buffer);
    }
    
    private ISession session;
    private byte[] buffer;
  }
  
  /**
   * (XModel Thread)
   * A class which removes all of the update listeners from elements in the identity table.
   */
  private class CleanupRunnable implements Runnable
  {
    public CleanupRunnable( ISession session)
    {
      this.session = session;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      ServerModelListener listener = ServerModelListener.getInstance( ModelServer.this, session);
      listener.uninstall();
      //for( IModelObject element: identities.getObjects()) listener.uninstall( element);
    }
    
    private ISession session;
  }
 
  private final static boolean debug = false;
  
  private IContext context;
  private IModel model;
  private IdentityTable identities;
  private ICompressor inCompressor;
  private ICompressor outCompressor;
}
