package org.xmodel;

/**
 * An implementation of IDispatcher which dispatches immediately in the flow.
 */
public class ImmediateDispatcher implements IDispatcher
{
  /* (non-Javadoc)
   * @see org.xmodel.IDispatcher#execute(java.lang.Runnable)
   */
  public void execute( Runnable runnable)
  {
    runnable.run();
  }
}
