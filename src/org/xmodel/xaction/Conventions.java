package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;

/**
 * Conventions for IXAction implementations.
 */
public class Conventions
{
  /**
   * Returns the factory defined for the specified locus. The first ancestor which defines a
   * factory determines which factory is created and returned.
   * @param locus The locus.
   * @return Returns the factory.
   */
  @SuppressWarnings("unchecked")
  public static IModelObjectFactory getFactory( IModelObject locus)
  {
    IExpression factoryExpr = XPath.createExpression( "ancestor-or-self::*/factory");
    IExpression loaderExpr = XPath.createExpression( "ancestor-or-self::*/classLoader");
      
    IModelObject factoryElement = factoryExpr.queryFirst( locus);
    if ( factoryElement == null) return new ModelObjectFactory();
    
    String className = Xlate.get( factoryElement, (String)null);
    if ( className == null) 
    {
      throw new XActionException(
        "Class name is undefined in factory element: "+
          ModelAlgorithms.createIdentityPath( factoryElement));
    }
    
    ClassLoader loader = null;
    IModelObject loaderElement = loaderExpr.queryFirst( locus);
    if ( loaderElement != null) loader = (ClassLoader)loaderElement.getValue();
    if ( loader == null) loader = Conventions.class.getClassLoader();
    
    try
    {
      Class<IModelObjectFactory> clss = (Class<IModelObjectFactory>)loader.loadClass( className);
      return clss.newInstance();
    }
    catch( Exception e)
    {
      throw new XActionException( "Unable to resolve IModelObjectFactory class: "+className);
    }
  }
  
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
        SLog.warnf( Conventions.class, "Deprecated use of '%s' attribute, use 'var' instead at %s", alias, path);
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
}
