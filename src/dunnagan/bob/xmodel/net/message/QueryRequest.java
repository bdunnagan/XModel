/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * Refer to messages.xsd for documentation.
 */
public class QueryRequest extends Request
{
  /**
   * Create a query request.
   * @param query The query expression.
   */
  public QueryRequest( String query)
  {
    super( "queryRequest");
    content.getCreateChild( "query").setValue( query);
  }
}
