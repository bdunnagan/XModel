package org.xmodel.xaction;

import org.xmodel.BlockingDispatcher;
import org.xmodel.IDispatcher;
import org.xmodel.IModel;
import org.xmodel.concurrent.SerialExecutorDispatcher;
import org.xmodel.net.XioServer;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that creates an XIO server.
 */
public class ServerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    addressExpr = document.getExpression( "address", true);
    portExpr = document.getExpression( "port", true);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    setDispatcher( context);

    String address = addressExpr.evaluateString( context);
    int port = (int)portExpr.evaluateNumber( context);
    
    XioServer server = new XioServer( context, context);
    server.start( address, port);
    
    return null;
  }
  
  private void setDispatcher( IContext context)
  {
    IModel model = context.getModel();
    
    IDispatcher dispatcher = model.getDispatcher();
    if ( dispatcher instanceof BlockingDispatcher)
    {
      BlockingDispatcher blocking = (BlockingDispatcher)dispatcher;
      
      // install new dispatcher
      model.setDispatcher( new SerialExecutorDispatcher( model, 1));

      // insure dispatcher not empty
      blocking.execute( new Runnable() {
        public void run()
        {
        }
      });
      
      // drain
      blocking.process();
      
      // shutdown
      blocking.shutdown( false);
    }
  }
  
  private IExpression addressExpr;
  private IExpression portExpr;
}
