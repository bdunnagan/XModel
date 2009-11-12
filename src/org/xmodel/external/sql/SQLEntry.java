/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SQLEntry.java
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

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IExpression;


/**
 * A convenience class which handles the SQL statement definitions in the configuration of an SQLCachingPolicy.
 * The class is responsible for evaluating parameter expressions and assigning parameters to a statement.
 * <p>
 * SQL statement annotations have the form:
 * <sql>
 *   <statement>...</statement>
 *   <param>...</param>
 *   <param>...</param>
 *   ...
 * </sql>
 */
public class SQLEntry
{
  /**
   * Create an SQLStatement object for the specified <sql> annotation.
   * @param annotation The <sql> annotation.
   */
  public SQLEntry( IModelObject annotation)
  {
    // get statement
    sql = Xlate.childGet( annotation, "statement", "");
    
    // get parameters
    List<IModelObject> paramNodes = annotation.getChildren( "param");
    params = new IExpression[ paramNodes.size()];
    for( int i=0; i<paramNodes.size(); i++)
      params[ i] = XPath.createExpression( Xlate.get( paramNodes.get( i), ""));
  }
  
  /**
   * Returns the SQL string for this statement.
   * @return Returns the SQL string for this statement.
   */
  public String getSQL()
  {
    return sql;
  }
  
  /**
   * Returns the name of the tables defined in the query.
   * @param statement The PreparedStatement from which the ResultMetaData will be obtained.
   * @return Returns the name of the tables defined in the query.
   */
  public String[] getTables( PreparedStatement statement) throws CachingException
  {
    if ( tables != null) return tables;
    
    try
    {
      ResultSetMetaData meta = statement.getMetaData();
      tables = new String[ meta.getColumnCount()];
      for( int i=0; i<tables.length; i++) tables[ i] = meta.getTableName( i);
      return tables;
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to get table names from metadata.", e);
    }
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
      for( int i=0; i<columns.length; i++) columns[ i] = meta.getColumnName( i);
      return columns;
    }
    catch( SQLException e)
    {
      throw new CachingException( "Unable to get column names from metadata.", e);
    }
  }
  
  /**
   * Evaluate the parameter expressions with the specified reference context and assign to statement.
   * @param reference The context of the parameter evaluations.
   * @param statement The statement.
   */
  public void assignParameters( IExternalReference reference, PreparedStatement statement) throws CachingException
  {
    try
    {
      for( int i=0; i<params.length; i++)
      {
        IExpression param = params[ i];
        switch( param.getType())
        {
          case NODES:
            throw new CachingException( "Illegal return type (NODES) for expression: "+param);
          
          case STRING:
          {
            String value = param.evaluateString( new Context( reference));
            statement.setString( i, value);
          }
          break;
          
          case NUMBER:
          {
            double value = param.evaluateNumber( new Context( reference));
            int intValue = (int)Math.floor( value);
            if ( intValue == value) statement.setInt( i, intValue); else statement.setDouble( i, value);
          }
          break;
          
          case BOOLEAN:
          {
            boolean value = param.evaluateBoolean( new Context( reference));
            statement.setBoolean( i, value);
          }
          break;
        }
      }
    }
    catch( SQLException e)
    {
      throw new CachingException( "Error setting sql parameters for reference: "+reference, e);
    }
  }
  
  String sql;
  String[] tables;
  String[] columns;
  IExpression[] params;
}
