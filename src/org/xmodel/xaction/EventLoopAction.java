package org.xmodel.xaction;

import org.xmodel.BlockingDispatcher;
import org.xmodel.IDispatcher;
import org.xmodel.GlobalSettings;
import org.xmodel.xpath.expression.IContext;

/**
 * An XAction that installs a BlockingDispatcher and processes events until the dispatcher is shutdown.
 * Call this at the end of a server script when the server exports a model to its clients.
 */
public class EventLoopAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    IDispatcher dispatcher = GlobalSettings.getInstance().getModel().getDispatcher();
    if ( !(dispatcher instanceof BlockingDispatcher))
    {
      System.err.println( (dispatcher != null)? dispatcher.getClass().getName(): "Dispatcher is null!"); 
      throw new IllegalStateException( "EventLoopAction requires a BlockingDispatcher on the context model.");
    }
   
    BlockingDispatcher blocking = (BlockingDispatcher)dispatcher;
    while( true)
      if ( !blocking.process()) 
        break;
    
    return null;
  }
}
