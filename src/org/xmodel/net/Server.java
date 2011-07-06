package org.xmodel.net;

import java.io.IOException;

import org.xmodel.IDispatcher;
import org.xmodel.IModelObject;
import org.xmodel.ManualDispatcher;
import org.xmodel.ModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.net.stream.Connection;
import org.xmodel.net.stream.TcpServer;
import org.xmodel.xaction.XAction;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A class that implements the server-side of the network caching policy protocol.
 * This class is not thread-safe.
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
    if ( dispatchers.empty()) pushDispatcher( context.getModel().getDispatcher());
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#onClose(org.xmodel.net.stream.Connection)
   */
  @Override
  public void onClose( Connection connection)
  {
    super.onClose( connection);
    
    ConnectionInfo info = map.get( connection);
    if ( info != null) doDetach( connection, info.xpath);
    
    map.remove( connection);
  }

  /**
   * Find the element on the specified xpath and attach listeners.
   * @param sender The sender.
   * @param xpath The xpath expression.
   */
  private void doAttach( Connection sender, String xpath) throws IOException
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
        copy = encode( sender, target);
        
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
  private void doDetach( Connection sender, String xpath)
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
   * Encode the specified element that is the root of the attachment.
   * @param connection The connection.
   * @param element The element to be copied.
   * @return Returns the copy.
   */
  private IModelObject encode( Connection connection, IModelObject element)
  {
    return encode( connection, element, true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleAttachRequest(org.xmodel.net.nu.Connection, java.lang.String)
   */
  @Override
  protected void handleAttachRequest( Connection sender, String xpath)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new AttachRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.nu.Protocol#handleDetachRequest(org.xmodel.net.nu.Connection, java.lang.String)
   */
  @Override
  protected void handleDetachRequest( Connection sender, String xpath)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new DetachRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleQueryRequest(org.xmodel.net.Connection, java.lang.String)
   */
  @Override
  protected void handleQueryRequest( Connection sender, String xpath)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new QueryRunnable( sender, xpath));
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleDebugStepIn(org.xmodel.net.Connection)
   */
  @Override
  protected void handleDebugStepIn( Connection sender)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepIn();
      }
    });
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleDebugStepOut(org.xmodel.net.Connection)
   */
  @Override
  protected void handleDebugStepOut( Connection sender)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepOut();
      }
    });
  }

  /* (non-Javadoc)
   * @see org.xmodel.net.Protocol#handleDebugStepOver(org.xmodel.net.Connection)
   */
  @Override
  protected void handleDebugStepOver( Connection sender)
  {
    IDispatcher dispatcher = dispatchers.peek();
    dispatcher.execute( new Runnable() {
      public void run()
      {
        XAction.getDebugger().stepOver();
      }
    });
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
        doAttach( sender, xpath);
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
      doDetach( sender, xpath);
    }

    private Connection sender;
    private String xpath;
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
  
  private static Log log = Log.getLog( "org.xmodel.net");

  private TcpServer server;
  private IContext context;
  
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
      boolean f = false;
      if ( f)
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
