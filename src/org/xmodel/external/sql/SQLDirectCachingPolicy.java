/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * A caching policy for accessing information from an SQL database. This caching policy is used to load both rows
 * and columns of a table.
 */
public class SQLDirectCachingPolicy extends ConfiguredCachingPolicy
{
  public SQLDirectCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  /**
   * Create the caching policy with the specified cache.
   * @param cache The cache.
   */
  public SQLDirectCachingPolicy( ICache cache)
  {
    super( cache);
    rowCachingPolicy = new SQLRowCachingPolicy( cache);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    factory = new ModelObjectFactory();
    
    // get parameters
    otherKeys = new ArrayList<String>( 1);
    for( IModelObject keyDecl: annotation.getChildren( "key"))
    {
      String keyName = Xlate.get( keyDecl, (String)null);
      if ( Xlate.get( keyDecl, "primary", false))
      {
        primaryKey = keyName;
      }
      else
      {
        otherKeys.add( keyName);
      }
    }
    
    // set static attributes
    List<String> staticAttributes = new ArrayList<String>();
    staticAttributes.add( "id");
    staticAttributes.addAll( otherKeys);
    setStaticAttributes( staticAttributes.toArray( new String[ 0]));

    // set element name for row elements
    child = Xlate.childGet( annotation, "rows", (String)null);
    
    // add second stage
    IExpression stageExpr = XPath.createExpression( child);
    defineNextStage( stageExpr, rowCachingPolicy, true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    syncTable( reference);
  }
  
  /**
   * Sync a table reference.
   * @param reference The table reference.
   */
  protected void syncTable( IExternalReference reference) throws CachingException
  {
    try
    {
      // get schema
      columns = getColumns( reference);
      
      // get row stubs
      PreparedStatement statement = createTableSelectStatement( reference);
      ResultSet result = statement.executeQuery();

      IModelObject parent = reference.cloneObject();
      while( result.next())
      {
        IModelObject stub = factory.createObject( reference, child);
        stub.setID( result.getString( 1));
        for( int i=0; i<otherKeys.size(); i++) 
          stub.setAttribute( otherKeys.get( i), result.getString( i+2));
        parent.addChild( stub);
      }
      
      // update reference
      update( reference, parent);
      
      // cleanup
      result.close();
      statement.close();
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to cache reference: "+reference, e);
    }
  }
  
  /**
   * Create the row element corresponding to the specified unsynced referenced.
   * @param reference The reference which is in the process of being synced.
   * @return Returns the prototype row element.
   */
  protected IModelObject createRowPrototype( IExternalReference reference) throws CachingException
  {
    try
    {
      IModelObject object = factory.createObject( reference.getParent(), child);
      ModelAlgorithms.copyAttributes( reference, object);

      PreparedStatement statement = createRowSelectStatement( reference);
      ResultSet result = statement.executeQuery();
      if ( result.next())
      {      
        for( int i=0; i<columns.length; i++)
        {
          if ( columns[ i].name.equals( primaryKey)) continue;
          
          if ( otherKeys.contains( columns[ i].name))
          {
            Object value = transformValue( reference, null, result, i);
            object.setAttribute( columns[ i].name, value);
          }
          else
          {
            IModelObject field = getFactory().createObject( object, columns[ i].name);
            Object value = transformValue( reference, field, result, i);
            field.setValue( value);
            object.addChild( field);
          }
        }        
      }
      
      result.close();
      statement.close();
      
      return object;
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to cache reference: "+reference, e);
    }
  }
    
  /**
   * Transform the value of a table row column into the value to be stored in the fragment.
   * TODO: This method is a work-in-progress.
   * @param rowElement The table row element.
   * @param columnElement The column element.
   * @param value The value of the column from the ResultSet.
   * @return Returns the value to be stored in the element.
   */
  private Object transformValue( IModelObject rowElement, IModelObject columnElement, ResultSet result, int column) throws SQLException
  {
    if ( columns[ column].type == Types.LONGVARBINARY)
    {
      if ( columnElement == null) 
        throw new CachingException(
          "Blobs cannot be mapped to attributes.");
      
      // create new statement to access blob later (current blob will be out-of-scope)
      PreparedStatement statement = createColumnSelectStatement( rowElement, columnElement);
      BlobAccess access = new BlobAccess( statement);
      return access;
    }
    else
    {
      return result.getObject( column+1);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Returns the SQLManager for the specified reference.
   * @param locus Either a table or row reference.
   * @return Returns the SQLManager for the specified reference.
   */
  @SuppressWarnings("unchecked")
  public static SQLManager getSQLManager( IModelObject locus) throws CachingException
  {
    IModelObject sqlManagerNode = sqlManagerExpr.queryFirst( locus);
    if ( sqlManagerNode == null) return null;
    
    SQLManager sqlManager = (SQLManager)sqlManagerNode.getAttribute( "instance");
    if ( sqlManager == null)
    {
      String className = Xlate.get( sqlManagerNode, "class", (String)null);
      if ( className == null) throw new CachingException( "SQLManager class attribute not defined: "+sqlManagerNode);
      
      try
      {
        Class clss = SQLDirectCachingPolicy.class.getClassLoader().loadClass( className);
        sqlManager = (SQLManager)clss.newInstance();
        sqlManager.configure( sqlManagerNode);
        sqlManagerNode.setAttribute( "instance", sqlManager);
      }
      catch( Exception e)
      {
        throw new CachingException( "Unable to create instance of SQLManager: "+sqlManagerNode, e);
      }
    }
    
    return sqlManager;
  }
   
  /**
   * Returns the names of the columns in the specified database table.
   * @param table The table reference.
   * @return Returns the names of the columns in the specified database table.
   */
  public Column[] getColumns( IExternalReference table) throws CachingException
  {
    try
    {
      Connection connection = getSQLManager( table).getConnection();
      DatabaseMetaData meta = connection.getMetaData();
      ResultSet result = meta.getColumns( null, null, Xlate.get( table, "table", ""), null);
      List<Column> columns = new ArrayList<Column>();
      while( result.next()) 
      {
        Column column = new Column();
        column.name = result.getString( "COLUMN_NAME");
        column.type = result.getInt( "DATA_TYPE");
        columns.add( column);
      }
      connection.close();
      return columns.toArray( new Column[ 0]);
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to get column names for table: "+table, e);
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
    String table = Xlate.get( reference, "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "SELECT "); 
    sb.append( primaryKey);
    
    for( String otherKey: otherKeys)
    {
      sb.append( ",");
      sb.append( otherKey);
    }
    
    sb.append( " FROM "); sb.append( table);
    
    SQLManager sqlManager = getSQLManager( reference);
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());

    return statement;
  }
  
  /**
   * Returns a prepared statement which will select one or more rows from a table.
   * @param reference The reference representing a table row.
   * @return Returns a prepared statement which will select one or more nodes.
   */
  private PreparedStatement createRowSelectStatement( IExternalReference reference) throws SQLException
  {
    String table = Xlate.get( reference.getParent(), "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "SELECT * "); sb.append( "FROM "); sb.append( table);
    sb.append( " WHERE "); sb.append( primaryKey); sb.append( "=?");
    
    SQLManager sqlManager = getSQLManager( reference.getParent());
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    statement.setString( 1, reference.getID());
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will select a table row column.
   * @param rowElement The element representing a table row.
   * @param columnElement The element representing a table row column.
   * @return Returns a prepared statement which will select the column.
   */
  private PreparedStatement createColumnSelectStatement( IModelObject rowElement, IModelObject columnElement) throws SQLException
  {
    String table = Xlate.get( rowElement.getParent(), "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "SELECT "); sb.append( columnElement.getType()); sb.append( " FROM "); sb.append( table);
    sb.append(" WHERE "); sb.append( primaryKey); sb.append( "=?");
    
    SQLManager sqlManager = getSQLManager( rowElement.getParent());
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    statement.setString( 1, rowElement.getID());
    
    return statement;
  }
  
  /**
   * Set a field of a prepared statement given the row element.
   * TODO: This method is a work-in-progress.
   * @param statement The statement.
   * @param row The row element.
   * @param index The field index (0 based).
   */
  protected void setField( PreparedStatement statement, int index, IModelObject row) throws CachingException, SQLException
  {
    String columnName = columns[ index].name;
    Object value = otherKeys.contains( columns[ index].name)?
      row.getAttribute( columnName):
      row.getFirstChild( columnName).getValue();

    if ( value == null)
    {
      statement.setNull( index+1, columns[ index].type);
    }
    else if ( value instanceof File)
    {
      try
      {
        File file = (File)value;
        FileInputStream stream = new FileInputStream( file);
        statement.setBinaryStream( index+1, stream, (int)file.length());
      }
      catch( IOException e)
      {
        throw new CachingException( "Unable to open file in table row: "+row+", file="+value, e);
      }
    }
    else if ( columns[ index].type == Types.DATE)
    {
      statement.setDate( index+1, new Date( Long.parseLong( value.toString())));
    }
    else if ( columns[ index].type == Types.TIMESTAMP)
    {
      statement.setDate( index+1, new Date( Long.parseLong( value.toString())));
    }
    else
    {
      statement.setObject( index+1, value);
    }
  }
  
  /**
   * Returns a prepared statement which will insert one or more rows. If a field of a row is a BLOB
   * then the field may contain an InputStream.  In this case, the field must also have the <i>length</i>
   * attribute set to the length of the stream (because JDBC requires it for some reason).
   * @param reference The reference representing a table.
   * @param nodes The rows to be inserted in the table.
   * @return Returns a prepared statement which will insert one or more nodes.
   */
  public PreparedStatement createInsertStatement( IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    String table = Xlate.get( reference, "table", (String)null);
    
    // get columns if not already
    if ( columns == null) columns = getColumns( reference);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "INSERT INTO "); sb.append( table);
    sb.append( " VALUES");
    for( int i=0; i<nodes.size(); i++)
    {
      if ( i > 0) sb.append( ",");
      sb.append( "(?");
      for( int j=1; j<columns.length; j++) sb.append( ",?");
      sb.append( ")");
    }

    SQLManager sqlManager = getSQLManager( reference);
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    
    for( IModelObject node: nodes)
    {
      statement.setString( 1, node.getID());
      for( int j=0; j<columns.length; j++)
      {
        if ( columns[ j].name.equals( primaryKey)) continue;
        setField( statement, j, node);
      }
      statement.addBatch();
    }
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will update one or more rows.
   * @param reference The reference representing a table.
   * @param nodes The rows to be updated in the table.
   * @return Returns a prepared statement which will update one or more rows.
   */
  public PreparedStatement createUpdateStatement( IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    String table = Xlate.get( reference, "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "UPDATE "); sb.append( table);
    sb.append( " SET ");

    for( int i=0; i<columns.length; i++)
    {
      if ( columns[ i].name.equals( primaryKey)) continue;
      if ( i>0) sb.append( ",");
      sb.append( columns[ i].name);
      sb.append( "=?");
    }
    
    sb.append(" WHERE ");
    sb.append( primaryKey);
    sb.append( "=?");
    
    SQLManager sqlManager = getSQLManager( reference);
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    
    for( IModelObject node: nodes)
    {
      statement.setString( 1, node.getID());
      for( int j=0; j<columns.length; j++)
      {
        if ( columns[ j].name.equals( primaryKey)) continue;
        setField( statement, j, node);
      }
      statement.addBatch();
    }
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will delete one or more rows.
   * @param reference The reference representing a table.
   * @param nodes The rows to be updated in the table.
   * @return Returns a prepared statement which will delete one or more rows.
   */
  public PreparedStatement createDeleteStatement( IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    String table = Xlate.get( reference, "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "DELETE FROM "); sb.append( table);
    sb.append( " WHERE "); sb.append( primaryKey);
    sb.append( "=?");

    SQLManager sqlManager = getSQLManager( reference);
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    
    for( IModelObject node: nodes)
    {
      statement.setString( 1, node.getID());
      statement.addBatch();
    }
    
    return statement;
  }

  private class Column
  {
    String name;
    int type;
    
    public String toString()
    {
      return name+":"+type;
    }
  }
  
  private class SQLRowCachingPolicy extends AbstractCachingPolicy
  {
    protected SQLRowCachingPolicy( ICache cache)
    {
      super( cache);
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
     */
    public void sync( IExternalReference reference) throws CachingException
    {
      IModelObject object = createRowPrototype( reference);
      update( reference, object);
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.ICachingPolicy#flush(org.xmodel.external.IExternalReference)
     */
    public void flush( IExternalReference reference) throws CachingException
    {
      throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.ICachingPolicy#insert(org.xmodel.external.IExternalReference, 
     * org.xmodel.IModelObject, boolean)
     */
    public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
    {
      throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
     * org.xmodel.IModelObject)
     */
    public void remove( IExternalReference parent, IModelObject object) throws CachingException
    {
      throw new UnsupportedOperationException();
    }
  } 
  
  private static IExpression sqlManagerExpr = XPath.createExpression( 
    "ancestor-or-self::*/meta:sqlmanager");
  
  private IModelObjectFactory factory;
  private SQLRowCachingPolicy rowCachingPolicy;
  private Column[] columns;
  private String primaryKey;
  private List<String> otherKeys;
  private String child;
}

