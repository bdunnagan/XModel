/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.external.caching;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
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
public class SQLCachingPolicy extends ConfiguredCachingPolicy
{
  public SQLCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  /**
   * Create the caching policy with the specified cache.
   * @param cache The cache.
   */
  public SQLCachingPolicy( ICache cache)
  {
    super( cache);
    setStaticAttributes( new String[] { "id", "meta:*"});
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    // get manager defined in annotation
    defaultManager = annotation.getFirstChild( "meta:sqlmanager");

    // save annotation
    this.annotation = annotation;
    
    // validate queries
    List<IModelObject> queryNodes = annotation.getChildren( "query");    
    String[] queries = new String[ queryNodes.size()];
    for( int i=0; i<queries.length; i++)
    {
      queries[ i] = Xlate.get( queryNodes.get( i), "");
      if ( queries[ i].length() == 0) 
        throw new CachingException( 
          "Undefined query in annotation: "+annotation);
    }
    
    // validate update
    IModelObject updateNode = annotation.getFirstChild( "update");
    if ( updateNode != null)
    {
      String update = Xlate.get( updateNode, "");
      if ( update.length() == 0) 
        throw new CachingException( 
          "Undefined update in annotation: "+annotation);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    // get SQLManager
    SQLManager sqlManager = getSQLManager( reference);
    
    // execute queries
    IModelObject object = getFactory().createObject( null, reference.getType());
    ModelAlgorithms.copyAttributes( reference, object);
    List<IModelObject> queries = annotation.getChildren( "query");    
    for( IModelObject query: queries)
    {
      try
      {
        PreparedStatement statement = sqlManager.prepareStatement( Xlate.get( query, ""));
        ParameterMetaData parameterMetaData = statement.getParameterMetaData();
        if ( parameterMetaData.getParameterCount() > 0)
          statement.setString( 1, reference.getID());
        
        // execute query
        ResultSet result = statement.executeQuery();
        
        // query has two forms: one for querying the reference fields, and one for bulk load
        String createSpec = Xlate.get( query, "create", "");
        if ( createSpec.length() == 0)
        {
          // handle result set for reference
          String[] columns = getColumns( statement);        
        
          // get first row
          if ( result.next())
          {      
            for( int i=0; i<columns.length; i++)
            {
              if ( columns[ i].equals( "id")) continue;
              IModelObject field = getFactory().createObject( object, columns[ i]);
              field.setValue( result.getObject( i+1));
              object.addChild( field);
            }        
          }
          
          // cleanup
          result.close();
        }
        else
        {
          // handle result set for bulk load
          IPath createPath = XPath.createPath( createSpec);
          IModelObject dummy = getFactory().createObject( null, "dummy");
          while( result.next())
          {
            ModelAlgorithms.createPathSubtree( dummy, createPath, null, null);
            if ( dummy.getNumberOfChildren() > 0)
            {
              // set id
              IModelObject leaf = createPath.queryFirst( dummy);
              leaf.setID( result.getString( 1));
              
              // move subtree
              IModelObject subtree = dummy.getChild( 0);
              object.addChild( subtree);
            }
          }
          
          // cleanup
          result.close();
        }
      }
      catch( SQLException e)
      {
        throw new CachingException( "Error executing SELECT for reference: "+reference, e);
      }
    }
        
    // update reference
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
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#remove(org.xmodel.external.IExternalReference, 
   * org.xmodel.IModelObject)
   */
  public void remove( IExternalReference parent, IModelObject object) throws CachingException
  {
  }
  
  /**
   * Returns the SQLManager for the specified reference. If the manager annotation exists, but the
   * manager has not yet been initialized, then it will be initialized here.
   * @return Returns the SQLManager for the specified reference.
   */
  @SuppressWarnings("unchecked")
  private SQLManager getSQLManager( IExternalReference reference) throws CachingException
  {
    IModelObject sqlManagerNode = defaultManager;
    if ( sqlManagerNode == null) sqlManagerNode = sqlManagerExpr.queryFirst( reference);
    SQLManager sqlManager = (SQLManager)sqlManagerNode.getAttribute( "instance");
    if ( sqlManager == null)
    {
      String className = Xlate.get( sqlManagerNode, "class", (String)null);
      if ( className == null) throw new CachingException( "SQLManager class attribute not defined: "+sqlManagerNode);
      
      try
      {
        Class clss = getClass().getClassLoader().loadClass( "org.xmodel.external.caching."+className);
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
  
  private static IExpression sqlManagerExpr = XPath.createExpression( 
    "ancestor::*/meta:sqlmanager");    
  
  private IModelObject defaultManager;
  private IModelObject annotation;
  private String[] columns;
  
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
