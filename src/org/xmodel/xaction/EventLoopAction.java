package org.xmodel.xaction;

import org.xmodel.BlockingDispatcher;
import org.xmodel.IDispatcher;
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
    IDispatcher dispatcher = context.getModel().getDispatcher();
    if ( !(dispatcher instanceof BlockingDispatcher))
    {
      throw new IllegalStateException( 
          "EventLoopAction requires a BlockingDispatcher on the context model.");
    }
    
    BlockingDispatcher blocking = (BlockingDispatcher)dispatcher;
    while( blocking.process());
    return null;
  }
}
