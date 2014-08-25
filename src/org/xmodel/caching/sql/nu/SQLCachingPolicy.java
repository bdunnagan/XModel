package org.xmodel.caching.sql.nu;

import java.sql.Connection;
import org.xmodel.IModelObject;
import org.xmodel.IPathElement;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.ISQLProvider;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class SQLCachingPolicy extends ConfiguredCachingPolicy
{
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    
    IExpression schemaExpr = Xlate.childGet( annotation, "schema", (IExpression)null);
    if ( schemaExpr == null) throw new IllegalArgumentException( "Table schema expression is missing.");
    
    IModelObject schema = schemaExpr.queryFirst( context);
    if ( schema == null) throw new IllegalArgumentException( "Table schema is not defined.");

    tableSchema = schema.getFirstChild( "table").getChild( 0);
    queryBuilder = new SQLPredicateBuilder( schema);
  }

  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
  }

  @Override
  public void sync( IContext parent, IExternalReference reference, IPathElement step)
  {
    Connection connection = provider.leaseConnection();
    PreparedStatement statement = null;
    try
    {
      StringBuilder sql = buildSelect( new Context( parent, reference), step);
      PreparedStatement statement = provider.createStatement( connection, sql);
    }
    finally
    {
      
    }
    
  }
  
  private StringBuilder buildSelect( IContext context, IPathElement step)
  {
    StringBuilder sql = new StringBuilder();
    sql.append( "SELECT ");

    for( IModelObject column: tableSchema.getChildren())
    {
      sql.append( column.getType());
      sql.append( ", ");
    }
    sql.setLength( sql.length() - 2);
    
    sql.append( " FROM ");
    sql.append( tableSchema.getType());
    
    sql.append( " WHERE ");
    queryBuilder.build( context, step, sql);
    
    return sql;
  }
  
  private IModelObject tableSchema;
  private SQLPredicateBuilder queryBuilder;
  private ISQLProvider provider;
}
