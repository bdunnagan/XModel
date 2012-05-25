/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SQLDirectCachingPolicy.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.external.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.external.UnboundedCache;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A caching policy for accessing information from an SQL database. 
 * This caching policy is used to load both rows and columns of a table.
 */
public class SQLTableCachingPolicy extends ConfiguredCachingPolicy
{
  public SQLTableCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  /**
   * Create the caching policy with the specified cache.
   * @param cache The cache.
   */
  public SQLTableCachingPolicy( ICache cache)
  {
    super( cache);
    
    rowCachingPolicy = new SQLRowCachingPolicy( cache);
    rowCachingPolicy.setParent( this);
    
    updateMonitor = new SQLEntityListener();
    
    rowInserts = new HashMap<IModelObject, List<IModelObject>>();
    rowDeletes = new HashMap<IModelObject, List<IModelObject>>();
    rowUpdates = new HashMap<IModelObject, List<String>>();
    
    if ( providers == null)
    {
      providers = new HashMap<String, Class<? extends ISQLProvider>>();
      providers.put( "mysql", MySQLProvider.class);
    }
    
    Log.getLog( this).setLevel( Log.all);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    // create SQLManager
    provider = getProvider( annotation);
    
    factory = new ModelObjectFactory();
    tableName = Xlate.childGet( annotation, "table", (String)null);
    rowElementName = Xlate.childGet( annotation, "row", annotation.getParent().getType());
    
    // add second stage
    IExpression stageExpr = XPath.createExpression( rowElementName);
    defineNextStage( stageExpr, rowCachingPolicy, true);
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
   * Notify this caching policy that the specified transaction has completed.
   * @param transaction The transaction.
   */
  protected void transactionComplete( ITransaction transaction)
  {
    transaction = null;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    SLog.debugf( this, "sync: %s", reference);
    
    // get table meta-data
    fetchMetadata();
    
    // configure static attributes of SQLRowCachingPolicy
    rowCachingPolicy.addStaticAttribute( primaryKey);
    for( String otherKey: otherKeys) rowCachingPolicy.addStaticAttribute( otherKey);
    
    // sync
    syncTable( reference);
    
    // install update monitor
    updateMonitor.install( reference);
  }
  
  /**
   * Sync a table reference.
   * @param reference The table reference.
   */
  protected void syncTable( IExternalReference reference) throws CachingException
  {
    PreparedStatement statement = null;
    try
    {
      // get row stubs
      statement = createTableSelectStatement( reference);
      ResultSet result = statement.executeQuery();

      IModelObject parent = reference.cloneObject();
      while( result.next())
      {
        IModelObject stub = factory.createObject( reference, rowElementName);
        stub.setID( result.getString( 1));
        
        for( int i=0; i<otherKeys.size(); i++) 
          stub.setAttribute( otherKeys.get( i), result.getObject( i+2));
        
        parent.addChild( stub);
      }
      
      // update reference
      update( reference, parent);
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to cache reference: "+reference, e);
    }
    finally
    {
      if ( statement != null) close( statement);
    }
  }
  
  /**
   * Create the row element corresponding to the specified unsynced referenced.
   * @param reference The reference which is in the process of being synced.
   * @return Returns the prototype row element.
   */
  protected IModelObject createRowPrototype( IExternalReference reference) throws CachingException
  {
    PreparedStatement statement = null;
    try
    {
      IModelObject object = factory.createObject( reference.getParent(), rowElementName);
      ModelAlgorithms.copyAttributes( reference, object);

      statement = createRowSelectStatement( reference);
      ResultSet result = statement.executeQuery();
      if ( result.next())
      {      
        for( int i=0; i<columnNames.size(); i++)
        {
          String columnName = columnNames.get( i);
          if ( columnName.equals( primaryKey)) continue;

          Object value = result.getObject( i+1);
          if ( otherKeys.contains( columnName))
          {
            object.setAttribute( columnName, value);
          }
          else
          {
            IModelObject field = getFactory().createObject( object, columnName);
            field.setValue( value);
            object.addChild( field);
          }
        }        
      }

      return object;
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to cache reference: "+reference, e);
    }
    finally
    {
      if ( statement != null) close( statement);
    }
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    IExternalReference reference = rowCachingPolicy.createExternalTree( object, true, parent);
    parent.addChild( reference);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    if ( object.getParent() == parent) object.removeFromParent();
  }
  
  /**
   * Returns the SQLManager for the specified reference.
   * @param annotation The caching policy annotation.
   * @return Returns the SQLManager for the specified reference.
   */
  private static ISQLProvider getProvider( IModelObject annotation) throws CachingException
  {
    String providerName = Xlate.childGet( annotation, "provider", (String)null);
    if ( providerName == null) throw new CachingException( "SQL database provider not defined.");
    
    Class<? extends ISQLProvider> providerClass = providers.get( providerName);
    if ( providerClass == null) throw new CachingException( String.format( "Provider %s not supported.", providerName));

    try
    {
      ISQLProvider provider = providerClass.newInstance();
      provider.configure( annotation);
      return provider;
    }
    catch( Exception e)
    {
      throw new CachingException( e.getMessage());
    }
  }
  
  /**
   * Close the specified PreparedStatement and release its Connection instance.
   * @param statement The statement.
   */
  private void close( PreparedStatement statement)
  {
    try
    {
      provider.releaseConnection( statement.getConnection());
      statement.close();
    }
    catch( SQLException e)
    {
      SLog.exception( this, e);
    }
  }

  /**
   * Fetch the meta-data for the table.
   */
  private void fetchMetadata()
  {
    try
    {
      Connection connection = provider.leaseConnection();
      DatabaseMetaData meta = connection.getMetaData();
      ResultSet result = meta.getColumns( null, null, tableName, null);
      columnNames = new ArrayList<String>();
      while( result.next()) 
      {
        String columnName = result.getString( "COLUMN_NAME");
        columnNames.add( columnName.toLowerCase());
      }
      
      result = meta.getPrimaryKeys( null, null, tableName);
      while( result.next())
      {
        String name = result.getString( "COLUMN_NAME");
        
        if ( primaryKey != null) 
        {
          throw new CachingException( String.format(
              "Composite primary keys not supported in table, %s", tableName));
        }
        
        primaryKey = name.toLowerCase();
      }
      
      otherKeys = new ArrayList<String>( 1);
      result = meta.getIndexInfo( null, null, tableName, false, false);
      while( result.next())
      {
        String columnName = result.getString( "COLUMN_NAME");
        if ( columnName != null) otherKeys.add( columnName.toLowerCase());
      }
      
      provider.releaseConnection( connection);
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to get column names for table: "+tableName, e);
    }
  }
  
  /**
   * Returns a prepared statement which will select stubs for all rows of a table.
   * @param reference The reference representing a table.
   * @param nodes The row stubs to be populated.
   * @return Returns a prepared statement which will select stubs for all rows of a table.
   */
  private PreparedStatement createTableSelectStatement( IExternalReference reference) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "SELECT "); 
    sb.append( primaryKey);
    
