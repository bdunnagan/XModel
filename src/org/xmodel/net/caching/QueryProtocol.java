package org.xmodel.net.caching;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.Reference;
import org.xmodel.Xlate;
import org.xmodel.net.robust.IServerSession;
import org.xmodel.net.robust.TimeoutException;
import org.xmodel.net.robust.XmlClient;
import org.xmodel.net.robust.XmlMessage;
import org.xmodel.net.robust.XmlServer;
import org.xmodel.net.robust.XmlServerSession;
import org.xmodel.net.robust.XmlServer.IReceiver;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class which implements the XML client/server query protocol. 
 * This receiver handles <i>query</i> messages.
 */
public class QueryProtocol implements IReceiver
{
  public QueryProtocol( Context context)
  {
    this.context = context;
  }

  public static class Query
  {
    public String id;
    public String xpath;
    public boolean listen;
  }

  /**
   * Send a query request and return the result.
   * @param client The client to send from.
   * @param timeout The response timeout.
   * @param query The query xpath.
   * @return Returns one of List, String, Number or Boolean.
   */
  public static Object sendQueryRequest( XmlClient client, int timeout, String query) throws QueryException
  {
    try
    {
      IModelObject message = XmlMessage.createSimple( "query", query);
      Xlate.set( message, "action", "query");
      IModelObject response = client.sendAndWait( message, timeout);
      return parseResponse( response);
    }
    catch( TimeoutException e)
    {
      throw new QueryException( "Timeout waiting for query response: "+query, e);
    }
    catch( InterruptedException e)
    {
      throw new QueryException( "Query interrupted: "+query, e);
    }
  }
  
  /**
   * Send a bind request and return the result. The expression will be bound on the server
   * and asynchronous messages will be sent by the server to keep the client apprised of
   * changes to the result of the expression.  In addition, if the listen flag is true and 
   * expression returns a node-set, then each node tree will be completely instrumented with 
   * non-syncing listeners and asynchronous messages will be sent for any change made to a node
   * in the trees. Asynchronous messages can be canceled by sending a cancel message. The Query 
   * argument should be retained for the purpose of canceling.
   * @param client The client to send from.
   * @param timeout The response timeout.
   * @param query The query.
   * @return Returns one of List, String, Number or Boolean.
   */
  public static Object sendBindRequest( XmlClient client, int timeout, Query query) throws QueryException
  {
    try
    {
      IModelObject message = XmlMessage.createSimple( "query", query.xpath);
      IModelObject response = client.sendAndWait( message, timeout);
      Xlate.set( message, "action", query.listen? "listen": "bind");
      query.id = Xlate.get( response, "qid", "");
      return parseResponse( response);
    }
    catch( TimeoutException e)
    {
      throw new QueryException( "Timeout waiting for query bind response: "+query.xpath, e);
    }
    catch( InterruptedException e)
    {
      throw new QueryException( "Query bind interrupted: "+query.xpath, e);
    }
  }
  
  /**
   * Cancel a previous bind request. After this method returns, no further asynchronous messages
   * will be sent by the server for the specified query. If there is no response from the server
   * within the timeout period, a QueryException will be thrown.
   * @param client The client.
   * @param timeout The response timeout.
   * @param query The query to be canceled.
   */
  public static void sendCancelRequest( XmlClient client, int timeout, Query query) throws QueryException
  {
    try
    {
      IModelObject message = XmlMessage.createSimple( "cancel", query.id);
      Xlate.set( message, "action", "cancel");
      client.sendAndWait( message, timeout);
    }
    catch( TimeoutException e)
    {
      throw new QueryException( "Timeout waiting for query cancel response: "+query.xpath, e);
    }
    catch( InterruptedException e)
    {
      throw new QueryException( "Query cancel interrupted: "+query.xpath, e);
    }
  }

