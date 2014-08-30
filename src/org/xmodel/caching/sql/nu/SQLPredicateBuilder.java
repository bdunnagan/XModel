package org.xmodel.caching.sql.nu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmodel.IAxis;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.IPathElement;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.EqualityExpression;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.expression.LogicalExpression;
import org.xmodel.xpath.expression.PathExpression;
import org.xmodel.xpath.expression.PredicateExpression;
import org.xmodel.xpath.expression.RelationalExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class SQLPredicateBuilder
{
  public boolean build( IModelObject schema, IContext context, IPathElement step, StringBuilder sql)
  {
    if ( step.type() != null && !step.type().equals( "*") && !step.type().equals( schema.getFirstChild( "table").getChild( 0).getType()))
    {
      sql.append( "FALSE");
      return true;
    }
    
    PredicateExpression predicate = (PredicateExpression)step.predicate();
    return buildAny( schema, context, predicate.getArgument( 0), sql);
  }

  private boolean buildAny( IModelObject schema, IContext context, IExpression expr, StringBuilder sql)
  {
    if ( expr instanceof EqualityExpression)
    {
      return buildEquality( schema, context, (EqualityExpression)expr, sql);
    }
    else if ( expr instanceof LogicalExpression)
    {
      return buildLogical( schema, context, (LogicalExpression)expr, sql);
    }
    else if ( expr instanceof RelationalExpression)
    {
      return buildRelational( schema, context, (RelationalExpression)expr, sql);
    }
    else if ( expr instanceof PathExpression)
    {
      IModelObject column = getColumnSchema( expr);
      if ( column != null)
      {
        sql.append( column.getType());
        sql.append( " IS NOT NULL ");
      }
    }
    
    return false;
  }

  private boolean buildEquality( IModelObject schema, IContext context, EqualityExpression expr, StringBuilder sql)
  {
    IExpression targetExpr = null;
    
    IModelObject column = getColumnSchema( expr.getArgument( 0));
    if ( column != null) 
    {
      targetExpr = expr.getArgument( 1);
    }
    else
    {
      targetExpr = expr.getArgument( 0);
      column = getColumnSchema( expr.getArgument( 1));
    }
    
    if ( column == null) 
    {
      SLog.warnf( this, "Missing table column reference in equality expression: %s", expr.toString());
      return false;
    }
    
    StringBuilder targetSql = new StringBuilder();
    int count = buildTargets( column, context, targetExpr, true, targetSql);
    if ( count == -1)
    {
      return false;
    }
    else if ( count == 0)
    {
      sql.append( " FALSE ");
    }
    else if ( count == 1)
    {
      sql.append( column.getType());
      
      switch( expr.getOperator())
      {
        case EQ:  sql.append( " = "); break;
        case NEQ: sql.append( " != "); break;
      }
      
      sql.append( targetSql);
    }
    else
    {
      sql.append( column.getType());
      
      switch( expr.getOperator())
      {
        case EQ:  sql.append( " IN "); break;
        case NEQ: sql.append( " NOT IN "); break;
      }
      
      sql.append( targetSql);
    }
    
    return true;
  }
  
  private boolean buildLogical( IModelObject schema, IContext context, LogicalExpression expr, StringBuilder sql)
  {
    sql.append( '(');
    if ( !buildAny( schema, context, expr.getArgument( 0), sql)) return false;
    
    switch( expr.getOperator())
    {
      case AND: sql.append( " AND "); break;
      case OR:  sql.append( " OR "); break;
    }
    
    if ( !buildAny( schema, context, expr.getArgument( 1), sql)) return false;
    sql.append( ')');
    
    return true;
  }
  
  private boolean buildRelational( IModelObject schema, IContext context, RelationalExpression expr, StringBuilder sql)
  {
    IExpression targetExpr = null;
    
    IModelObject column = getColumnSchema( expr.getArgument( 0));
    if ( column != null) 
    {
      targetExpr = expr.getArgument( 1);
    }
    else
    {
      targetExpr = expr.getArgument( 0);
      column = getColumnSchema( expr.getArgument( 1));
    }
    
    if ( column == null) 
    {
      SLog.warnf( this, "Missing table column reference in relational expression: %s", expr.toString());
      return false;
    }
    
    sql.append( column.getType());
    
    switch( expr.getOperator())
    {
      case GE: sql.append( " >= "); break;
      case GT: sql.append( " > "); break;
      case LE: sql.append( " <= "); break;
      case LT: sql.append( " < "); break;
    }
    
    buildTargets( column, context, targetExpr, false, sql);
    
    return true;
  }

  private int buildTargets( IModelObject column, IContext context, IExpression targetExpr, boolean allowMultiple, StringBuilder sql)
  {
    ResultType columnResultType = getResultType( Xlate.get( column, "type", (String)null));
    ResultType targetResultType = targetExpr.getType( context);
    switch( targetResultType)
    {
      case NODES:
      {
        List<IModelObject> targets = targetExpr.query( context, null);
        if ( targets.size() == 0)
        {
          return 0;
        }
        else if ( targets.size() == 1 || !allowMultiple)
        {
          switch( columnResultType)
          {
            case STRING:  sql.append( '"'); sql.append( Xlate.get( targets.get( 0), "")); sql.append( '"'); break;
            case NUMBER:  sql.append( Xlate.get( targets.get( 0), 0)); break;
            case BOOLEAN: sql.append( Xlate.get( targets.get( 0), false)); break;
            default: return -1;
          }
          return 1;
        }
        else
        {
          sql.append( "(");
          
          String sep = "";
          for( IModelObject target: targets)
          {
            sql.append( sep); sep = ", ";
            switch( columnResultType)
            {
              case STRING:  sql.append( '"'); sql.append( Xlate.get( target, "")); sql.append( '"'); break;
              case NUMBER:  sql.append( Xlate.get( target, 0)); break;
              case BOOLEAN: sql.append( Xlate.get( target, false)); break;
              default: return -1;
            }
          }
          
          sql.append( ")");
          
          return targets.size();
        }
      }
      
      case STRING:
      {
        String value = targetExpr.evaluateString( context); 
        switch( columnResultType)
        {
          case STRING:  sql.append( '"'); sql.append( value); sql.append( '"'); break;
          case NUMBER:  sql.append( value); break;
          case BOOLEAN: sql.append( value); break;
          default: return -1;
        }
        return 1;
      }
        
      case NUMBER:
      {
        String value = targetExpr.evaluateString( context); 
        switch( columnResultType)
        {
          case STRING:  sql.append( '"'); sql.append( value); sql.append( '"'); break;
          case NUMBER:  sql.append( value); break;
          case BOOLEAN: sql.append( value); break;
          default: return -1;
        }
        return 1;
      }
      
      default: return -1;
    }
  }
  
  private ResultType getResultType( String sqlType)
  {
    sqlType = sqlType.toUpperCase();
    
    ResultType type = numericTypeMap.get( sqlType);
    if ( type != null) return type;
    
    if ( sqlType.startsWith( "CHAR")) return ResultType.STRING;
    if ( sqlType.startsWith( "VARCHAR")) return ResultType.STRING;
    
    return ResultType.UNDEFINED;
  }
  
  private IModelObject getColumnSchema( IExpression expr)
  {
    if ( expr instanceof PathExpression)
    {
      PathExpression pathExpr = (PathExpression)expr;
      IPath path = pathExpr.getPath();
      if ( path.length() == 1)
      {
        IPathElement step = path.getPathElement( 0);
        if ( (step.axis() & (IAxis.ATTRIBUTE | IAxis.CHILD)) != 0)
        {
          return schema.getFirstChild( "table").getChild( 0).getFirstChild( step.type());
        }
      }
    }
    
    return null;
  }

  private IModelObject schema;
  
  private static Map<String, ResultType> numericTypeMap = new HashMap<String, ResultType>();
  static
  {
    numericTypeMap.put( "TINYINT", ResultType.NUMBER);
    numericTypeMap.put( "SMALLINT", ResultType.NUMBER);
    numericTypeMap.put( "INT", ResultType.NUMBER);
    numericTypeMap.put( "LONG", ResultType.NUMBER);
    numericTypeMap.put( "BIGINT", ResultType.NUMBER);
    numericTypeMap.put( "FLOAT", ResultType.NUMBER);
    numericTypeMap.put( "DECIMAL", ResultType.NUMBER);
    numericTypeMap.put( "DOUBLE", ResultType.NUMBER);
  }
  
  public static void main( String[] args) throws Exception
  {
    String schemaXml = 
      "<schema> "+
      "  <table> "+
      "    <user> "+
      "      <id type='BIGINT'/>"+
      "      <name type='VARCHAR(100)'/>"+
      "    </user> "+
      "  </table> "+
      "</schema>";
    
    IModelObject schema = new XmlIO().read( schemaXml);
    
    IExpression expr = XPath.createExpression( "*[ (name = 'Bob' or @id = $x/*) and name = 'Fred']");
    IPathElement step = ((PathExpression)expr.getArgument( 0)).getPath().getPathElement( 0);
    
    StatefulContext context = new StatefulContext( new ModelObject( "test"));
    
    String listXml =
      "<xs> "+
      "  <x>1</x> "+
      "  <x>2</x> "+
      "  <x>3</x> "+
      "</xs> ";
    
    IModelObject list = new XmlIO().read( listXml);
    context.set( "x", list);
    
    SQLPredicateBuilder builder = new SQLPredicateBuilder();
    StringBuilder sql = new StringBuilder();
    builder.build( schema, context, step, sql);
    System.out.println( sql.toString());
  }
}
