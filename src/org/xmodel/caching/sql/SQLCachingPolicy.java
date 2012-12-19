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
import org.xmodel.external.ITransaction;
import org.xmodel.xaction.Conventions;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An ICachingPolicy that uses an arbitrary SQL query statement to load data.  The caching policy can also generate
 * statements for inserting, updating and deleting elements when the <i>update</i> flag is true.  By default the 
 * following transformation rules apply:
 * <ul>
 * <li>Each table row is transformed into a child element of the table element.</li>
 * <li>Table columns are stored in child elements of the row element with the following exceptions.</li>
 * <li>Table columns listed in <i>indexes</i> are stored in static attributes of the row element.</li>
 * <li>Table columns listed in <i>attributes</i> are stored in non-static attributes of the row element.</li>
 * <li>The names of row element attributes and children are the same as the table column name or alias.</li>
 * </ul>
 * 
 * <h3>Two-Layer Caching</h3>
 * When the <i>shallow</i> flag evaluates true, the indexed columns of the table (columns appearing in the 
 * <i>indexes</i> configuration element) are loaded into row elements
 * that have a second layer of caching.  Row elements can be queried by the static attributes that store
 * the indexed columns and individual rows can be selected by xpath without loading all the rows addressed
 * by the SQL query.
 *
 * <h3>Database Updates</h3>
 * When the <i>update</i> flag evaluates true, changes to the data-model will make corresponding changes to the
 * database.  The following table summarizes the types of database updates:
 * <ul>
 * <li>Inserting a row element will cause a row to be added to the database table.</li>
 * <li>Deleting a row element will cause a row to be deleted from the database table.</li>
 * <li>Updating any column of a row element will cause that column to be updated in the database.</li>
 * <li>Any changes to the data-model of an XML column will cause that column to be updated in the database.</li>
 * <li>Caching policy transactions allow multiple updates to be committed together.</li>
 * <li>Insert and delete statements appearing in caching policy transactions are optimized by batch updating.</li>
 * </ul> 
 * 
 * <h3>Reference</h3>
 * <ul>
 * <li>provider - An optional expression that identifies the implementation of ISQLProvider.</li>
 * <li>query - A string expression that gives the query to be executed.</li>
 * <li>indexes - A string expression giving comma-separated list of indexed columns where the first index is primary key.</li>
 * <li>attributes - An optional string expression giving a comma-separated list of column names to store in attributes.</li>
 * <li>shallow - A boolean expression that specifies whether row content is loaded on demand (default: false()).</li>
 * <li>update - A boolean expression that specifies whether the database is updated when the model is changed (default: false()).</li>
 * </ul>
 * 
 * <h3>Example:</h3>
 * <pre>
 * &lt;create var="table"&gt;
 *   &lt;table&gt;
 *     &lt;extern:cache class="org.xmodel.caching.sql.SQLCachingPolicy"&gt;
 *       &lt;provider&gt;$provider&lt;/provider&gt;
 *       &lt;query&gt;"SELECT id, name, age FROM employee"&lt;query&gt;
 *       &lt;indexes&gt;"id"&lt;indexes&gt;
 *     &lt;/extern:cache&gt;
 *   &lt;/table&gt;
 * &lt;create&gt;
 * </pre>
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
    
    this.rowCachingPolicy = new SQLRowCachingPolicy( this, getCache());
    this.updateListener = new UpdateListener();
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
      
      statement.close();
    }
    catch( SQLException e)
    {
      String message = String.format( "Unable to sync reference with query, '%s'", query);
      throw new CachingException( message, e);
    }
    
    update( reference, prototype);
    
    if ( update) updateListener.install( reference);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.AbstractCachingPolicy#transaction()
   */
  @Override
  public ITransaction transaction()
  {
    if ( transaction != null) return transaction;
    transaction = new SQLTransaction( this);
    return transaction;
  }

  /**
   * Called by SQLCachingPolicyTransaction when the transaction is complete.
   */
  protected void transactionComplete()
  {
    transaction = null;
  }
  
  /**
   * @return Returns the UpdateListener instance.
   */
  protected UpdateListener getUpdateListener()
  {
    return updateListener;
  }
  
  private ISQLProvider provider;
  private String query;
  private SimpleSQLParser parser;
  private boolean update;
  private boolean shallow;
  private SQLColumnMetaData metadata;
  private boolean metadataReady;
  private SQLRowCachingPolicy rowCachingPolicy;
  private DefaultSQLRowTransform transform;
  private ITransaction transaction;
  protected UpdateListener updateListener;
}
