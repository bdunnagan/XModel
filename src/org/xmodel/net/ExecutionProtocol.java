package org.xmodel.net;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.AttributeNode;
import org.xmodel.xpath.TextNode;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A class responsible for translating the results of a remote script invocation into a document.
 * Local context variables are stored in the request.  The local context object is ignored.
 * On the remote side, the remote variable assignments are stored in the response along with
 * the invocation result array.
 */
public class ExecutionProtocol
{
  /**
   * Create a document containing the variable assignments in the specified context and the specified script.
   * @param context The context.
   * @param variables The variables to be passed.
   * @param script The script.
   * @return Returns the document root.
   */
  public static IModelObject buildRequest( IContext context, String[] variables, IModelObject script)
  {
    ModelObject request = new ModelObject( "request");

    // store variable assignments
    buildScope( context, variables, request);
    
    // store script
    request.addChild( script.cloneTree());
    
    return request;
  }
  
  /**
   * Transfer the variable assignments in the specified request to the specified context and return the script.
   * @param request The request document.
   * @param context The context.
   * @return Returns the script to be executed.
   */
  public static IModelObject readRequest( IModelObject request, IContext context)
  {
    // read variable assignments
    readScope( request, context);
    
    // read script
    return request.getFirstChild( "script");
  }
  
  /**
   * Create a document containing the variable assignments in the specified context and the specified result array.
   * @param context The context.
   * @param objects The result array.
   * @return Returns the document root.
   */
  public static IModelObject buildResponse( IContext context, Object[] objects)
  {
    ModelObject response = new ModelObject( "response");

    // store variable assignments
    buildScope( context, null, response);

    // store results
    if ( objects != null)
    {
      ModelObject results = new ModelObject( "results");
      response.addChild( results);
      
      for( Object object: objects)
      {
        ModelObject result = new ModelObject( "result");
        result.setValue( object);
        results.addChild( result);
      }
    }
    
    return response;
  }
  
  /**
   * Transfer the variable assignments in the specified request to the specified context and return the result array.
   * @param response The response document.
   * @param context The context.
   * @return Returns null or the results array.
   */
  public static Object[] readResponse( IModelObject response, IContext context)
  {
    // read variable assignments
    readScope( response, context);
    
    // read results
    Object[] objects = null;
    
    IModelObject results = response.getFirstChild( "results");
    if ( results != null)
    {
      List<IModelObject> children = results.getChildren( "result");
      objects = new Object[ children.size()];
      for( int i=0; i<children.size(); i++)
      {
        objects[ i] = children.get( i).getValue();
      }
    }
    
    return objects;
  }
  
  /**
   * Add variable assignments to the specified request/response element.
   * @param context The context.
   * @param variables The variables to consider.
   * @param root The request or response element.
   */
  @SuppressWarnings("unchecked")
  public static void buildScope( IContext context, String[] variables, IModelObject root)
  {
    IVariableScope scope = context.getScope();
    if ( variables == null) variables = scope.getVariables().toArray( new String[ 0]);
    
    for( String variable: variables)
    {
      ModelObject assignment = new ModelObject( "assign");
      assignment.setAttribute( "name", variable);

      Object object = scope.get( variable);
      if ( object != null)
      {
        if ( scope.getType( variable, context) == ResultType.NODES)
        {
          for( IModelObject node: (List<IModelObject>)object)
          {
            if ( node instanceof TextNode || node instanceof AttributeNode)
            {
              assignment.setValue( node.getValue());
            }
            else
            {
              assignment.addChild( node.cloneTree());
            }
          }
        }
        else
        {
          assignment.setValue( object);
        }
        root.addChild( assignment);
      }
    }
  }
  
  /**
   * Transfer variable assignments from the specified request or response to the specified context.
   * @param root The request or response element.
   * @param context The target context.
   */
  public static void readScope( IModelObject root, IContext context)
  {
    IVariableScope scope = context.getScope();
    for( IModelObject assignment: root.getChildren( "assign"))
    {
      String variable = Xlate.get( assignment, "name", (String)null);
      if ( assignment.getNumberOfChildren() > 0)
      {
        scope.set( variable, new ArrayList<IModelObject>( assignment.getChildren()));
        assignment.removeChildren();
      }
      else
      {
        Object value = assignment.getValue();
        scope.set( variable, value);
      }
    }
  }
}
