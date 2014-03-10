package org.xmodel.net.transport.amqp;

public class AmqpQueueNames
{
  /**
   * Returns the name of the request queue for the specified registration name.
   * @param name The name with which an endpoint has registered.
   * @return Returns the name of the request queue.
   */
  public static String getOutputQueue( String name)
  {
    return name+"_out";
  }
  
  /**
   * Returns the name of the response queue for the specified registration name.
   * @param name The name with which an endpoint has registered.
   * @return Returns the name of the response queue.
   */
  public static String getInputQueue( String name)
  {
    return name+"_in";
  }
}
