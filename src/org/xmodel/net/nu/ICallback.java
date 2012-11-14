package org.xmodel.net.nu;

import org.xmodel.xpath.expression.IContext;

/**
 * An interface for receiving notifications of asynchronous execution events.
 */
public interface ICallback
{
  /**
   * Called when an asynchronous remote invocation completes.
   * @param context The context in which the remote invocation occurred.
   */
  public void onComplete( IContext context);
  
  /**
   * Called when an asynchronous remote invocation completes successfully.
   * @param context The context in which the remote invocation occurred.
   * @param results The results of the execution.
   */
  public void onSuccess( IContext context, Object[] results);
  
  /**
   * Called when an asynchronous remote invocation fails.
   * @param context The context in which the remote invocation occurred.
   */
  public void onError( IContext context, String error);
}
