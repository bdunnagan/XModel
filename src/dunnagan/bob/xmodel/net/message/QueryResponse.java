/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

/**
 * Refer to messages.xsd for documentation.
 */
public class QueryResponse extends Response
{
  public QueryResponse( String id)
  {
    super( id, "queryResponse");
  }
}
