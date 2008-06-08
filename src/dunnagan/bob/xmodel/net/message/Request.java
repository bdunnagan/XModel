/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

import java.util.Random;

/**
 * Refer to messages.xsd for documentation.
 */
public class Request extends Message
{
  public Request( String type)
  {
    super( type, Long.toString( index++, 36).toUpperCase());
  }

  /**
   * Returns the id of the request.
   * @return Returns the id of the request.
   */
  public String getID()
  {
    return content.getID();
  }
  
  // initialize index
  static
  {
    Random random = new Random();
    index = random.nextInt( Integer.MAX_VALUE);
  }
  
  private static long index;
}