    for( String otherKey: otherKeys)
    {
      sb.append( ",");
      sb.append( otherKey);
    }
    
    sb.append( " FROM "); sb.append( tableName);
    
    Connection connection = provider.leaseConnection();
    return connection.prepareStatement( sb.toString());
  }
  
  /**
   * Returns a prepared statement which will select one or more rows from a table.
   * @param reference The reference representing a table row.
   * @return Returns a prepared statement which will select one or more nodes.
   */
  private PreparedStatement createRowSelectStatement( IExternalReference reference) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "SELECT * "); sb.append( "FROM "); sb.append( tableName);
    sb.append( " WHERE "); sb.append( primaryKey); sb.append( "=?");
    
    Connection connection = provider.leaseConnection();
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    statement.setString( 1, reference.getID());
    return statement;
  }
  
  /**
   * Returns a prepared statement which will insert one or more rows. If a field of a row is a BLOB
   * then the field may contain an InputStream.  In this case, the field must also have the <i>length</i>
   * attribute set to the length of the stream (because JDBC requires it for some reason).
   * @param connection The database connection.
   * @param object The object representing a table.
   * @param nodes The rows to be inserted in the table.
   * @return Returns a prepared statement which will insert one or more nodes.
   */
  private PreparedStatement createInsertStatement( Connection connection, IModelObject object, List<IModelObject> nodes) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "INSERT INTO "); sb.append( tableName);
    sb.append( " VALUES");
    sb.append( "(?");
    for( int i=1; i<columnNames.size(); i++) sb.append( ",?");
    sb.append( ")");

    PreparedStatement statement = connection.prepareStatement( sb.toString());
    
    for( IModelObject node: nodes)
    {
      statement.setString( 1, node.getID());
      for( int i=0, j=0; i<columnNames.size(); i++)
      {
        if ( columnNames.get( i).equals( primaryKey)) continue;
        if ( otherKeys.contains( columnNames.get( i))) continue;
        IModelObject fieldNode = node.getChild( j++);
        statement.setObject( i+1, fieldNode.getValue());
      }
      statement.addBatch();
    }
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will update one or more rows.
   * @param connection The database connection.
   * @param object The object representing a table row.
   * @param fields The fields of the row that were updated.
   * @return Returns a prepared statement which will update one or more rows.
   */
  private PreparedStatement createUpdateStatement( Connection connection, IModelObject object, List<String> fields) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "UPDATE "); sb.append( tableName);
    sb.append( " SET ");

    for( int i=0; i<fields.size(); i++)
    {
      if ( i > 0) sb.append( ",");
      sb.append( fields.get( i));
      sb.append( "=?");
    }
    
    sb.append(" WHERE ");
    sb.append( primaryKey);
    sb.append( "=?");
    
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    for( int i=0; i<fields.size(); i++)
    {
      String field = fields.get( i);
      Object value = otherKeys.contains( field)? object.getAttribute( field): object.getFirstChild( field).getValue();
      statement.setObject( i+1, value);
    }
    
    statement.setString( fields.size() + 1, object.getID());
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will delete one or more rows.
   * @param connection The database connection.
   * @param object The reference representing a table.
   * @param nodes The rows to be updated in the table.
   * @return Returns a prepared statement which will delete one or more rows.
   */
  private PreparedStatement createDeleteStatement( Connection connection, IModelObject object, List<IModelObject> nodes) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "DELETE FROM "); sb.append( tableName);
    sb.append( " WHERE "); sb.append( primaryKey);
    sb.append( "=?");

    PreparedStatement statement = connection.prepareStatement( sb.toString());
    
    for( IModelObject node: nodes)
    {
      statement.setString( 1, node.getID());
      statement.addBatch();
    }
    
    return statement;
  }
  
  /**
   * Returns true if the specified object is a table reference.
   * @param object The object.
   * @return Returns true if the specified object is a table reference.
   */
  protected boolean isTable( IModelObject object)
  {
    if ( !(object instanceof IExternalReference)) return false;
    
    IExternalReference reference = (IExternalReference)object;
    if ( reference.getCachingPolicy() instanceof SQLTableCachingPolicy)
    {
      IModelObject parent = object.getParent();
      if ( parent == null || !(parent instanceof IExternalReference)) return true;
    }

    return false;
  }
  
  /**
   * Set whether update monitoring is enabled.
   * @param enabled True if enabled.
   */
  protected void setUpdateMonitorEnabled( boolean enabled)
  {
    updateMonitor.setEnabled( enabled);
  }
  
  /**
   * Commit changes.
   */
  protected void commit()
  {
    Connection connection = provider.leaseConnection();
    try
    {
      commit( connection);
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to commit change to database entity.", e);
    }
    finally
    {
      provider.releaseConnection( connection);
    }
  }
  
  /**
   * Commit changes to the specified transaction Connection.
   * @param connection The Connection.
   */
  protected void commit( Connection connection) throws SQLException
  {
    for( Map.Entry<IModelObject, List<IModelObject>> entry: rowDeletes.entrySet())
    {
      PreparedStatement statement = createDeleteStatement( connection, entry.getKey(), entry.getValue());
      SLog.debugf( this, "Executing SQL: %s", statement);
      statement.execute();
    }
    
    rowDeletes.clear();
    
    for( Map.Entry<IModelObject, List<IModelObject>> entry: rowInserts.entrySet())
    {
      PreparedStatement statement = createInsertStatement( connection, entry.getKey(), entry.getValue());
      SLog.debugf( this, "Executing SQL: %s", statement);
      statement.execute();
    }
    
    rowInserts.clear();
    
    for( Map.Entry<IModelObject, List<String>> entry: rowUpdates.entrySet())
    {
      PreparedStatement statement = createUpdateStatement( connection, entry.getKey(), entry.getValue());
      SLog.debugf( this, "Executing SQL: %s", statement);
      statement.execute();
    }
    
    rowUpdates.clear();
  }
  
  /**
   * @return Returns the instance of ISQLProvider.
   */
  protected ISQLProvider getSQLProvider()
  {
    return provider;
  }
  
  /**
   * A listener that monitors changes to the table data-model.
   */
  class SQLEntityListener extends NonSyncingListener
  {
    /**
     * Enable or disable notifications from this listener.
     * @param enabled True to enable.
     */
    public void setEnabled( boolean enabled)
    {
      this.enabled = enabled;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyAddChild( parent, child, index);
      
      if ( enabled)
      {
        if ( isTable( parent))
        {
          List<IModelObject> inserts = rowInserts.get( parent);
          if ( inserts == null)
          {
            inserts = new ArrayList<IModelObject>();
            rowInserts.put( parent, inserts);
          }
          inserts.add( child);
        }
        else
        {
          throw new CachingException( String.format( 
              "Illegal field insert operation on SQLDirectCachingPolicy external reference: %s, field: %s",
              ModelAlgorithms.createIdentityPath( parent), child.getType()));
        }
  
        if ( transaction == null) commit();
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      
      if ( enabled)
      {
        if ( isTable( parent))
        {
          List<IModelObject> deletes = rowDeletes.get( parent);
          if ( deletes == null)
          {
            deletes = new ArrayList<IModelObject>();
            rowDeletes.put( parent, deletes);
          }
          deletes.add( child);
          
          // remove any cached update records for removed row
          rowUpdates.remove( child);
        }
        else
        {
          throw new CachingException( String.format( 
              "Illegal field delete operation on SQLDirectCachingPolicy external reference: %s, field: %s",
              ModelAlgorithms.createIdentityPath( parent), child.getType()));
        }
        
        if ( transaction == null) commit();
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      super.notifyChange( object, attrName, newValue, oldValue);
      
      if ( enabled)
      {
        if ( isTable( object)) return;
        
        if ( isTable( object.getParent()))
        {
          // row
          List<String> updates = rowUpdates.get( object);
          if ( updates == null)
          {
            updates = new ArrayList<String>();
            rowUpdates.put( object, updates);
          }
          updates.add( attrName);
        }
        else if ( attrName.equals( ""))
        {
          // field
          List<String> updates = rowUpdates.get( object.getParent());
          if ( updates == null)
          {
            updates = new ArrayList<String>();
            rowUpdates.put( object.getParent(), updates);
          }
          updates.add( object.getType());
        }
        else
        {
          SLog.warnf( this, "Attribute, %s, of table row field, %s, was updated.", attrName, object.getType());
        }
        
        if ( transaction == null) commit();
      }
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyClear(org.xmodel.IModelObject, java.lang.String, java.lang.Object)
     */
    @Override
    public void notifyClear( IModelObject object, String attrName, Object oldValue)
    {
      super.notifyClear( object, attrName, oldValue);
      notifyChange( object, attrName, null, null);
    }
    
    private boolean enabled;
  }
    
  private static Map<String, Class<? extends ISQLProvider>> providers;

  private ISQLProvider provider;
  private IModelObjectFactory factory;
  private SQLRowCachingPolicy rowCachingPolicy;
  private String tableName;
  private List<String> columnNames;
  private String primaryKey;
  private List<String> otherKeys;
  private String rowElementName;
  private SQLEntityListener updateMonitor;
  private SQLTransaction transaction;
  private Map<IModelObject, List<IModelObject>> rowInserts;
  private Map<IModelObject, List<IModelObject>> rowDeletes;
  private Map<IModelObject, List<String>> rowUpdates;  
}

