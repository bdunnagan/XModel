package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.transform.ISQLRowTransform;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.expression.IContext;

/**
 * An ICachingPolicy that uses an arbitrary SQL query statement to load data.  The caching policy also generates
 * statements for inserting, updating and deleting elements when the readonly flag is false.  Some flexibility is
 * supported as to how table columns are transformed into xml.  However, the following transformation rules are
 * always applied:
 * <ul>
 * <li>Each table row is transformed into an IModelObject that becomes a child of the reference being sync'ed.</li>
 * <li>Data is either stored in an attribute of the row element or in the value of a child of the row element.</li>
 * </ul>
 * <p>
 * By default, the names of row element attributes or children are the same as the name of the corresponding 
 * table column.
 * <p>
 * When the <i>shallow</i> flag is true, and indexed columns are stored in static attributes of the row elements,
 * the rows may be searched by xpath expressions without their content being pulled from the database.  By default,
 * when the <i>shallow</i> flag is true, the primary key is stored in a static attribute.
 */
public class SQLCachingPolicy extends ConfiguredCachingPolicy
{
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    database = Xlate.childGet( annotation, "database", (String)null);
    sqlQuery = Xlate.childGet( annotation, "query", (String)null);
    readonly = Xlate.childGet( annotation, "readonly", true);
    shallow = Xlate.childGet( annotation, "shallow", true);
    
    configureProvider( annotation);
    configureStaticAttributes( annotation);
    configureRowTransform( annotation);
  }

  /**
   * Configure the ISQLProvider instance from the annotation.
   * @param annotation The caching policy annotation.
   */
  private void configureProvider( IModelObject annotation) throws CachingException
  {
    try
    {
      provider = SQLProviderFactory.getProvider( annotation);
    }
    catch( Exception e)
    {
      throw new CachingException( e.getMessage());
    }
  }
  
  /**
   * Set the static attributes from the specified annotation.
   * @param annotation The caching policy annotation.
   */
  private void configureStaticAttributes( IModelObject annotation) throws CachingException
  {
  }
  
  /**
   * Configure the ISQLRowTransform from the annotation.
   * @param annotation The caching policy annotation.
   */
  private void configureRowTransform( IModelObject annotation) throws CachingException
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    Connection connection = provider.leaseConnection();
    connection.setCatalog( catalog);
    
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    //transform.importRow( )
  }
  
  private ISQLProvider provider;
  private String database;
  private String sqlQuery;
  private boolean readonly;
  private boolean shallow;
  private ISQLRowTransform transform;
}
