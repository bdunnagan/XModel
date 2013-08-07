package org.xmodel.log.xaction;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction used for sending a message to a log, or setting the logging level of a log.
 */
public class LogAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    logName = createLogName( document.getRoot());
    logNameExpr = document.getExpression( "name", true);
    levelExpr = document.getExpression( "level", true);
    messageExpr = document.getExpression();
  }
  
  /**
   * Generate the log name from the location of the script element.
   * @param root The root of the script element.
   * @return Returns the generated log name.
   */
  static String createLogName( IModelObject root)
  {
    IModelObject node = root;
    IModelObject parent = node.getParent();
    while( parent != null && node.getAttribute( "xaction") != null)
    {
      node = parent;
      parent = node.getParent();
    }
    
    String scriptName = Xlate.get( node, "name", (String)null);
    if ( scriptName != null)
    {
      return "\""+scriptName+"\"";
    }
    else
    {
      IPath path = ModelAlgorithms.createIdentityPath( node);
      return path.toString();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    int level = Log.getLevelIndex( levelExpr.evaluateString( context));
    if ( logNameExpr != null) logName = logNameExpr.evaluateString( context);
    
    if ( messageExpr == null)
    {
      Log.setLevel( logName, level);
    }
    else
    {
      Log log = Log.getLog( logName);
      if ( log.isLevelEnabled( level))
      {
        String message = messageExpr.evaluateString( context);
        log.log( level, message);
      }
    }
    
    return null;
  }
  
  private String logName;
  private IExpression logNameExpr;
  private IExpression levelExpr;
  private IExpression messageExpr;
}
