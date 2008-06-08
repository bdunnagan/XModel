/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * Refer to messages.xsd for documentation.
 */
public class SyncRequest extends Request
{
  /**
   * Create a sync request for the specified remote id.
   * @param remoteID The remote id.
   */
  public SyncRequest( String remoteID)
  {
    super( "syncRequest");
    content.setValue( remoteID);
  }

  /**
   * Perform the query specified by this request and return the result.
   * @return Returns the result of the query.
   */
  public List<IModelObject> performQuery() throws MessageException
  {
    String querySpec = Xlate.childGet( content, "query", (String)null);
    if ( querySpec == null) createException( "Missing query.");
    
    IExpression query = XPath.createExpression( querySpec);
    if ( query == null) throw new MessageException( "Syntax error in query: "+querySpec);
    
    return query.query( null);
  }
}
