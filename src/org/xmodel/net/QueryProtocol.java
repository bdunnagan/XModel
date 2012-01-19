package org.xmodel.net;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.PathSyntaxException;
import org.xmodel.Xlate;
import org.xmodel.xpath.AttributeNode;
import org.xmodel.xpath.TextNode;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A class responsible for translating the results of a remote query invocation into a document.
 * Local context variables are stored in the request.  The local context object is ignored.
 */
public class QueryProtocol
{
  /**
   * Create a document containing the variable assignments in the specified context and the query.
   * @param context The context.
   * @param script The script.
   * @return Returns the document root.
   */
  public static IModelObject buildRequest( IContext context, String query)
  {
    ModelObject request = new ModelObject( "request");

    // store variable assignments
    IVariableScope scope = context.getScope();
    for( String variable: scope.getVariables())
    {
      ModelObject assignment = new ModelObject( "assign");
      assignment.setAttribute( "name", variable);
      assignment.setValue( scope.get( variable));
      request.addChild( assignment);
    }

    // store query
    request.getCreateChild( "query").setValue( query);
    
    return request;
  }
  
  /**
   * Transfer the variable assignments in the specified request to the specified context and return the script.
   * @param request The request document.
   * @param context The context.
   * @return Returns the query.
   */
  public static IExpression readRequest( IModelObject request, IContext context) throws PathSyntaxException
  {
    // read variable assignments
    IVariableScope scope = context.getScope();
    for( IModelObject assignment: request.getChildren( "assignment"))
    {
      String variable = Xlate.get( assignment, "name", (String)null);
      Object value = assignment.getValue();
      scope.set( variable, value);
    }

    // read query
    return XPath.compileExpression( Xlate.get( request, "query", (String)null));
  }
  
  /**
   * Create a document containing the query result.
   * @oaram result The query result.
   * @return Returns the document root.
   */
  public static IModelObject buildResponse( Object result)
  {
    ModelObject response = new ModelObject( "response");

    if ( result instanceof List)
    {
      List<?> list = (List<?>)result;
      if ( list.size() > 0 && list.get( 0) instanceof IModelObject)
      {
        for( Object object: list)
        {
          IModelObject node = (IModelObject)object;
          if ( node instanceof AttributeNode)
          {
            // not supported
          }
          else if ( node instanceof TextNode)
          {
            // not supported
          }
          else
          {
            response.addChild( node);
          }
        }
        
        return response;
      }
    }
    
    response.setAttribute( "result", result);
    return response;
  }
  
  /**
   * Returns the query result.
   * @param response The response document.
   * @return Returns the query result.
   */
  public static Object readResponse( IModelObject response)
  {
    Object result = response.getAttribute( "result");
    if ( result != null) return result;
    return response.getChildren();
  }
}
