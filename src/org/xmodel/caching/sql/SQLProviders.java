package org.xmodel.caching.sql;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class SQLProviders
{
  /**
   * Returns the SQLManager for the specified reference.
   * @param context The configuration context.
   * @param annotation The caching policy annotation.
   * @return Returns the SQLManager for the specified reference.
   */
  public static ISQLProvider getProvider( IContext context, IModelObject annotation) throws CachingException
  {
    IExpression providerExpr = Xlate.childGet( annotation, "provider", Xlate.get( annotation, "provider", (IExpression)null));
    IModelObject providerAnnotation = providerExpr.queryFirst( context);
    if ( providerAnnotation == null) 
      throw new CachingException( String.format(
        "Provider not found for expression '%s'",
        providerExpr));
    
    try
    {
      return SQLProviderFactory.getProvider( providerAnnotation);
    }
    catch( Exception e)
    {
      throw new CachingException( e.getMessage());
    }
  }

}
