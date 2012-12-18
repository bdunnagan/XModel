package org.xmodel.caching.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.transform.DefaultSQLRowTransform;
import org.xmodel.caching.sql.transform.ISQLColumnTransform;
import org.xmodel.caching.sql.transform.SQLColumnMetaData;
import org.xmodel.caching.sql.transform.SQLDirectColumnTransform;
import org.xmodel.caching.sql.transform.SimpleSQLParser;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.xaction.Conventions;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

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
 * the rows may be searched by xpath expressions without their content being pulled from the database.
 * <p>
 * There are two ways to specify the names of columns that will be stored in row element attributes: any column
 * with an alias preceded by '@', and the columns specified in the 'attributes' element of the annotation.
 * <p>
 * Annotation elements:
 * <ul>
 * <li>provider - an optional expression that identifies the implementation of ISQLProvider</li>
 * <li>query - a string expression that gives the query to be executed</li>
 * <li>static - an optional string expression giving a comma-separated static attributes</li>
 * <li>attributes - an optional string expression giving a comma-separated list of column names to store in attributes</li>
 * <li>shallow - a boolean expression that specifies whether row content is loaded on demand (default: false())</li>
 * <li>update - a boolean expression that specifies whether the database is updated when the model is changed (default: false())</li>
 * </ul>
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
    
    this.metadata = new SQLColumnMetaData();
    this.metadataReady = false;
    
    IExpression providerExpr = Xlate.childGet( annotation, "provider", (IExpression)null);
    provider = (providerExpr != null)? (ISQLProvider)Conventions.getCache( context, providerExpr): null;
    
    IExpression queryExpr = Xlate.childGet( annotation, "query", (IExpression)null);
    query = queryExpr.evaluateString( context);
    parser = new SimpleSQLParser( query);
    
    update = Xlate.childGet( annotation, "update", false);
    shallow = Xlate.childGet( annotation, "shallow", true);
    
    configureProvider( annotation);
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
   * Configure the ISQLRowTransform from the annotation.
   * @param annotation The caching policy annotation.
   */
  private void configureRowTransform( IModelObject annotation) throws CachingException
  {
    // create list of column names that will be attributes
    Set<String> attributes = new HashSet<String>();
    
    // static attributes
    for( String staticAttribute: getStaticAttributes())
      attributes.add( staticAttribute);
    
    // other attributes
    for( IModelObject element: annotation.getChildren( "attribute"))
    {
      Object attribute = element.getValue();
      if ( attribute != null) attributes.add( attribute.toString());
    }
    
    // create row transform
    transform = new DefaultSQLRowTransform( parser.getTableName());
    for( String column: parser.getColumnNames())
    {
      ISQLColumnTransform columnTransform = new SQLDirectColumnTransform( metadata, column, column, attributes.contains( column));
      transform.defineColumn( column, columnTransform);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    IModelObject prototype = new ModelObject( reference.getType());
    
    try
    {
      Connection connection = provider.leaseConnection();
      PreparedStatement statement = connection.prepareStatement( query);
      ResultSet rowCursor = statement.executeQuery();
      
      if ( !metadataReady) 
      {
        metadata.setColumnTypes( rowCursor.getMetaData());
        metadataReady = true;
      }
      
      while( rowCursor.next())
      {
        IModelObject rowElement = transform.importRow( rowCursor);
        prototype.addChild( rowElement);
      }
    }
    catch( SQLException e)
    {
      String message = String.format( "Unable to sync reference with query, '%s'", query);
      throw new CachingException( message, e);
    }
    
    update( reference, prototype);
  }
  
  private ISQLProvider provider;
  private String query;
  private SimpleSQLParser parser;
  private boolean update;
  private boolean shallow;
  private SQLColumnMetaData metadata;
  private boolean metadataReady;
  private DefaultSQLRowTransform transform;
}
