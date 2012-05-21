package org.xmodel.external.sql;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class SQLCachingPolicyTest
{
  public static void main( String[] args) throws Exception
  {
    String xml = 
        "<script>" +
        "  <create var='users'>" +
        "    <users>" +
        "      <extern:cache class='org.xmodel.external.sql.SQLDirectCachingPolicy'>" +
        "        <provider>mysql</provider>" +
        "        <db>nephos6_com_main</db>" +
        "        <user>root</user>" +
        "        <password>root</password>" +
        "        <table>user</table>" +
        "        <row>user</row>" +
        "      </extern:cache>" +
        "    </users>" +
        "  </create>" +
        "</script>";
    
    XmlIO xmlIO = new XmlIO();
    IModelObject node = xmlIO.read( xml);
    XActionDocument doc = new XActionDocument( node);
    IXAction script = doc.createScript();    
    StatefulContext context = new StatefulContext();
    script.run( context);
    
    IExpression usersExpr = XPath.createExpression( "$users");
    IModelObject user = usersExpr.queryFirst( context);
    System.out.println( xmlIO.write( user));

    xml = 
        "<script>" +
        "  <create parent='$users'>" +
        "    <user id='bob'>" +
        "      <created>0</created>" +
        "      <status>enabled</status>" +
        "      <status_ts>0</status_ts>" +
        "      <tenant>nephos6_com</tenant>" +
        "      <first_name>Bob</first_name>" +
        "      <last_name>Dunnagan</last_name>" +
        "      <email>bdunnagan@gmail.com</email>" +
        "      <phone>919.610.8300</phone>" +
        "      <password>root</password>" +
        "    </user>" +
        "  </create>" +
        "</script>";
    
    node = xmlIO.read( xml);
    doc = new XActionDocument( node);
    script = doc.createScript();    
    script.run( context);
  }
}
