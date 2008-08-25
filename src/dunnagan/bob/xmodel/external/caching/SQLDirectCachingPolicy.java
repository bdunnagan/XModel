/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external.caching;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.ModelObject;
import dunnagan.bob.xmodel.ModelObjectFactory;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ConfiguredCachingPolicy;
import dunnagan.bob.xmodel.external.ICache;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.external.UnboundedCache;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

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
    setStaticAttributes( new String[] { "id", "meta:*"});
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#configure(dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    factory = new ModelObjectFactory();
    
    // get manager defined in annotation
    defaultManager = annotation.getFirstChild( "meta:sqlmanager");

    // get parameters
    key = Xlate.childGet( annotation, "key", (String)null);
    child = Xlate.childGet( annotation, "child", (String)null);
    
    // add second stage
    IExpression stageExpr = XPath.createExpression( child);
    defineNextStage( stageExpr, this, true);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ConfiguredCachingPolicy#syncImpl(dunnagan.bob.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    String table = Xlate.get( reference, "table", (String)null);
    if ( table != null)
    {
      try
      {
        PreparedStatement statement = createTableSelectStatement( reference);
        ResultSet result = statement.executeQuery();

        IModelObject parent = reference.cloneObject();
        while( result.next())
        {
          IModelObject stub = factory.createObject( reference, child);
          stub.setID( result.getString( 1));
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
    else
    {
      try
      {
        IModelObject object = factory.createObject( reference.getParent(), child);

        PreparedStatement statement = createRowSelectStatement( reference);
        ResultSet result = statement.executeQuery();
        String[] columns = getColumns( statement);        
        if ( result.next())
        {      
          for( int i=0; i<columns.length; i++)
          {
            if ( columns[ i].equals( key)) continue;
            IModelObject field = new ModelObject( columns[ i]);
            field.setValue( result.getObject( i+1));
            object.addChild( field);
          }        
        }
        
        // update reference
        update( reference, object);
        
        // cleanup
        result.close();
        statement.close();
      }
      catch( SQLException e)
      {
        throw new CachingException( "Unable to cache reference: "+reference, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#flush(dunnagan.bob.xmodel.external.IExternalReference)
   */
  public void flush( IExternalReference reference) throws CachingException
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#insert(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public void insert( IExternalReference parent, IModelObject object, int index, boolean dirty) throws CachingException
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.ICachingPolicy#remove(dunnagan.bob.xmodel.external.IExternalReference, 
   * dunnagan.bob.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
  }
  
  /**
   * Returns the SQLManager for the specified reference. If the manager annotation exists, but the
   * manager has not yet been initialized, then it will be initialized here.
   * @param locus Either a table or row reference.
   * @return Returns the SQLManager for the specified reference.
   */
  @SuppressWarnings("unchecked")
  private SQLManager getSQLManager( IModelObject locus) throws CachingException
  {
    IModelObject sqlManagerNode = defaultManager;
    if ( sqlManagerNode == null) sqlManagerNode = sqlManagerExpr.queryFirst( locus);
    SQLManager sqlManager = (SQLManager)sqlManagerNode.getAttribute( "instance");
    if ( sqlManager == null)
    {
      String className = Xlate.get( sqlManagerNode, "class", (String)null);
      if ( className == null) throw new CachingException( "SQLManager class attribute not defined: "+sqlManagerNode);
      
      try
      {
        Class clss = getClass().getClassLoader().loadClass( "dunnagan.bob.xmodel.external.caching."+className);
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
   * Returns the names of the columns.
   * @param statement The PreparedStatement from which the ResultMetaData will be obtained.
   * @return Returns the names of the columns.
   */
  public String[] getColumns( PreparedStatement statement) throws CachingException
  {
    if ( columns != null) return columns;

    try
    {
      ResultSetMetaData meta = statement.getMetaData();
      columns = new String[ meta.getColumnCount()];
      for( int i=0; i<columns.length; i++) columns[ i] = meta.getColumnName( i+1);
      return columns;
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to get column names from metadata.", e);
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
    sb.append( "SELECT "); sb.append( key); sb.append( " FROM "); sb.append( table);
    
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
    sb.append( "SELECT * "); sb.append( " FROM "); sb.append( table);
    sb.append(" WHERE "); sb.append( key); sb.append( "=?");
    
    SQLManager sqlManager = getSQLManager( reference.getParent());
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    statement.setString( 1, reference.getID());
    
    return statement;
  }
  
  /**
   * Returns a prepared statement which will insert one or more rows.
   * @param reference The reference representing a table.
   * @param nodes The rows to be inserted in the table.
   * @return Returns a prepared statement which will insert one or more nodes.
   */
  private PreparedStatement createInsertStatement( IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    String table = Xlate.get( reference, "table", (String)null);
    
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
        if ( columns.equals( key)) continue;
        IModelObject field = node.getFirstChild( columns[ j]);
        statement.setObject( j+1, field.getValue());
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
  private PreparedStatement createUpdateStatement( IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    String table = Xlate.get( reference, "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "UPDATE "); sb.append( table);
    sb.append( " SET ");

    for( int i=0; i<columns.length; i++)
    {
      if ( columns.equals( key)) continue;
      if ( i>0) sb.append( ",");
      sb.append( columns[ i]);
      sb.append( "=?");
    }
    
    sb.append(" WHERE ");
    sb.append( key);
    sb.append( "=?");
    
    SQLManager sqlManager = getSQLManager( reference);
    PreparedStatement statement = sqlManager.prepareStatement( sb.toString());
    
    for( IModelObject node: nodes)
    {
      statement.setString( 1, node.getID());
      for( int j=0; j<columns.length; j++)
      {
        if ( columns.equals( key)) continue;
        IModelObject field = node.getFirstChild( columns[ j]);
        statement.setObject( j+1, field.getValue());
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
  private PreparedStatement createDeleteStatement( IExternalReference reference, List<IModelObject> nodes) throws SQLException
  {
    String table = Xlate.get( reference, "table", (String)null);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "DELETE FROM "); sb.append( table);
    sb.append( " WHERE "); sb.append( key);
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
  
  private static IExpression sqlManagerExpr = XPath.createExpression( 
    "ancestor::*/meta:sqlmanager");
  
  private IModelObject defaultManager;
  private IModelObjectFactory factory;
  private String[] columns;
  private String key;
  private String child;
  
  /**
   * An interface for creating (and possibly caching) instances of java.sql.Statement. An implementation
   * of the interface must be defined on the <i>meta:sqlmanager</i> attribute or <i>meta:sqlmanager</i> child 
   * of an ancestor of each IExternalReference which uses an SQLCachingPolicy.
   */
  public interface SQLManager
  {
    /**
     * Configure this manager from the specified annotation.
     * @param annotation The annotation.
     */
    public void configure( IModelObject annotation) throws CachingException;
    
    /**
     * Returns a Connection possibly from a pool of Connection objects.
     * @return Returns a Connection possibly from a pool of Connection objects.
     */
    public Connection getConnection() throws CachingException;
    
    /**
     * Returns a PreparedStatement built from the specified SQL. This method is provided to allow
     * the implementation to optionally cache PreparedStatement instances.
     * @param sql The SQL statement.
     * @return Returns a PreparedStatement built from the specified SQL.
     */
    public PreparedStatement prepareStatement( String sql) throws CachingException; 
  }
}
