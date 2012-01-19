package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.log.Log;

/**
 * Conventions for IXAction implementations.
 */
public class Conventions
{
  /**
   * Returns the name of the variable to which a result will be assigned.
   * @param config The configuration.
   * @param required True if an exception should be thrown if a variable name is not provided.
   * @param aliases Deprecated aliases for the variable attribute.
   * @return Returns the name of the variable to which a result will be assigned.
   */
  public static String getVarName( IModelObject config, boolean required, String ... aliases)
  {
    String var = Xlate.get( config, "var", (String)null);
    if ( var != null) return var;
    
    for( String alias: aliases)
    {
      var = Xlate.get( config, alias, (String)null);
      if ( var != null)
      {
        IPath path = ModelAlgorithms.createIdentityPath( config);
        log.warnf( "Deprecated use of '%s' attribute, use 'var' instead at %s", alias, path);
        return var;
      }
    }
    
    if ( required) throw createException( config, "Attribute 'var' is required");
    
    return null;
  }
  
  /**
   * Create an XActionException for the specified configuration.
   * @param config The configuration.
   * @param message The message.
   * @return Returns the exception.
   */
  public static XActionException createException( IModelObject config, String message)
  {
    IPath path = ModelAlgorithms.createIdentityPath( config);
    
    StringBuilder sb = new StringBuilder();
    sb.append( message);
    sb.append( " at ");
    sb.append( path);
    
    return new XActionException( sb.toString());
  }
  
  private static Log log = Log.getLog( Conventions.class.getName());
}
