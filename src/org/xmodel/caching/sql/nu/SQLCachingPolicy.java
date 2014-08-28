package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;
import org.xmodel.IPathElement;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.caching.sql.SQLTransaction;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

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
  
  public void setStreaming( boolean streaming)
  {
    this.streaming = streaming;
  }

  @Override
  public ITransaction transaction()
  {
    return null;
  }

  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    if ( !streaming)
    {
      throw new CachingException( 
        "Full table loading requires streaming mode. If you did not intend "+
        "to load the entire table, insure that your xpath expression includes "+
        "a predicate.");
    }
  }

  @Override
  public void sync( IContext parent, IExternalReference reference, IPathElement step) throws CachingException
  {
    ISQLCursor cursor = null;
    try
    {
      StringBuilder sql = buildSelect( new StatefulContext( parent, reference), step);
      cursor = provider.query( sql.toString());
      
      if ( streaming)
      {
        throw new UnsupportedOperationException();
      }
      else
      {
        IModelObject update = new ModelObject( reference.getType());
        
        for( IModelObject row; (row = cursor.next()) != null; )
          update.addChild( row);
        
        update( reference, update);
      }
    }
    catch( Exception e)
    {
      String message = String.format( "Unable to sync reference, %s, with location step, %s", reference, step);
      throw new CachingException( message, e);
    }
    finally
    {
      if ( cursor != null) cursor.dispose();
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
  private boolean streaming;
  private SQLTransaction transaction;
}
