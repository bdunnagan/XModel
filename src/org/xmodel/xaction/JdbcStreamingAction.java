package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.caching.sql.SQLCachingPolicy;
import org.xmodel.external.ICachingPolicy;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that configures one or more external references using SQLCachingPolicy for streaming.
 * JDBC Statements are guaranteed to be closed when the action exits.
 */
public class JdbcStreamingAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    onExpr = document.getExpression( "on", true);
    script = document.createScript();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    List<SQLCachingPolicy> cachingPolicies = new ArrayList<SQLCachingPolicy>( 1);
    for( IModelObject element: onExpr.evaluateNodes( context))
    {
      ICachingPolicy cachingPolicy = element.getCachingPolicy();
      if ( cachingPolicy instanceof SQLCachingPolicy)
      {
        SQLCachingPolicy sqlCachingPolicy = (SQLCachingPolicy)cachingPolicy; 
        sqlCachingPolicy.setStreamingEnabled( true);
        cachingPolicies.add( sqlCachingPolicy);
      }
    }
    
    try
    {
      script.run( context);
    }
    finally
    {
      for( SQLCachingPolicy cachingPolicy: cachingPolicies)
        cachingPolicy.closeStreamedStatement();
    }
    
    return null;
  }
  
  private IExpression onExpr;
  private IXAction script;
}
