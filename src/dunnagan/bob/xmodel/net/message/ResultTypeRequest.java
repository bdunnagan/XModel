/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * Refer to messages.xsd for documentation.
 */
public class ResultTypeRequest extends Request
{
  /**
   * Create a result type request for the specified query.
   * @param query The query expression.
   */
  public ResultTypeRequest( String query)
  {
    super( "resultTypeRequest");
    content.getCreateChild( "query").setValue( query);
  }
}
