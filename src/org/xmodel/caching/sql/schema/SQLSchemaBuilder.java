package org.xmodel.caching.sql.schema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.ISQLProvider;
import org.xmodel.log.SLog;

/**
 * This class reads an xml description of a table schema and generates SQL commands
 * to build the table.
 */
public class SQLSchemaBuilder
{
  public SQLSchemaBuilder( ISQLProvider provider)
  {
    this.provider = provider;
  }
  
  public void build( IModelObject schema) throws SQLException
  {
    IModelObject table = schema.getFirstChild( "table").getChild( 0);
    
    StringBuilder sb = new StringBuilder();
    sb.append( "CREATE TABLE "); sb.append( table); sb.append( " (\n");
    
    // 
    // Column definitions 
    //
    List<IModelObject> columns = table.getChildren();
    for( int i=0; i<columns.size(); i++)
    {
      IModelObject column = columns.get( i);
      
      String type = Xlate.get( column, "type", (String)null);
      if ( type == null) throw new IllegalArgumentException( "SQL column type is not defined.");
      sb.append( String.format( "  %-20s %s", column.getType(), type.toUpperCase()));
      
      String defaultValue = Xlate.get( column, "default", (String)null);
      if ( defaultValue == null)
      {
        sb.append( " NOT NULL");
      }
      else if ( !defaultValue.equalsIgnoreCase( "null"))
      {
        sb.append( " DEFAULT ");
        sb.append( defaultValue);
      }
      
      sb.append( ",\n");
    }

    //
    // Index definitions
    //
    IModelObject indexes = schema.getFirstChild( "indexes");
    if ( indexes != null)
    {
      for( IModelObject index: indexes.getChildren())
      {
        sb.append( "  ");
        
        String firstColumn = Xlate.get( index, "column", Xlate.childGet( index, "column", (String)null));
        String indexName = Xlate.get( index, "name", firstColumn);

        boolean primary = Xlate.get( index, "primary", false);
        boolean unique = Xlate.get( index, "unique", false);
        
        if ( unique && !primary) sb.append( "UNIQUE ");
        sb.append( primary? "PRIMARY KEY( ": "INDEX ");
        if ( !primary) sb.append( indexName);
        sb.append( " (");

        for( IModelObject column: index.getChildren())
        {
          String name = Xlate.get( column, (String)null);
          if ( name != null)
          {
            sb.append( name);
            sb.append( ", ");
          }
        }
        sb.setLength( sb.length() - 2);
        sb.append( "),\n");
      }
    }
    
    //
    // Close table
    //
    sb.append( "\n)");
    
    //
    // Specify MySQL engine
    //
    String engine = Xlate.get( table, "engine", (String)null);
    if ( engine != null)
    {
      sb.append( "\nENGINE = ");
      sb.append( engine);
    }
    
    //
    // Partitioning
    //
    IModelObject partitions = schema.getFirstChild( "partitions"); 
    for( IModelObject partition: partitions.getChildren())
    {
      String column = Xlate.get( partition, (String)null);
      if ( column != null)
      {
        int count = Xlate.get( partition, "count", 0);
        if ( count > 1)
        {
          sb.append( "\nPARTITION BY KEY( ");
          sb.append( column);
          sb.append( ")");
          sb.append( "\nPARTITIONS ");
          sb.append( count);
        }
      }
    }
    
    if ( partitions.getChildren().size() == 0)
      sb.append( ";");
    
    SLog.infof( SQLSchemaBuilder.class, "Creating table %s with statement:\n%s", table, sb);
    
    Connection connection = provider.leaseConnection();
    PreparedStatement statement = connection.prepareStatement( sb.toString());
    try
    {
      statement.execute();
    }
    finally
    {
      provider.close( statement);
    }
  }

  private ISQLProvider provider;
}
