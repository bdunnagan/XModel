/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import dunnagan.bob.xmodel.IModel;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.compress.CompressorException;
import dunnagan.bob.xmodel.compress.ICompressor;
import dunnagan.bob.xmodel.compress.TabularCompressor;
import dunnagan.bob.xmodel.compress.TabularCompressor.PostCompression;
import dunnagan.bob.xmodel.external.ICachingPolicy;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.external.NonSyncingIterator;
import dunnagan.bob.xmodel.net.message.*;
import dunnagan.bob.xmodel.net.robust.Client;
import dunnagan.bob.xmodel.net.robust.ISession;
import dunnagan.bob.xmodel.net.robust.RobustSession;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.expression.IExpression.ResultType;
import dunnagan.bob.xmodel.xpath.function.BooleanFunction;
import dunnagan.bob.xmodel.xpath.function.NumberFunction;
import dunnagan.bob.xmodel.xpath.function.StringFunction;

/**
 * A TCP-based client which provides remote access to xmodel collections.
 */
public class ModelClient extends RobustSession
{
  /**
   * Create a client to connect to the specified host.  Clients are not intended to be 
   * constructed directly.  Instead call the <code>getClient</code> method on the
   * <code>IModel</code> interface.
   * @param host A host running an xmodel server.
   * @param port The port to which the server is bound.
   * @param model The model with which to associate this client.
   */
  public ModelClient( String host, int port, IModel model)
  {
    super( new Client( host, port), 100);
    
    this.model = model; 
    this.identities = new IdentityTable();
    this.inCompressor = new TabularCompressor( PostCompression.zip);
    this.outCompressor = new TabularCompressor( PostCompression.zip);
    this.synchronizer = new Semaphore( 0);
    
    addListener( sessionHandler);
    
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
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.net.robust.RobustSession#close()
   */
  @Override
  public void close()
  {
    if ( isOpen()) try { sendMessage( new CloseRequest());} catch( MessageException e) { e.printStackTrace( System.err);}
    super.close();
  }

  /**
   * Register the specified element by the given remote id.
   * @param remoteID The remote id.
   * @param element The element.
   */
  public void register( String remoteID, IModelObject element)
  {
    String storeID = identities.get( element);
    if ( storeID == null) identities.insert( remoteID, element);
    element.removeAttribute( "remote:id");
  }
  
  /**
   * Register the remote id of the specified element.
   * @param element The element.
   */
  public void register( IModelObject element)
  {
    NonSyncingIterator iterator = new NonSyncingIterator( element);
    while( iterator.hasNext())
    {
      IModelObject descendant = (IModelObject)iterator.next();
      String remoteID = Xlate.get( descendant, "remote:id", (String)null);
      if ( remoteID == null) throw new IllegalArgumentException( "Missing remote:id attribute: "+element);
      register( remoteID, descendant);
    }
  }
  
  /**
   * Unregister the specified element with its remote id.
   * @param element The element.
   */
  public void unregister( IModelObject element)
  {
    NonSyncingIterator iterator = new NonSyncingIterator( element);
    while( iterator.hasNext())
    {
      IModelObject descendant = (IModelObject)iterator.next();
      identities.remove( descendant);
    }
  }
  
  /**
   * Add a listener for notifications of server events.
   * @param listener The listener.
   */
  public void addUpdateListener( IUpdateListener listener)
  {
    if ( listeners == null) listeners = new ArrayList<IUpdateListener>( 1);
    listeners.add( listener);
  }
  
  /**
   * Remove a listener for notifications of server events.
   * @param listener The listener.
   */
  public void removeUpdateListener( IUpdateListener listener)
  {
    if ( listeners == null) return;
    listeners.remove( listener);
    if ( listeners.size() == 0) listeners = null;
  }
  
  /**
   * (XModel Thread)
   * Perform the specified remote query and return the string result.
   * @param query The query specification.
   * @return Returns the string result of the query.
   */
  public String evaluateString( String query) throws MessageException
  {
    QueryRequest request = new QueryRequest( query);
    sendMessage( request);
    synchronizer.acquireUninterruptibly();

    IModelObject queryResult = getQueryResult();
    String error = Xlate.childGet( queryResult, "error", (String)null);
    if ( error != null) throw new MessageException( error);
    
    IModelObject type = queryResult.getChild( 0);
    if ( type.isType( "string")) return Xlate.get( type, "");
    if ( type.isType( "boolean")) return StringFunction.stringValue( Xlate.get( type, false));
    if ( type.isType( "number")) return StringFunction.stringValue( Xlate.get( type, 0));
    if ( type.isType( "nodes")) return StringFunction.stringValue( type.getChildren());
    
    throw new MessageException( "Undefined result type: "+type.getType());
  }
  
  /**
   * (XModel Thread)
   * Perform the specified remote query and return the numeric result.
   * @param query The query specification.
   * @return Returns the numeric result of the query.
   */
  public double evaluateNumber( String query) throws MessageException
  {
    QueryRequest request = new QueryRequest( query);
    sendMessage( request);
    synchronizer.acquireUninterruptibly();

    IModelObject queryResult = getQueryResult();
    String error = Xlate.childGet( queryResult, "error", (String)null);
    if ( error != null) throw new MessageException( error);
    
    IModelObject type = queryResult.getChild( 0);
    if ( type.isType( "number")) return Xlate.get( type, 0);
    if ( type.isType( "string")) return NumberFunction.numericValue( Xlate.get( type, "false"));
    if ( type.isType( "boolean")) return NumberFunction.numericValue( Xlate.get( type, false));
    if ( type.isType( "nodes")) return NumberFunction.numericValue( type.getChildren());
    
    throw new MessageException( "Undefined result type: "+type.getType());
  }
  
  /**
   * (XModel Thread)
   * Perform the specified remote query and return the boolean result.
   * @param query The query specification.
   * @return Returns the boolean result of the query.
   */
  public boolean evaluateBoolean( String query) throws MessageException
  {
    QueryRequest request = new QueryRequest( query);
    sendMessage( request);
    synchronizer.acquireUninterruptibly();

    IModelObject queryResult = getQueryResult();
    String error = Xlate.childGet( queryResult, "error", (String)null);
    if ( error != null) throw new MessageException( error);
    
    IModelObject type = queryResult.getChild( 0);
    if ( type.isType( "boolean")) return Xlate.get( type, false);
    if ( type.isType( "string")) return BooleanFunction.booleanValue( Xlate.get( type, "false"));
    if ( type.isType( "number")) return BooleanFunction.booleanValue( Xlate.get( type, 0));
    if ( type.isType( "nodes")) return BooleanFunction.booleanValue( type.getChildren());
    
    throw new MessageException( "Undefined result type: "+type.getType());
  }
  
  /**
   * (XModel Thread)
   * Perform the specified remote query and return the node-set. If the query does not 
   * evaluate to a node-set then an empty node-set is returned since there are no rules
   * for converting other types to a node-set.
   * @param query The query specification.
   * @return Returns the query node-set.
   */
  public List<IModelObject> evaluateNodes( String query) throws MessageException
  {
    QueryRequest request = new QueryRequest( query);
    sendMessage( request);
    synchronizer.acquireUninterruptibly();

    IModelObject queryResult = getQueryResult();
    String error = Xlate.childGet( queryResult, "error", (String)null);
    if ( error != null) throw new MessageException( error);
    
    IModelObject nodes = queryResult.getFirstChild( "nodes");
    if ( nodes == null) return Collections.emptyList();

    return nodes.getChildren();
  }
  
  /**
   * (XModel Thread)
   * Returns a clone of the query result for the calling thread.
   * @return Returns a clone of the query result for the calling thread.
   */
  private IModelObject getQueryResult()
  {
    return queryResult.cloneTree();
  }
  
  /**
   * Returns the result type of the specified query on the remote host.
   * @param query The query.
   * @return Returns the result type of the query.
   */
  public ResultType sendResultTypeRequest( String query) throws MessageException
  {
    ResultTypeRequest request = new ResultTypeRequest( query);
    sendMessage( request);
    synchronizer.acquireUninterruptibly();
    return queryResultType;
  }
  
  /**
   * (XModel Thread)
   * Query using the remote id of the specified reference.
   * @param reference The reference being synchronized.
   * @return Returns the result of the sync.
   */
  public IModelObject sendSyncRequest( IExternalReference reference) throws MessageException
  {
    String remoteID = identities.get( reference);
    SyncRequest request = new SyncRequest( remoteID);
    sendMessage( request);
    synchronizer.acquireUninterruptibly();
    
    String error = Xlate.childGet( queryResult, "error", (String)null);
    if ( error != null) throw new MessageException( error);
    
    IModelObject nodes = queryResult.getFirstChild( "nodes");
    if ( nodes == null) return null;
    
    if ( nodes.getNumberOfChildren() != 1) 
      throw new MessageException( "Unable to synchronize reference: "+reference);

    return nodes.getChild( 0);
  }
  
  /**
   * (XModel Thread)
   * Send a mark dirty request for the specified element.
   * @param elements The elements.
   */
  public void sendMarkDirty( List<IModelObject> elements) throws MessageException
  {
    DirtyRequest request = new DirtyRequest();
    for( IModelObject element: elements)
    {
      String remoteID = identities.get( element);
      if ( remoteID != null) request.addRemoteID( remoteID);
    }
    sendMessage( request);
  }
  
  /**
   * (XModel Thread)
   * Send a message to the server.
   * @param message The message.
   */
  protected void sendMessage( Request request) throws MessageException
  {
    try
    {
      if ( debug) System.err.printf( "Sending: \n%s\n", request.toXml());
     
      DataOutputStream stream = (DataOutputStream)getOutputStream();
      if ( stream != null) 
      {
        byte[] bytes = outCompressor.compress( request.content);
        stream.writeInt( bytes.length);
        stream.write( bytes);
        stream.flush();
        
        if ( debug) System.err.printf( "Sent %2.1fK.\n", (bytes.length / 1000f));
      }
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      throw new MessageException( "Unable to send message: "+request, e);
    }
  }

  /**
   * (Connection Thread)
   * Set the query result and release the semaphore.
   * @param content The query response.
   */
  private void handleQueryResponse( IModelObject content)
  {
    queryResult = content;
    synchronizer.release();
  }
  
  /**
   * (Connection Thread)
   * Set the query result type and release the semaphore.
   * @param content The result type response.
   */
  private void handleResultTypeResponse( IModelObject content)
  {
    queryResultType = ResultType.valueOf( Xlate.childGet( content, "type", ""));
    synchronizer.release();
  }
  
  /**
   * (Connection Thread)
   * Set the query result and release the semaphore.
   * @param content The sync response.
   */
  private void handleSyncResponse( IModelObject content)
  {
    queryResult = content;
    synchronizer.release();
  }
  
  /**
   * (XModel Thread)
   * Handle a message from the server.
   * @param content The message content.
   */
  private void handleMessage( IModelObject content)
  {
    try
    {
      String type = content.getType();
      if ( type.equals( "updateResponse"))
      {
      }
      else if ( type.equals( "insert"))
      {
        IModelObject result = content.getFirstChild( "nodes");
        if ( result != null)
        {
          String parentID = Xlate.childGet( content, "id", ""); 
          int index = Xlate.childGet( content, "index", -1);
          
          IModelObject element = result.getChild( 0);
          element.removeFromParent();
          
          IExternalReference parent = (IExternalReference)identities.get( parentID);
          ICachingPolicy cachingPolicy = parent.getCachingPolicy();
          cachingPolicy.insert( parent, element, index, true);

          notifyInsert( parent, element);
        }
      }
      else if ( type.equals( "update"))
      {
        IModelObject result = content.getFirstChild( "nodes");
        if ( result != null)
        {
          IModelObject element = result.getChild( 0); 
          element.removeFromParent();
          
          String remoteID = Xlate.get( element, "remote:id", "");
          IExternalReference updatee = (IExternalReference)identities.get( remoteID);
          IModelObject original = updatee.cloneObject();
          if ( updatee != null) ModelAlgorithms.copyAttributes( element, updatee);

          notifyUpdate( original, element);
        }
      }
      else if ( type.equals( "delete"))
      {
        String id = Xlate.childGet( content, "id", ""); 
        IExternalReference element = (IExternalReference)identities.get( id);
        if ( element != null) 
        {
          unregister( element);
          element.getCachingPolicy().remove( (IExternalReference)element.getParent(), element);  
          
          notifyDelete( element.getParent(), element);
        }
      }
      else if ( type.equals( "context"))
      {
        // root should be sent from server
        //root.clearCache();
      }
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
      close();
    }
  }
  
  /**
   * Notify listeners of an insert event.
   * @param parent The parent.
   * @param child The child.
   */
  private void notifyInsert( IModelObject parent, IModelObject child)
  {
    try
    {
      if ( listeners == null) return;
      IUpdateListener[] listeners = this.listeners.toArray( new IUpdateListener[ 0]);
      for( IUpdateListener listener: listeners) listener.notifyInsert( parent, child);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
  }
  
  /**
   * Notify listeners of a delete event.
   * @param parent The parent.
   * @param child The child.
   */
  private void notifyDelete( IModelObject parent, IModelObject child)
  {
    try
    {
      if ( listeners == null) return;
      IUpdateListener[] listeners = this.listeners.toArray( new IUpdateListener[ 0]);
      for( IUpdateListener listener: listeners) listener.notifyDelete( parent, child);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
  }

  /**
   * Notify listeners of an update event.
   * @param element The original element.
   * @param update The updated element.
   */
  private void notifyUpdate( IModelObject element, IModelObject update)
  {
    try
    {
      if ( listeners == null) return;
      IUpdateListener[] listeners = this.listeners.toArray( new IUpdateListener[ 0]);
      for( IUpdateListener listener: listeners) listener.notifyUpdate( element, update);
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
  }
  
  private final ISession.Listener sessionHandler = new ISession.Listener() {
    public void notifyOpen( ISession session)
    {
      exit = false;
      thread = new Thread( connectionRunnable, "Session");
      thread.setDaemon( true);
      thread.start();
    }
    public void notifyClose( ISession session)
    {
      exit = true;
      if ( thread != null)
      {
        thread.interrupt();
        try { thread.join();} catch( InterruptedException e) {}
        thread = null;
      }
    }
    public void notifyConnect( ISession session)
    {
    }
    public void notifyDisconnect( ISession session)
    {
    }
  };
  
  /**
   * (Connection Thread)
   */
  private final Runnable connectionRunnable = new Runnable() {
    public void run()
    {
      try
      {
        DataInputStream stream = (DataInputStream)getInputStream();
        while( !exit)
        {
          int length = stream.readInt();
          if ( length == -1) break;
          
          byte[] buffer = new byte[ length];
          stream.readFully( buffer, 0, length);
          
          IModelObject message = parse( buffer);
          if ( message.isType( "queryResponse"))
          {
            handleQueryResponse( message);
          }
          else if ( message.isType( "resultTypeResponse"))
          {
            handleResultTypeResponse( message);
          }
          else if ( message.isType( "syncResponse"))
          {
            handleSyncResponse( message);
          }
          else
          {
            model.dispatch( new MessageRunnable( message));
          }
        }
      }
      catch( Exception e1)
      {
        e1.printStackTrace( System.err);
      }
      finally
      {
        exit = true;
      }
    }
  };
  
  /**
   * Parse the specified buffer into a message.
   * @param buffer The message buffer.
   * @return Returns the message.
   */
  private IModelObject parse( byte[] buffer)
  {
    try
    {
      if ( debug) System.err.printf( "Received ?K:\n");
      IModelObject content = inCompressor.decompress( buffer, 0);
      if ( debug) System.err.printf( "%s\n", (new XmlIO()).write( content));
      return content;
    }
    catch( CompressorException e)
    {
      e.printStackTrace( System.err);
      synchronizer.release();
      exit = true;
      return null;
    }
  }

  private class MessageRunnable implements Runnable
  {
    public MessageRunnable( IModelObject message)
    {
      this.message = message;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      handleMessage( message);
    }
    
    private IModelObject message;
  }
  
  public static interface IUpdateListener
  {
    /**
     * Called when a child is inserted.
     * @param parent The parent.
     * @param child The child.
     */
    public void notifyInsert( IModelObject parent, IModelObject child);
    
    /**
     * Called when a child is deleted.
     * @param parent The parent.
     * @param child The child.
     */
    public void notifyDelete( IModelObject parent, IModelObject child);
    
    /**
     * Called when an element is updated.
     * @param element The element prior to updating.
     * @param update The update to the element.
     */
    public void notifyUpdate( IModelObject element, IModelObject update);
  }
  
  private final static boolean debug = false;
  
  private IModel model;
  private boolean exit;
  private ICompressor inCompressor;
  private ICompressor outCompressor;
  private Semaphore synchronizer;
  private IModelObject queryResult;
  private ResultType queryResultType;
  private IdentityTable identities;
  private Thread thread;
  private List<IUpdateListener> listeners;
}
