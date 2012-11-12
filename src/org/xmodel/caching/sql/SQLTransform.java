package org.xmodel.caching.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelObjectFactory;

/**
 * This class provides a configurable transform from the results of a SQL query to one
 * or more elements. This class allows each column in the result-set to be mapped to an
 * attribute or a child.  Unmapped columns are mapped to children with the same name as
 * the column name returned by the SQL statement.
 */
public class SQLTransform
{
  /**
   * Create a SQLTransform from the specified statement.
   * @param statement The query statement.
   */
  public SQLTransform( PreparedStatement statement)
  {
    this.statement = statement;
    rowElementFactory = new ModelObjectFactory();
    columnElementFactory = new ModelObjectFactory();
    attributes = new HashMap<String, String>();
    children = new HashMap<String, String>();
  }
  
  /**
   * Set the factory for row elements.
   * @param factory The factory.
   */
  public void setRowElementFactory( IModelObjectFactory factory)
  {
    rowElementFactory = factory;
  }
  
  /**
   * Set the factory for columns containing xml.
   * @param factory The factory.
   */
  public void setColumnElementFactory( IModelObjectFactory factory)
  {
    columnElementFactory = factory;
  }
  
  /**
   * Set the element name to be used for row elements.
   * @param name The name of each row element.
   */
  public void setRowElementName( String name)
  {
    rowElementName = name;
  }
  
  /**
   * Map the specified column to an attribute.
   * @param column The column name.
   * @param attribute The attribute name.
   */
  public void mapAttribute( String column, String attribute)
  {
    attributes.put( column, attribute);
  }
  
  /**
   * Map the specified column to a child.
   * @param column The column name.
   * @param child The child name.
   */
  public void mapChild( String column, String child)
  {
    children.put( column, child);
  }

  /**
   * Execute the query.
   * @return Returns the transformed result-set.
   */
  public List<IModelObject> execute() throws SQLException
  {
    ResultSet result = statement.executeQuery();
    while( result.next())
    {
      IModelObject rowElement = rowElementFactory.createObject( null, rowElementName);
      
    }    
  }
  
  private PreparedStatement statement;
  private IModelObjectFactory rowElementFactory;
  private IModelObjectFactory columnElementFactory;
  private String rowElementName;
  private Map<String, String> attributes;
  private Map<String, String> children;
}
