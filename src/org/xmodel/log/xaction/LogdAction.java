package org.xmodel.log.xaction;

import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that logs a message at a predefined logging level.
 */
public class LogdAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    String logName = Xlate.get( document.getRoot(), "name", (String)null);
    if ( logName == null) logName = LogAction.createLogName( document.getRoot());
    log = Log.getLog( logName);
    
    level = Log.debug;
    messageExpr = document.getExpression();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( log.isLevelEnabled( level))
    {
      String message = messageExpr.evaluateString( context);
      log.log( level, message);
    }
    return null;
  }

  private Log log;
  private int level;
  private IExpression messageExpr;
}
