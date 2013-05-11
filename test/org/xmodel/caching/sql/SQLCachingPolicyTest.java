package org.xmodel.caching.sql;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.log.SLog;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.StatefulContext;

public class SQLCachingPolicyTest
{
  @Before
  public void setUp() throws Exception
  {
    Class.forName( "com.mysql.jdbc.Driver");
    connection = DriverManager.getConnection( "jdbc:mysql://localhost/", "root", "root");
    
    try
    {
      PreparedStatement dropDB = connection.prepareStatement( "drop database cptest");
      dropDB.execute();
    }
    catch( Exception e)
    {
      SLog.warnf( this, e.getMessage());
    }
    
    try
    {
      PreparedStatement createDB = connection.prepareStatement( "create database cptest");
      createDB.execute();
      
      connection.setCatalog( "cptest");
      
      PreparedStatement createTable = connection.prepareStatement( 
          "create table test (" +
          "  id varchar(10) primary key," +
          "  text varchar(10)" +
          ")");
      createTable.execute();
      
      for( int i=0; i<10; i++)
      {
        PreparedStatement insertRow = connection.prepareStatement(
            String.format( "insert into test set id = %d, text = '%d'", i, i));
        insertRow.execute();
      }
    }
    catch( Exception e)
    {
      SLog.warnf( this, e.getMessage());
    }
  }
  
  @After
  public void tearDown() throws Exception
  {
    connection.close();
  }

  @Test
  public void test() throws Exception
  {
    String xml = 
        "<script>" +
        "  <create var=\"sonarDB\">\n" + 
        "    <provider>mysql</provider>\n" + 
        "    <host>localhost</host>\n" + 
        "    <username>root</username>\n" + 
        "    <password>root</password>\n" + 
        "    <catalog>cptest</catalog>\n" + 
        "  </create>\n" + 
        "" +
        "  <assign var='id'>5</assign>" +
        "  <create var='x'>" +
        "    <table>" +
        "      <extern:cache class='org.xmodel.caching.sql.SQLTableCachingPolicy'>" +
        "        <provider>$sonarDB</provider>\n" + 
        "        <host>localhost</host>\n" + 
        "        <username>root</username>\n" + 
        "        <password>root</password>\n" + 
        "        <catalog>cptest</catalog>\n" + 
        "        <table>test</table>" +
        "        <where>format( 'id = %s', $id)</where>\n" + 
        "      </extern:cache>" +
        "    </table>" +
        "  </create>" +
        "  <print>$x</print>" +
        "  <return>$x</return>" +
        "</script>";
    
    IXAction action = XActionDocument.parseScript( xml);
    action.run( new StatefulContext());
    
    fail( "Not yet implemented");
  }
  
  private Connection connection;
}