  /**
   * Send an asynchronous notification message to the client for the specified query.
   * @param query The query.
   * @param nodes The nodes that were added.
   */
  protected void sendAddUpdate( Query query, List<IModelObject> nodes)
  {
    IModelObject message = new ModelObject( "addUpdate");
    Xlate.set( message, "qid", query.id);
    for( IModelObject node: nodes)
    {
      int hashCode = (node instanceof Reference)? ((Reference)node).nativeHashCode(): node.hashCode();
      ModelObject result = new ModelObject( "node", Integer.toString( hashCode));
      result.addChild( node.cloneTree());
      message.addChild( result);
    }
  }
  
  /**
   * Send an asynchronous notification message to the client for the specified query.
   * @param query The query.
   * @param nodes The nodes that were removed.
   */
  protected void sendRemoveUpdate( Query query, List<IModelObject> nodes)
  {
    IModelObject message = new ModelObject( "removeUpdate");
    Xlate.set( message, "qid", query.id);
    for( IModelObject node: nodes)
    {
      int hashCode = (node instanceof Reference)? ((Reference)node).nativeHashCode(): node.hashCode();
      ModelObject result = new ModelObject( "node", Integer.toString( hashCode));
      message.addChild( result);
    }
  }
  
  /**
   * Send an asynchronous notification message to the client for the specified query.
   * @param query The query.
   * @param newValue The new value.
   */
  protected void sendChangeUpdate( Query query, String newValue)
  {
    IModelObject message = new ModelObject( "changeUpdate");
    Xlate.set( message, "qid", query.id);
    message.setValue( newValue);
  }
  
  /**
   * Send an asynchronous notification message to the client for the specified query.
   * @param query The query.
   * @param newValue The new value.
   */
  protected void sendChangeUpdate( Query query, double newValue)
  {
    IModelObject message = new ModelObject( "changeUpdate");
    Xlate.set( message, "qid", query.id);
    message.setValue( newValue);
  }
  
  /**
   * Send an asynchronous notification message to the client for the specified query.
   * @param query The query.
   * @param newValue The new value.
   */
  protected void sendChangeUpdate( Query query, boolean newValue)
  {
    IModelObject message = new ModelObject( "changeUpdate");
    Xlate.set( message, "qid", query.id);
    message.setValue( newValue);
  }
  
  /**
   * Send an asynchronous notification message to the client for the specified query.
   * @param query The query.
   * @param newValue The new value.
   */
  protected void sendValueUpdate( Query query, String newValue)
  {
    IModelObject message = new ModelObject( "valueUpdate");
    Xlate.set( message, "qid", query.id);
    message.setValue( newValue);
  }
  
