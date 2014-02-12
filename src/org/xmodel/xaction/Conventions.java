package org.xmodel.xaction;

import java.util.Collections;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.diff.ConfiguredXmlMatcher;
import org.xmodel.diff.IXmlMatcher;
import org.xmodel.log.SLog;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Conventions for IXAction implementations.
 */
public class Conventions
{
  /**
   * Cache the specified object in the specified context variable.
   * @param context The context.
   * @param var The name of the variable.
   * @param object The object to be cached.
   */
  public static void putCache( IContext context, String var, Object object)
  {
    if ( object == null) 
    {
      context.set( var, Collections.<IModelObject>emptyList());
    }
    else
    {
      IModelObject holder = new ModelObject( object.getClass().getName());
      holder.setValue( object);
      context.set( var, holder);
    }
  }
  
  /**
   * Get the cached object in the specified context variable.
   * @param context The context.
   * @param var The name of the variable.
   * @return Returns null or the cached object.
   */
  public static Object getCache( IContext context, String var)
  {
    List<?> list = (List<?>)context.get( var);
    if ( list == null || list.size() == 0) return null;
    
    IModelObject holder = (IModelObject)list.get( 0);
    return holder.getValue();
  }
  
  /**
   * Get the cached object addressed by the specified expression.
   * @param context The context.
   * @param expression The expression.
   * @return Returns null or the cached object.
   */
  public static Object getCache( IContext context, IExpression expression)
  {
    IModelObject holder = expression.queryFirst( context);
    return (holder != null)? holder.getValue(): null;
  }
  
  /**
   * Returns the matcher defined for the specified locus. The first ancestor which defines a
   * matcher determines which matcher is created and returned.
   * @param doc The document.
   * @param locus The locus.
   * @return Returns the matcher.
   */
  @SuppressWarnings("unchecked")
  public static IXmlMatcher getMatcher( XActionDocument doc, IModelObject locus)
  {
    final IExpression matcherExpr = XPath.createExpression( "ancestor-or-self::*/matcher");
    final IExpression loaderExpr = XPath.createExpression( "ancestor-or-self::*/classLoader");
    
    IModelObject matcherElement = matcherExpr.queryFirst( locus);
    if ( matcherElement == null) return new ConfiguredXmlMatcher();
    
    String className = Xlate.get( matcherElement, (String)null);
    if ( className == null) 
      throw new XActionException(
        "Class name is undefined in matcher element: "+
          ModelAlgorithms.createIdentityPath( matcherElement));
    
    ClassLoader loader = null;
    IModelObject loaderElement = loaderExpr.queryFirst( locus);
    if ( loaderElement != null) loader = (ClassLoader)loaderElement.getValue();
    if ( loader == null) loader = doc.getClassLoader();
    
    try
    {
      Class<IXmlMatcher> clss = (Class<IXmlMatcher>)loader.loadClass( className);
      return clss.newInstance();
    }
    catch( Exception e)
    {
      throw new XActionException( "Unable to resolve IXmlMatcher class: "+className);      
    }
  }
  
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
  
  /**
   * Get the script from the specified expression.
   * @param document Typically the document of the XAction creating the script.
   * @param context The context.
   * @param expression The script expression.
   * @return Returns null or the script.
   */
  public static IXAction getScript( XActionDocument document, IContext context, IExpression expression)
  {
    IXAction script = null;
    if ( expression != null)
    {
      IModelObject scriptNode = expression.queryFirst( context);
      CompiledAttribute attribute = (scriptNode != null)? (CompiledAttribute)scriptNode.getAttribute( "compiled"): null;
      if ( attribute != null) script = attribute.script;
      if ( script == null)
      {
        script = document.createScript( scriptNode);
        if ( script != null)
        {
          scriptNode.setAttribute( "compiled", new CompiledAttribute( script));
        }
        else
        {
          SLog.warnf( Conventions.class, "Script not found: %s", expression);
        }
      }
    }
    return script;
  }
  
  private final static class CompiledAttribute
  {
    public CompiledAttribute( IXAction script)
    {
      this.script = script;
    }
    
    public IXAction script;
  }
}
