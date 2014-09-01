package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;
import org.xmodel.xpath.PathElement;
import org.xmodel.xpath.expression.IContext;

public class GenericSelectRequest implements ISQLRequest
{
  public GenericSelectRequest( IModelObject schema, IContext context, PathElement step)
  {
    queryBuilder = new SQLPredicateBuilder( schema, step);
    build( schema, context);
  }
  
  private void build( IModelObject schema, IContext context)
  {
    this.schema = schema;
    
    IModelObject tableSchema = schema.getFirstChild( "table").getChild( 0);
    
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
    
    int mark = sql.length();
    sql.append( " WHERE ");
    if ( queryBuilder.build( context, sql))
    {
      long rowLimit = queryBuilder.getRowLimit();
      if ( rowLimit >= 0)
      {
        sql.append( " LIMIT ");
        sql.append( rowLimit);
      }
    }
    else
    {
      sql.setLength( mark);
    }
  }
  
  @Override
  public IModelObject getSchema()
  {
    return schema;
  }

  @Override
  public String getSQL()
  {
    return sql;
  }

  @Override
  public int getParamType( int paramIndex)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Object getParamValue( int paramIndex)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getParamCount()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getLimit()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isStreaming()
  {
    // TODO Auto-generated method stub
    return false;
  }

  private IModelObject schema;
  private SQLPredicateBuilder queryBuilder;
  private String sql;
}
