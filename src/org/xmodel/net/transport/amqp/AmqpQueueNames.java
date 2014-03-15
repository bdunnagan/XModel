package org.xmodel.net.transport.amqp;

public class AmqpQueueNames
{
  /**
   * Returns the name of the response queue for the specified registration name.
   * @param queue The name with which an endpoint has registered.
   * @return Returns the name of the response queue.
   */
  public static String getInputQueue( String queue)
  {
    return "C_"+queue;
  }
  
  /**
   * Returns the name of the request queue for the specified registration name.
   * @param queue The name with which an endpoint has registered.
   * @return Returns the name of the request queue.
   */
  public static String getOutputQueue( String queue)
  {
    return "S_"+queue;
  }
  
  /**
   * Returns the name of the response queue for the specified registration name, and server.  This type
   * of queue is qualified with the name of the server queue to insure that responses are routed to
   * the correct server when multiple clients register with the same name.
   * @param server The name of the server to which the queue belongs.
   * @param queue The name with which an endpoint has registered.
   * @return Returns the name of the response queue.
   */
  public static String getOutputQueue( String server, String queue)
  {
    return "S_"+server+":"+queue;
  }

  /**
   * @return Returns the name of the heartbeat echo-request channel.
   */
  public static String getHeartbeatExchange()
  {
    return "heartbeat";
  }
}
