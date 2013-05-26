package org.xmodel.xaction;

import java.text.ParseException;
import org.xmodel.util.JsonParser;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Parse JSON into a data-model.
 */
public class ParseJsonAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    
    jsonExpr = document.getExpression();
    if ( jsonExpr == null) jsonExpr = document.getExpression( "json", true);
    
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    try
    {
      JsonParser parser = new JsonParser();
      Object object = parser.parse( jsonExpr.evaluateString( context));
      
      if ( object != null)
      {
        context.getScope().set( var, object);
      }
    }
    catch( ParseException e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  private String var;
  private IExpression jsonExpr;
}
