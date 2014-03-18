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
    return "C_"+queue;
  }
  
  /**
   * Returns the name of the server-side output queue.
   * @param server The name of the server to which the queue belongs.
   * @param queue The name with which an endpoint has registered.
   * @return Returns the name of the response queue.
   */
  public static String getOutputQueue( String server, String queue)
  {
    return "S_"+server+":"+queue;
  }
}