  /**
   * Parse the query response and return the result. 
   * @param response The query response.
   * @return Returns the result of the query.
   */
  public static Object parseResponse( IModelObject response) throws QueryException
  {
    String type = Xlate.get( response, "type", ""); 
    if ( type.equals( "nodes"))
    {
      List<IModelObject> children = response.getChildren();
      for( IModelObject child: children) child.removeFromParent();
      return children;
    }
    else if ( type.equals( "string"))
    {
      return Xlate.get( response, "");
    }
    else if ( type.equals( "number"))
    {
      return Xlate.get( response, 0d);
    }
    else if ( type.equals( "boolean"))
    {
      return Xlate.get( response, true);
    }
    else if ( type.equals( "exception"))
    {
      throw new QueryException( Xlate.get( response, ""));
    }
    else
    {
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.robust.XmlServer.IReceiver#handle(org.xmodel.net.robust.XmlServer, 
   * org.xmodel.net.robust.IServerSession, org.xmodel.IModelObject)
   */
  public void handle( XmlServer server, IServerSession session, IModelObject message)
  {
    // squirrel away server for sending async messages
    if ( this.server == null) this.server = server;
    
    // switch on action
    String action = Xlate.get( message, "action", "");
    if ( action.equals( "query"))
    {
      handleQuery( server, session, message);
    }
    else if ( action.equals( "bind"))
    {
      handleBind( server, session, message);
    }
    else if ( action.equals( "listen"))
    {
      handleListen( server, session, message);
    }
    else if ( action.equals( "cancel"))
    {
      handleCancel( server, session, message);
    }
  }
    
  /**
   * Handle a query request.
   * @param server The server.
   * @param session The server session that received the request.
   * @param message The request.
   */
  private void handleQuery( XmlServer server, IServerSession session, IModelObject message)
  {
    IModelObject response = new ModelObject( "queryResponse");
    response.setID( message.getID());
    populateQueryResult( message, response);
    ((XmlServerSession)session).send( response);
  }
  
  /**
   * Handle a query bind request.
   * @param server The server.
   * @param session The server session that received the request.
   * @param message The request.
   */
  private void handleBind( XmlServer server, IServerSession session, IModelObject message)
  {
    IModelObject response = new ModelObject( "queryResponse");
    response.setID( message.getID());
    populateQueryResult( message, response);
    ((XmlServerSession)session).send( response);
    
    // bind result
    
  }
  
  /**
   * Handle a query listen request.
   * @param server The server.
   * @param session The server session that received the request.
   * @param message The request.
   */
  private void handleListen( XmlServer server, IServerSession session, IModelObject message)
  {
    IModelObject response = new ModelObject( "queryResponse");
    response.setID( message.getID());
    populateQueryResult( message, response);
    ((XmlServerSession)session).send( response);
  }
  
  /**
   * Handle a query cancel request.
   * @param server The server.
   * @param session The server session that received the request.
   * @param message The request.
   */
  private void handleCancel( XmlServer server, IServerSession session, IModelObject message)
  {
    IModelObject response = new ModelObject( "queryResponse");
    response.setID( message.getID());
    ((XmlServerSession)session).send( response);
  }
  
  /**
   * Populate the query result for the specified query request.
   * @param request The request.
   * @param response The response.
   * @return Returns null or the node-set if the expression returns a node-set.
   */
  private List<IModelObject> populateQueryResult( IModelObject request, IModelObject response)
  {
    String[] params = XmlMessage.parseSimple( request);
    try
    {
      IExpression expression = XPath.compileExpression( params[ 0]);
      switch( expression.getType( context))
      {
        case NODES:   
        {
          List<IModelObject> nodes = expression.evaluateNodes( context);
          createResponse( response, nodes);
          return nodes;
        }
        
        case STRING:  createResponse( response, expression.evaluateString( context)); break;
        case NUMBER:  createResponse( response, expression.evaluateNumber( context)); break;
        case BOOLEAN: createResponse( response, expression.evaluateBoolean( context)); break;
      }
    }
    catch( PathSyntaxException e)
    {
      createResponse( response, e); 
    }
    catch( ExpressionException e)
    {
      createResponse( response, e);
    }
    
    return null;
  }
  
  /**
   * Create a query response for a node-set.
   * @param response The response.
   * @param nodes The result of the query.
   * @return Returns the query response.
   */
  private IModelObject createResponse( IModelObject response, List<IModelObject> nodes)
  {
    Xlate.set( response, "type", "nodes");
    for( IModelObject node: nodes)
      response.addChild( node.cloneTree());
    return response;
  }
  
  /**
   * Create a query response for a string.
   * @param response The response.
   * @param string The result of the query.
   * @return Returns the query response.
   */
  private IModelObject createResponse( IModelObject response, String value)
  {
    Xlate.set( response, "type", "string");
    response.setValue( value);
    return response;
  }
  
  /**
   * Create a query response for a number.
   * @param response The response.
   * @param number The result of the query.
   * @return Returns the query response.
   */
  private IModelObject createResponse( IModelObject response, double value)
  {
    Xlate.set( response, "type", "number");
    response.setValue( value);
    return response;
  }
    
  /**
   * Create a query response for a boolean.
   * @param response The response.
   * @param number The result of the query.
   * @return Returns the query response.
   */
  private IModelObject createResponse( IModelObject response, boolean value)
  {
    Xlate.set( response, "type", "boolean");
    response.setValue( value);
    return response;
  }
    
  /**
   * Create a query response for an exception.
   * @param response The response.
   * @param e The exception.
   * @return Returns the query response.
   */
  private IModelObject createResponse( IModelObject response, Exception e)
  {
    Xlate.set( response, "type", "exception");
    response.setValue( e.getMessage());
    return response;
  }
    
  private Context context;
  private XmlServer server;
}
