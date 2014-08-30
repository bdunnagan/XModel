package org.xmodel.caching.sql.nu.mysql;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.nu.ISQLCursor;
import org.xmodel.caching.sql.nu.ISQLProvider;
import org.xmodel.caching.sql.nu.ISQLRequest;
import org.xmodel.caching.sql.nu.JDBCCursor;
import org.xmodel.external.CachingException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MysqlProvider implements ISQLProvider
{
  @Override
  public void configure( IModelObject annotation)
  {
    String username = Xlate.childGet( annotation, "username", (String)null);
    if ( username == null) throw new CachingException( "Username not defined in annotation: "+annotation);
    
    String password = Xlate.childGet( annotation, "password", (String)null);
    if ( password == null) throw new CachingException( "Password not defined in annotation: "+annotation);
    
    database = Xlate.childGet( annotation, "database", (String)null);
    
    String host = Xlate.childGet( annotation, "host", "localhost");
    String url = String.format( "jdbc:mysql://%s/%s", host, database);
    
    HikariConfig config = new HikariConfig();
    config.setPoolName( database);
    config.setRegisterMbeans( true);
    config.setDataSourceClassName( "com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
    config.addDataSourceProperty( "url", url);
    config.addDataSourceProperty( "serverName", host);
    config.addDataSourceProperty( "port", "3306");
    config.addDataSourceProperty( "databaseName", database);
    
    int maxPoolSize = Xlate.childGet( annotation, "maxPoolSize", -1); 
    if ( maxPoolSize > 0) config.setMaximumPoolSize( maxPoolSize);
    
    //int minIdleTime = Xlate.childGet( annotation, "minIdleTime", -1); 
    //if ( minIdleTime >= 0) config.setMinimumIdle( minIdleTime);
    
    config.addDataSourceProperty( "user", Xlate.childGet( annotation, "username", (String)null));
    config.addDataSourceProperty( "password", Xlate.childGet( annotation, "password", (String)null));
    
    IModelObject hikari = annotation.getFirstChild( "hikari");
    if ( hikari != null)
    {
      config.addDataSourceProperty( "cachePrepStmts", Xlate.childGet( hikari, "cachePrepStmts", true));
      config.addDataSourceProperty( "prepStmtCacheSize", Xlate.childGet( hikari, "prepStmtCacheSize", 250));
      config.addDataSourceProperty( "prepStmtCacheSqlLimit", Xlate.childGet( hikari, "prepStmtCacheSqlLimit", 2048));
      config.addDataSourceProperty( "useServerPrepStmts", Xlate.childGet( hikari, "useServerPrepStmts", true));
      config.addDataSourceProperty( "rewriteBatchedStatements", Xlate.childGet( hikari, "rewriteBatchedStatements", true));
    }

    dataSource = new HikariDataSource( config);
    dataSource.setRegisterMbeans( true);
  }

  @Override
  public ISQLCursor query( ISQLRequest request) throws SQLException
  {
    Connection connection = dataSource.getConnection();
    PreparedStatement statement = null;
    try
    {
      statement = connection.prepareStatement( request.getSQL());
      setParams( statement, request);
      return new JDBCCursor( request, statement.executeQuery());
    }
    catch( SQLException e)
    {
      if ( statement != null) statement.close();
      connection.close();
      throw e;
    }
  }

  @Override
  public void update( ISQLRequest request) throws SQLException
  {
    Connection connection = dataSource.getConnection();
    PreparedStatement statement = null;
    try
    {
      statement = connection.prepareStatement( request.getSQL());
      setParams( statement, request);
      statement.executeUpdate();
    }
    finally
    {
      if ( statement != null) statement.close();
      connection.close();
    }
  }
  
  private void setParams( PreparedStatement statement, ISQLRequest request) throws SQLException
  {
    for( int i=0; i<request.getParamCount(); i++)
    {
      Object value = request.getParamValue( i);
      switch( request.getParamType( i))
      {
        case Types.TINYINT:   statement.setByte( i+1, (byte)value); break;  
        case Types.SMALLINT:  statement.setShort( i+1, (short)value); break;
        case Types.INTEGER:   statement.setInt( i+1, (int)value); break;
        case Types.BIGINT:    statement.setLong( i+1, (long)value); break;
        case Types.FLOAT:     statement.setFloat( i+1, (float)value); break;
        case Types.DECIMAL:
        case Types.DOUBLE:    statement.setDouble( i+1, (double)value); break;
        case Types.CHAR:
        case Types.VARCHAR:   statement.setString( i+1, value.toString()); break;
        case Types.BLOB:
        case Types.VARBINARY:
        {
          if ( value instanceof byte[])
          {
            statement.setBytes( i+1, (byte[])value);
          }
          else if ( value instanceof InputStream)
          {
            statement.setBinaryStream( i+1, (InputStream)value);
          }
        }
        break;
        
        default:
          throw new IllegalStateException( "SQL request has unexpected data type, "+ request.getParamType( i));
      }
      
    }
  }
  
  private String database;
  private HikariDataSource dataSource;
}
