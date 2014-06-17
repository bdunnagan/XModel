package org.xmodel.net.transport.amqp;

public class AmqpQueueNames
{
  /**
   * Returns the name of the client-side output queue.
   * @param queue The name with which an endpoint has registered.
   * @return Returns the name of the response queue.
   */
  public static String getInputQueue( String queue)
  {
    return queue + "_C";
  }
  
  /**
   * Returns the name of the server-side output queue.
   * @param queue The name with which an endpoint has registered.
   * @return Returns the name of the response queue.
   */
  public static String getOutputQueue( String queue)
  {
    return queue + "_S";
  }
}
