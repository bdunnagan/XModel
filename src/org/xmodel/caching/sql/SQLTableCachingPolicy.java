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
package org.xmodel.caching.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.transform.SQLPredicateParser;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.MultiByteArrayInputStream;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.ZipCompressor;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.external.NonSyncingListener;
import org.xmodel.external.UnboundedCache;
import org.xmodel.log.Log;
import org.xmodel.log.SLog;
import org.xmodel.util.ThreadLocalMap;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A caching policy for accessing information from an SQL database. 
 * This caching policy is used to load both rows and columns of a table.
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
   
    rowCachingPolicy = new SQLRowCachingPolicy( this, cache);
    updateMonitor = new SQLEntityListener();
    
    rowInserts = new HashMap<IModelObject, List<IModelObject>>();
    rowDeletes = new HashMap<IModelObject, List<IModelObject>>();
    rowUpdates = new HashMap<IModelObject, List<String>>();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    // create SQLManager
    provider = getProvider( context, annotation);
    
    tableName = Xlate.childGet( annotation, "table", (String)null);
    rowElementName = Xlate.childGet( annotation, "row", tableName);
    stub = Xlate.childGet( annotation, "stub", true);
    readonly = Xlate.childGet( annotation, "readonly", false);
    
    attributes = new ArrayList<String>( 3);
    for( IModelObject element: annotation.getChildren( "attribute"))
      attributes.add( Xlate.get( element, ""));
    
    excluded = new ArrayList<String>( 3);
    for( IModelObject element: annotation.getChildren( "exclude"))
      excluded.add( Xlate.get( element, ""));
    
    IExpression whereExpr = Xlate.childGet( annotation, "where", (IExpression)null);
    if ( whereExpr != null) where = whereExpr.evaluateString( context);
    
    IExpression orderbyExpr = Xlate.childGet( annotation, "orderby", (IExpression)null);
    if ( orderbyExpr != null) orderby = orderbyExpr.evaluateString( context);
    
    IExpression offsetExpr = Xlate.childGet( annotation, "offset", (IExpression)null);
    offset = (offsetExpr != null)? (int)offsetExpr.evaluateNumber( context): -1;
    
    IExpression limitExpr = Xlate.childGet( annotation, "limit", (IExpression)null);
    limit = (limitExpr != null)? (int)limitExpr.evaluateNumber( context): -1;
    
    xmlColumns = new HashSet<String>( 1);
    for( IModelObject column: annotation.getChildren( "xml"))
      xmlColumns.add( Xlate.get( column, (String)null));
    
    // add second stage
    IExpression stageExpr = XPath.createExpression( rowElementName);
    defineNextStage( stageExpr, rowCachingPolicy, stub);
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
    if ( transaction != this.transaction) throw new IllegalArgumentException();
    this.transaction = null;
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
    for( String primaryKey: primaryKeys) rowCachingPolicy.addStaticAttribute( primaryKey);
    for( String otherKey: otherKeys) rowCachingPolicy.addStaticAttribute( otherKey);
    
    // sync
    syncTable( reference);
    
    // install update monitor
    if ( !readonly) updateMonitor.install( reference);
  }
  
  /**
   * Sync a table reference.
   * @param reference The table reference.
   */
  protected void syncTable( IExternalReference reference) throws CachingException
  {
    if ( limit == 0) return;
    
    PreparedStatement statement = null;
    try
    {
      log.debugf( "sync %s ...", reference.getType());
      
      long t0 = System.nanoTime();
      statement = createTableSelectStatement( reference);
      
      long t1 = System.nanoTime();
      log.debugf( "query prep: %1.3fs", ((t1 - t0) / 1e9));
      
      long t2 = System.nanoTime();
      ResultSet result = statement.executeQuery();
      
      long t3 = System.nanoTime();
      log.debugf( "query exec: %1.3fs", ((t3 - t2) / 1e9));
      
      long count = 0;
      IModelObject parent = reference.cloneObject();
      while( result.next())
      {
        count++;
        
        IModelObject row = getFactory().createObject( reference, rowElementName);
        if ( stub)
        {
          int k=1;
          for( String primaryKey: primaryKeys) row.setAttribute( primaryKey, result.getObject( k++));
          for( String otherKey: otherKeys) row.setAttribute( otherKey, result.getObject( k++));
        }
        else
        {
          populateRowElement( result, row);
        }
        
        parent.addChild( row);
      }
      
      log.debugf( "query size: %d", count);
      
      setUpdateMonitorEnabled( false);
      update( reference, parent);
      setUpdateMonitorEnabled( true);
    }
    catch( SQLException e)
    {
      String message = String.format( "Unable to cache reference, %s[%s], with statement, %s",
        reference.getType(),
        reference.getAttribute( "id"),
        statement);    
          
      throw new CachingException( message, e);
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
      IModelObject object = getFactory().createObject( reference.getParent(), rowElementName);
      ModelAlgorithms.copyAttributes( reference, object);

      statement = createRowSelectStatement( reference);
      log.debugf( "create %s from %s", reference.getType(), statement);
      
      ResultSet result = statement.executeQuery();
      if ( result.next()) populateRowElement( result, object);

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
  
  /**
   * Populate a row element from the current row in the specified result set.
   * @param result The result set.
   * @param object The row element to be populated.
   */
  protected void populateRowElement( ResultSet result, IModelObject object) throws SQLException
  {
    for( int i=0; i<columnNames.size(); i++)
    {
      String columnName = columnNames.get( i);

      Object value = result.getObject( i+1);
      if ( primaryKeys.contains( columnName) || otherKeys.contains( columnName) || attributes.contains( columnName))
      {
        object.setAttribute( columnName, value);
      }
      else
      {
        importColumn( object, columnName, value);
      }
    }        
  }

  /**
   * Import a database column value into the model.
   * @param row The row element.
   * @param column The column name.
   * @param value The database column value.
   */
  private void importColumn( IModelObject row, String column, Object value)
  {
    if ( otherKeys.contains( column) || attributes.contains( column))
    {
      row.setAttribute( column, value);
    }
    else if ( xmlColumns.contains( column))
    {
      row.getCreateChild( column);
      
      if ( value != null)
      {
        //
        // XML columns can contain multiple root nodes, so parser must be tricked by wrapping the XML
        // text with a dummy root node.
        //
        if ( value instanceof byte[])
        {
          if ( compressor == null) compressor = new ZipCompressor( new TabularCompressor( false, readonly));
          try
          {
            IModelObject superroot = compressor.decompress( new ByteArrayInputStream( (byte[])value));
            ModelAlgorithms.copyAttributes( superroot, row.getFirstChild( column));
            ModelAlgorithms.moveChildren( superroot, row.getFirstChild( column));
          }
          catch( Exception e)
          {
            SLog.exception( this, e);
          }
        }
        else
        {
          try
          {
            String xml = "<superroot>"+value.toString()+"</superroot>";
            if ( xml.length() > 0)
            {
              IModelObject superroot = new XmlIO().read( xml);
              ModelAlgorithms.moveChildren( superroot, row.getFirstChild( column));
            }
          }
          catch( XmlException e)
          {
            SLog.errorf( this, "Invalid xml in %s.%s", tableName, column, e);
          }
        }
      }
    }
    else
    {
      row.getCreateChild( column).setValue( value);
    }
  }

  /**
   * Export the content of a column.
   * @param row The row reference.
   * @param column The column name.
   * @param columnType The column type.
   * @return Returns the exported value.
   */
  private Object exportColumn( IModelObject row, String column, int type)
  {
    boolean wasUpdateEnabled = updateMonitor.getEnabled();
    try
    {
      // ignore updates generated by compressor caching policy syncing
      updateMonitor.setEnabled( false);
      
      if ( xmlColumns.contains( column))
      {
        if ( compressor == null && type == Types.BLOB || type == Types.BINARY || type == Types.VARBINARY || type == Types.LONGVARBINARY)
          compressor = new ZipCompressor( new TabularCompressor());
        
        if ( compressor != null)
        {
          try
          {
            IModelObject element = row.getFirstChild( column);
            if ( element == null) return null;
            
            List<byte[]> buffers = compressor.compress( element);
            return new MultiByteArrayInputStream( buffers);
          }
          catch( IOException e)
          {
            SLog.exception( this, e);
            return null;
          }
        }
        else
        {
          StringBuilder sb = new StringBuilder();
          for( IModelObject child: row.getFirstChild( column).getChildren())
            sb.append( XmlIO.write( Style.compact, child));
          return sb.toString();
        }
      }
      else
      {
        if ( otherKeys.contains( column) || attributes.contains( column)) 
        {
          return row.getAttribute( column);
        }
        else
        {
          IModelObject columnNode = row.getFirstChild( column);
          return (columnNode != null)? columnNode.getValue(): null;
        }
      }
    }
    finally
    {
      updateMonitor.setEnabled( wasUpdateEnabled);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    IExternalReference reference = rowCachingPolicy.createExternalTree( object, dirty, parent);
    parent.addChild( reference, index);
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
   * @param context The configuration context.
   * @param annotation The caching policy annotation.
   * @return Returns the SQLManager for the specified reference.
   */
  private static ISQLProvider getProvider( IContext context, IModelObject annotation) throws CachingException
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
  
  /**
   * Close the specified PreparedStatement and release its Connection instance.
   * @param statement The statement.
   */
  private void close( PreparedStatement statement)
  {
    try
    {
      provider.releaseConnection( statement.getConnection());
      provider.close( statement);
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
    Connection connection = null;
    try
    {
      Metadata metadata = metadataCache.get( tableName);
      if ( metadata == null)
      {
        metadata = new Metadata();
        
        connection = provider.leaseConnection();
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet result = meta.getColumns( null, null, tableName, null);
        while( result.next()) 
        {
          String columnName = result.getString( "COLUMN_NAME").toLowerCase();
          metadata.columnNames.add( columnName.toLowerCase());
          
          int columnType = result.getInt( "DATA_TYPE");
          metadata.columnTypes.add( columnType);
        }
        
        result = meta.getPrimaryKeys( null, null, tableName);
        while( result.next())
        {
          String name = result.getString( "COLUMN_NAME");
          if ( excluded.contains( name)) 
          {
            throw new IllegalArgumentException( String.format(
              "Primary key columns cannot be excluded."));
          }
          
          metadata.primaryKeys.add( name.toLowerCase());
        }
        
        // views do not provide meta-data that reflects the backing tables
        if ( metadata.primaryKeys.size() == 0) 
        {
          if ( attributes.size() == 0) throw new IllegalStateException( "Primary key or attribute must be defined.");
          metadata.primaryKeys.add( attributes.get( 0));
        }
        
        result = meta.getIndexInfo( null, null, tableName, false, false);
        while( result.next())
        {
          String columnName = result.getString( "COLUMN_NAME");
          if ( columnName != null) 
            metadata.otherKeys.add( columnName.toLowerCase());
        }
      
        metadataCache.put( tableName, metadata);
      }
      
      columnNames = new ArrayList<String>( metadata.columnNames);
      columnTypes = new ArrayList<Integer>( metadata.columnTypes);
      primaryKeys = new ArrayList<String>( metadata.primaryKeys);
      otherKeys = new ArrayList<String>( metadata.otherKeys);

      // remove excluded element columns
      for( int i=0; i<columnNames.size(); i++)
      {
        if ( excluded.contains( columnNames.get( i)))
        {
          columnNames.remove( i);
          columnTypes.remove(  i--);
        }
      }
      
      // remove excluded attribute columns
      for( int i=0; i<otherKeys.size(); i++)
      {
        if ( excluded.contains( otherKeys.get( i)))
        {
          otherKeys.remove( i--);
        }
      }
      
      // create query columns
      StringBuilder sb = new StringBuilder();
      sb.append( metadata.columnNames.get( 0));
      for( int i=1; i<metadata.columnNames.size(); i++)
      {
        sb.append( ",");
        sb.append( metadata.columnNames.get( i));
      }
      queryColumns = sb.toString();
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to get column names for table: "+tableName, e);
    }
    finally
    {
      if ( connection != null)
        provider.releaseConnection( connection);
    }
  }
  
  /**
   * Returns a prepared statement which will select stubs for all rows of a table.
   * @param reference The reference representing a table.
   * @param nodes The row stubs to be populated.
   * @return Returns a prepared statement which will select stubs for all rows of a table.
   */
  protected PreparedStatement createTableSelectStatement( IExternalReference reference) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "SELECT "); 

    if ( stub)
    {
      for( String primaryKey: primaryKeys)
      {
        sb.append( primaryKey);
        sb.append( ",");
      }
      
      for( String otherKey: otherKeys)
      {
        sb.append( otherKey);
        sb.append( ",");
      }
      
      sb.setLength( sb.length() - 1);
    }
    else
    {
      sb.append( queryColumns);
    }
    
    sb.append( " FROM "); 
    sb.append( tableName);
    
    // optional configured predicate
    List<String> params = null;
    if ( where != null)
    {
      sb.append( " WHERE ");
      
      SQLPredicateParser parser = new SQLPredicateParser();
      parser.parse( where);
     
      sb.append( parser.getParameterizedPredicate());
      params = parser.getParameters();
      
      log.debugf( "Parameterized predicate: %s", parser.getParameterizedPredicate());
    }
    
    // optional ordering
    if ( orderby != null)
    {
      sb.append( " ORDER BY ");
      sb.append( orderby);
    }
    
    Connection connection = provider.leaseConnection();
//    connection.setCatalog( catalog);
    
    log.debugf( "table query: %s", sb);
    
    PreparedStatement statement = provider.createStatement( connection, sb.toString(), limit, offset, false, true);
    if ( limit > 0) statement.setMaxRows( limit);
    
    if ( params != null)
    {
      for( int i=0; i<params.size(); i++)
      {
        String param = params.get( i);
        statement.setObject( i+1, param);
      }
    }
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will select one or more rows from a table.
   * @param reference The reference representing a table row.
   * @return Returns a prepared statement which will select one or more nodes.
   */
  private PreparedStatement createRowSelectStatement( IExternalReference reference) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append( "SELECT "); 
    sb.append( queryColumns); sb.append( " FROM "); sb.append( tableName);
    
    sb.append( " WHERE "); 
    for( String primaryKey: primaryKeys) { sb.append( primaryKey); sb.append( "=? AND ");}
    sb.setLength( sb.length() - 5);
    
    Connection connection = provider.leaseConnection();
//    connection.setCatalog( catalog);
    
    log.debugf( "row query: %s", sb);
    
    PreparedStatement statement = provider.createStatement( connection, sb.toString(), 1, 0, false, readonly);
    int k=1;
    for( String primaryKey: primaryKeys)
    {
      Object keyValue = reference.getAttribute( primaryKey);
      statement.setObject( k++, keyValue);
    }
    return statement;
  }
  
  /**
   * Returns a prepared statement which will insert one or more rows. If a field of a row is a BLOB
   * then the field may contain an InputStream.  In this case, the field must also have the <i>length</i>
   * attribute set to the length of the stream (because JDBC requires it for some reason).
   * @param connection The database connection.
   * @param reference The reference representing a table.
   * @param nodes The rows to be inserted in the table.
   * @return Returns a prepared statement which will insert one or more nodes.
   */
  private PreparedStatement createInsertStatement( Connection connection, IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "INSERT INTO "); sb.append( tableName);
    sb.append( " SET");

    char sep = ' ';    
    for( int i=0; i<columnNames.size(); i++)
    {
      sb.append( sep); sep = ',';
      sb.append( columnNames.get( i));
      sb.append( "=?");
    }

    PreparedStatement statement = provider.createStatement( connection, sb.toString(), -1, -1, false, false);
    
    for( IModelObject node: nodes)
    {
      for( int i=0; i<columnNames.size(); i++)
      {
        Object value = exportColumn( node, columnNames.get( i), columnTypes.get( i));
        if ( value != null)
        {
          if ( value instanceof InputStream)
          {
            statement.setBinaryStream( i+1, (InputStream)value);
          }
          else
          {
            statement.setObject( i+1, value);
          }
        }
        else
        {
          statement.setNull( i+1, columnTypes.get( i));
        }
      }
      statement.addBatch();
    }
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will update one or more rows.
   * @param connection The database connection.
   * @param reference The reference representing a table row.
   * @param columns The columns of the row that were updated.
   * @return Returns a prepared statement which will update one or more rows.
   */
  private PreparedStatement createUpdateStatement( Connection connection, IModelObject reference, List<String> columns) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "UPDATE "); sb.append( tableName);
    sb.append( " SET ");

    for( int i=0; i<columns.size(); i++)
    {
      if ( i > 0) sb.append( ",");
      sb.append( columns.get( i));
      sb.append( "=?");
    }
    
    sb.append( " WHERE ");
    for( String primaryKey: primaryKeys) { sb.append( primaryKey); sb.append( "=? AND ");}
    sb.setLength( sb.length() - 5);
    
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    for( int i=0; i<columns.size(); i++)
    {
      String column = columns.get( i);
      
      int columnNameIndex = columnNames.indexOf( column);
      if ( columnNameIndex == -1)
        throw new IllegalArgumentException( 
            String.format( "Column '%s' is not defined on table '%s'!", column, tableName));
      
      Object value = exportColumn( reference, column, columnTypes.get( columnNameIndex));
      if ( value != null)
      {
        if ( value instanceof InputStream)
        {
          statement.setBinaryStream( i+1, (InputStream)value);
        }
        else
        {
          statement.setObject( i+1, value);
        }
      }
      else
      {
        int index = columnNames.indexOf( column);
        statement.setNull( i+1, columnTypes.get( index));
      }
    }
    
    int k = columns.size() + 1;
    for( String primaryKey: primaryKeys)
      statement.setObject( k++, reference.getAttribute( primaryKey));
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will delete one or more rows.
   * @param connection The database connection.
   * @param reference The reference representing a table.
   * @param nodes The rows to be updated in the table.
   * @return Returns a prepared statement which will delete one or more rows.
   */
  private PreparedStatement createDeleteStatement( Connection connection, IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append( "DELETE FROM "); sb.append( tableName);
    
    sb.append( " WHERE ");
    for( String primaryKey: primaryKeys) { sb.append( primaryKey); sb.append( "=? AND ");}
    sb.setLength( sb.length() - 5);

    PreparedStatement statement = connection.prepareStatement( sb.toString());
    
    int k=1;
    for( IModelObject node: nodes)
    {
      for( String primaryKey: primaryKeys)   
        statement.setObject( k++, node.getAttribute( primaryKey));
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
    if ( object == null || !(object instanceof IExternalReference)) return false;
    
    IExternalReference reference = (IExternalReference)object;
    if ( reference.getCachingPolicy() instanceof SQLTableCachingPolicy)
    {
      IModelObject parent = object.getParent();
      if ( parent == null || !(parent instanceof IExternalReference)) return true;
    }

    return false;
  }
  
  /**
   * Returns true if the specified object is a column object.
   * @param object The object.
   * @return Returns true if the specified object is a column object.
   */
  protected boolean isColumn( IModelObject object)
  {
    if ( object.getParent() == null) return false;
    return isTable( object.getParent().getParent());
  }
  
  /**
   * Returns the column element that is the ancestor, or self, of the specified object.
   * @param object An object in an xml-column.
   * @return Returns null or the column object.
   */
  protected IModelObject findColumnElement( IModelObject object)
  {
    IModelObject table = object;
    int depth = 0;
    while( table != null && !isTable( table))
    {
      depth++;
      table = table.getParent();
    }
    
    if ( table == null) return null;
    
    depth -= 2;
    IModelObject column = object;
    for( int i=0; i<depth; i++) column = column.getParent();
    
    return column;
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
    long t0 = System.nanoTime();
    
//    connection.setCatalog( catalog);
    
    for( Map.Entry<IModelObject, List<IModelObject>> entry: rowDeletes.entrySet())
    {
      PreparedStatement statement = createDeleteStatement( connection, (IExternalReference)entry.getKey(), entry.getValue());
      log.verbosef( "%s", statement);
      try
      {
        statement.executeBatch();
      }
      catch( SQLException e)
      {
        SLog.errorf( this, "Delete statement failed: %s", statement);
        throw e;
      }
      finally
      {
        provider.close( statement);
      }
    }
    
    rowDeletes.clear();
    
    for( Map.Entry<IModelObject, List<IModelObject>> entry: rowInserts.entrySet())
    {
      PreparedStatement statement = createInsertStatement( connection, (IExternalReference)entry.getKey(), entry.getValue());
      log.verbosef( "%s", statement);
      try
      {
        statement.executeBatch();
      }
      catch( SQLException e)
      {
        SLog.errorf( this, "Insert statement failed: %s", statement);
        throw e;
      }
      finally
      {
        provider.close( statement);
      }
    }
    
    rowInserts.clear();
    
    for( Map.Entry<IModelObject, List<String>> entry: rowUpdates.entrySet())
    {
      PreparedStatement statement = createUpdateStatement( connection, entry.getKey(), entry.getValue());
      log.verbosef( "%s", statement);
      try
      {
        statement.execute();
      }
      catch( SQLException e)
      {
        SLog.errorf( this, "Update statement failed: %s", statement);
        throw e;
      }
      finally
      {
        provider.close( statement);
      }
    }
    
    rowUpdates.clear();
    
    long t1 = System.nanoTime();
    log.verbosef( "Commit time, %1.3fms", ((t1 - t0)/1e6));
  }
  
  /**
   * @return Returns the instance of ISQLProvider.
   */
  protected ISQLProvider getSQLProvider()
  {
    return provider;
  }
  
  /**
   * Enable or disable the update monitoring listener.
   * @param enabled True if enabled.
   */
  protected void setUpdateMonitorEnabled( boolean enabled)
  {
    updateMonitor.setEnabled( enabled);
  }
  
  /**
   * Mark the specified column as having been updated.
   * @param row The row that was updated.
   * @param column The column that was updated.
   */
  private void addRowUpdate( IModelObject row, String column)
  {
    List<String> updates = rowUpdates.get( row);
    if ( updates == null)
    {
      updates = new ArrayList<String>();
      rowUpdates.put( row, updates);
    }
    updates.add( column);
  }
  
  /**
   * A listener that monitors changes to the table data-model.
   */
  private class SQLEntityListener extends NonSyncingListener
  {
    public SQLEntityListener()
    {
      enabled = true;
    }
    
    /**
     * Enable or disable notifications from this listener.
     * @param enabled True to enable.
     */
    public void setEnabled( boolean enabled)
    {
      this.enabled = enabled;
    }
    
    /**
     * @return Returns true if notifications are enabled.
     */
    public boolean getEnabled()
    {
      return enabled;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyAddChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyAddChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyAddChild( parent, child, index);
      
      if ( !enabled) return;
      
      // handle row insert
      if ( isTable( parent))
      {
        ICachingPolicy cachingPolicy = child.getCachingPolicy();
        if ( cachingPolicy != null)
        {
          ((SQLRowCachingPolicy)cachingPolicy).parent = SQLTableCachingPolicy.this;
        }
        else
        {
          cachingPolicy = new SQLRowCachingPolicy( SQLTableCachingPolicy.this, getCache());
          child.setCachingPolicy( cachingPolicy);
        }
        
        List<IModelObject> inserts = rowInserts.get( parent);
        if ( inserts == null)
        {
          inserts = new ArrayList<IModelObject>();
          rowInserts.put( parent, inserts);
        }
        inserts.add( child);
      }
      else if ( xmlColumns.contains( parent.getType()))
      {
          if ( !excluded.contains( parent.getType()))
          {
            // xml column insert
            addRowUpdate( parent.getParent(), parent.getType());
          }
        }
      else if ( isTable( parent.getParent()))
      {
        if ( !excluded.contains( child.getType()))
        {
           addRowUpdate( parent, child.getType());
        }
      }
      else
      {
        // xml column content update
        IModelObject columnAncestor = findColumnElement( parent);
        if ( columnAncestor != null)
        {
          addRowUpdate( columnAncestor.getParent(), columnAncestor.getType());
        }
        else
        {
          throw new CachingException( String.format( 
              "Illegal field insert operation on SQLDirectCachingPolicy external reference: %s, field: %s",
              ModelAlgorithms.createIdentityPath( parent), child.getType()));
        }
      }

      if ( transaction == null) commit();
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.NonSyncingListener#notifyRemoveChild(org.xmodel.IModelObject, org.xmodel.IModelObject, int)
     */
    @Override
    public void notifyRemoveChild( IModelObject parent, IModelObject child, int index)
    {
      super.notifyRemoveChild( parent, child, index);
      
      if ( !enabled) return;
      
      // handle row delete
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
        if ( rowUpdates != null) rowUpdates.remove( child);
      }
      else if ( xmlColumns.contains( parent.getType()))
      {
        if ( !excluded.contains( parent.getType()))
        {
          // xml column delete
          addRowUpdate( parent.getParent(), parent.getType());
        }
      }
      else if ( isTable( parent.getParent()))
      {
        if ( !excluded.contains( child.getType()))
        {
          addRowUpdate( parent, child.getType());
        }
      }
      else
      {
        // xml column content updated
        IModelObject columnAncestor = findColumnElement( parent);
        if ( columnAncestor != null)
        {
          addRowUpdate( columnAncestor.getParent(), columnAncestor.getType());
        }
        else
        {
          throw new CachingException( String.format( 
              "Illegal field delete operation on SQLDirectCachingPolicy external reference: %s, field: %s",
              ModelAlgorithms.createIdentityPath( parent), child.getType()));
        }
      }
      
      if ( transaction == null) commit();
    }

    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyChange(org.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
    {
      super.notifyChange( object, attrName, newValue, oldValue);
      
      if ( !enabled) return;
      
      // ignore changes to table attributes
      if ( isTable( object)) return;

      // handle indexed column update
      if ( isTable( object.getParent()))
      {
        if ( !excluded.contains( attrName))
        {
          if ( attrName.length() > 0)
          {
            // update to the value of a column
            addRowUpdate( object, attrName);
          }
          else
          {
            SLog.errorf( this, "Ignored update to value of table row in table, %s", object.getParent().getType());
          }
        }
      }
      
      // handle non-indexed column update
      else if ( isColumn( object))
      {
        if ( !excluded.contains( object.getType()))
        {
          if ( attrName.length() == 0)
          {
            addRowUpdate( object.getParent(), object.getType());
          }
          else
          {
            SLog.errorf( this, "Ignored update to attribute of column, %s, in table, %s", object.getType(), object.getParent().getType());
          }
        }
      }

      // handle xml column internal update
      else
      {
        IModelObject columnAncestor = findColumnElement( object);
        if ( columnAncestor != null)
        {
          addRowUpdate( columnAncestor.getParent(), columnAncestor.getType());
        }
        else
        {
          SLog.errorf( this, "Ignored internal update to non-xml column - configuration error?"); 
        }
      }
      
      if ( transaction == null) commit();
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
    
    /* (non-Javadoc)
     * @see org.xmodel.ModelListener#notifyDirty(org.xmodel.IModelObject, boolean)
     */
    @Override
    public void notifyDirty( IModelObject object, boolean dirty)
    {
      // override default behavior
    }

    private boolean enabled;
  }
  
  private static class Metadata
  {
    public List<String> columnNames = new ArrayList<String>();
    public List<Integer> columnTypes = new ArrayList<Integer>();
    public List<String> primaryKeys = new ArrayList<String>( 1);
    public List<String> otherKeys = new ArrayList<String>( 1);
  }
  
  private static Log log = Log.getLog( SQLTableCachingPolicy.class);
  private static ThreadLocalMap<String, Metadata> metadataCache = new ThreadLocalMap<String, Metadata>();

  protected ISQLProvider provider;
  protected boolean stub;
  protected boolean readonly;
  protected List<String> excluded;
  protected List<String> attributes;
  protected String where;
  protected String orderby;
  protected int offset;
  protected int limit;
  protected SQLRowCachingPolicy rowCachingPolicy;
  protected String tableName;
  protected List<String> columnNames;
  protected List<Integer> columnTypes;
  protected String queryColumns;
  protected List<String> primaryKeys;
  protected List<String> otherKeys;
  protected String rowElementName;
  protected Set<String> xmlColumns;
  protected SQLEntityListener updateMonitor;
  protected SQLTransaction transaction;
  protected Map<IModelObject, List<IModelObject>> rowInserts;
  protected Map<IModelObject, List<IModelObject>> rowDeletes;
  protected Map<IModelObject, List<String>> rowUpdates;  
  protected ICompressor compressor;
}

