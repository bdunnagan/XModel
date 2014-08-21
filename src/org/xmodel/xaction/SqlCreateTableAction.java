package org.xmodel.xaction;

import java.sql.SQLException;

import org.xmodel.IModelObject;
import org.xmodel.caching.sql.ISQLProvider;
import org.xmodel.caching.sql.SQLProviders;
import org.xmodel.caching.sql.schema.SQLSchemaBuilder;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class SqlCreateTableAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    schemaExpr = document.getExpression( "schema", true);
    if ( schemaExpr == null) schemaExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    try
    {
      ISQLProvider provider = SQLProviders.getProvider( context, getDocument().getRoot());
      SQLSchemaBuilder schemaBuilder = new SQLSchemaBuilder( provider);
      for( IModelObject schema: schemaExpr.evaluateNodes( context))
      {
        schemaBuilder.build( schema);
      }
    }
    catch( SQLException e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }

  private IExpression schemaExpr;
}
