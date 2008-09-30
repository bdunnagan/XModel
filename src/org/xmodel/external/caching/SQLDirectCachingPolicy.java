/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.external.caching;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
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
@SuppressWarnings("unused")
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
    defineNextStage( stageExpr, this, true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
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
          for( int i=0; i<otherKeys.size(); i++) 
            stub.setAttribute( otherKeys.get( i), result.getString( i));
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
            if ( columns[ i].equals( primaryKey)) continue;
            
            if ( otherKeys.contains( columns[ i]))
            {
              Object value = transformValue( reference, null, result.getObject( i+1));
              object.setAttribute( columns[ i], value);
            }
            else
            {
              IModelObject field = getFactory().createObject( object, columns[ i]);
              Object value = transformValue( reference, field, result.getObject( i+1));
              field.setValue( value);
              object.addChild( field);
            }
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
  
  /**
   * Transform the value of a table row column into the value to be stored in the fragment.
   * @param rowElement The table row element.
   * @param columnElement The column element.
   * @param value The value of the column from the ResultSet.
   * @return Returns the value to be stored in the element.
   */
  private Object transformValue( IModelObject rowElement, IModelObject columnElement, Object value) throws CachingException
  {
    if ( value instanceof Blob)
    {
      if ( columnElement == null) 
        throw new CachingException(
          "Blobs cannot be mapped to attributes.");
      try
      {
        PreparedStatement statement = createColumnSelectStatement( rowElement, columnElement);
        BlobAccess access = new BlobAccess( statement);
        return access;
      }
      catch( SQLException e)
      {
        throw new CachingException( "Unable to create blob access for column: "+columnElement, e);
      }
    }
    else
    {
      return value;
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
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
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
        Class clss = SQLDirectCachingPolicy.class.getClassLoader().loadClass( "org.xmodel.external.caching."+className);
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
    sb.append( "SELECT * "); sb.append( " FROM "); sb.append( table);
    sb.append(" WHERE "); sb.append( primaryKey); sb.append( "=?");
    
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
        if ( columns.equals( primaryKey)) continue;
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
      if ( columns.equals( primaryKey)) continue;
      if ( i>0) sb.append( ",");
      sb.append( columns[ i]);
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
        if ( columns.equals( primaryKey)) continue;
        
        if ( otherKeys.contains( columns[ j]))
        {
          statement.setObject( j+1, node.getAttribute( columns[ j]));
        }
        else
        {
          IModelObject field = node.getFirstChild( columns[ j]);
          statement.setObject( j+1, field.getValue());
        }
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
  
  private static IExpression sqlManagerExpr = XPath.createExpression( 
    "ancestor-or-self::*/meta:sqlmanager");
  
  private IModelObjectFactory factory;
  private String[] columns;
  private String primaryKey;
  private List<String> otherKeys;
  private String child;
}
