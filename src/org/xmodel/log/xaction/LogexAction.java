package org.xmodel.log.xaction;

import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that logs a message at a predefined logging level.
 */
public class LogexAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    String logName = LogAction.createLogName( document.getRoot());
    log = Log.getLog( logName);
    
    level = Log.exception;
    exceptionExpr = document.getExpression();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( log.isLevelEnabled( level))
    {
      IModelObject exception = exceptionExpr.queryFirst( context);
      log.log( level, XmlIO.write( Style.printable, exception));
    }
    return null;
  }

  private Log log;
  private int level;
  private IExpression exceptionExpr;
}
