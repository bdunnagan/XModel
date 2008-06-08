/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * Refer to messages.xsd for documentation.
 */
public class Response extends Message
{
  /**
   * Create a response to the specified request with the specified status.
   * @param id The id of the request.
   * @param type The type of response.
   */
  public Response( String id, String type)
  {
    super( type, id);
  }
  
  /**
   * Set the error message.
   * @param message The error message.
   */
  public void setError( String message)
  {
    content.setAttribute( "error", message);
  }
}
