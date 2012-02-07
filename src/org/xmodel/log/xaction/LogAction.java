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
    setExpr = document.getExpression( "set", true);
    messageExpr = document.getExpression();
  }
  
  /**
   * Generate the log name from the location of the script element.
   * @param root The root of the script element.
   * @return Returns the generated log name.
   */
  static String createLogName( IModelObject root)
  {
    IModelObject script = root.getAncestor( "script");
    if ( script != null)
    {
      String name = Xlate.get( script, "name", (String)null);
      if ( name != null) return name;
    }
    
    IPath path = ModelAlgorithms.createIdentityPath( root);
    return path.toString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( logNameExpr != null) logName = logNameExpr.evaluateString( context);
    
    Log log = Log.getLog( logName);
    
    int level = Log.getLevelIndex( levelExpr.evaluateString( context));
    if ( setExpr == null)
    {
      if ( log.isLevelEnabled( level))
      {
        String message = messageExpr.evaluateString( context);
        log.log( level, message);
      }
    }
    else
    {
      log.setLevel( level);
    }
    
    return null;
  }

  private String logName;
  private IExpression logNameExpr;
  private IExpression levelExpr;
  private IExpression setExpr;
  private IExpression messageExpr;
}
