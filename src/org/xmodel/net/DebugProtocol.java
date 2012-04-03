package org.xmodel.net;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;

/**
 * The debugging protocol.
 */
public class DebugProtocol
{
  public static enum Operation { stepOver, stepIn, stepOut, pause, resume};
  
  /**
   * Create a document containing the variable assignments in the specified context and the specified result array.
   * @param context The debugging context.
   * @param location The debugging location.
   * @return Returns the document root.
   */
  public static IModelObject buildResponse( IContext context, IModelObject location)
  {
    ModelObject response = new ModelObject( "response");

    // store variable names in scope
    for( String varName: context.getScope().getVariables())
    {
      ModelObject varNode = new ModelObject( "var");
      varNode.setValue( varName);
      response.addChild( varNode);
    }
    
    // store location
    IPath path = ModelAlgorithms.createIdentityPath( location, true);
    Xlate.childSet( response, "location", path.toString());
    
    return response;
  }
}
