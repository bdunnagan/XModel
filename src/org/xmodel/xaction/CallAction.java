package org.xmodel.xaction;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class CallAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    var = Conventions.getVarName( document.getRoot(), false);
    argExprs = document.getExpressions( "args", true);
    scriptExpr = document.getExpression();
    if ( scriptExpr == null) scriptExpr = document.getExpression( "script", true);
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    IXAction script = Conventions.getScript( document, getScriptNode( context));
    if ( script == null) return null;
    
    StatefulContext nested = new StatefulContext( context);
    passVariables( context, nested, script);
    
    Object[] result = script.run( nested);
    if ( var != null && result != null && result.length > 0)
    {
      context.getScope().set( var, result[ 0]);
    }
    
    return null;
  }

  /**
   * Get the script node to be executed.
   * @param context The context.
   * @return Returns null or the script node.
   */
  private IModelObject getScriptNode( IContext context)
  {
    if ( scriptExpr != null) return scriptExpr.queryFirst( context);
    
    if ( inline == null)
    {
      inline = new ModelObject( "script");
      ModelAlgorithms.copyChildren( document.getRoot(), inline, null);
    }
    return inline;
  }
  
  /**
   * Pass variables to script.
   * @param context The calling context.
   * @param nested The nested execution context.
   * @param script The script.
   */
  private void passVariables( IContext context, StatefulContext nested, IXAction script)
  {
    if ( script instanceof ScriptAction)
    {
      String[] inVars = ((ScriptAction)script).getInVars();
      if ( inVars != null)
      {
        for( int i=0; i<argExprs.size(); i++)
        {
          if ( i == inVars.length) break;
          
          IExpression argExpr = argExprs.get( i);
          switch( argExpr.getType( context))
          {
            case NODES:   nested.set( inVars[ i], argExpr.evaluateNodes( context)); break;
            case STRING:  nested.set( inVars[ i], argExpr.evaluateString( context)); break;
            case NUMBER:  nested.set( inVars[ i], argExpr.evaluateNumber( context)); break;
            case BOOLEAN: nested.set( inVars[ i], argExpr.evaluateBoolean( context)); break; 
            default:      break;
          }
        }
      }
    }
  }

  private String var;
  private List<IExpression> argExprs;
  private IExpression scriptExpr;
  private IModelObject inline;
}
