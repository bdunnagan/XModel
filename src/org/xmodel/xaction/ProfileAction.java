/**
 * 
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which executes a script an stores the amount of time the script took to execute
 * in the variable defined by the <i>assign</i> attribute. Time is measured in nanoseconds.
 */
public class ProfileAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    IModelObject root = getDocument().getRoot();
    variable = Xlate.get( root, "assign", (String)null);
    
    // reuse ScriptAction to handle for script (must temporarily remove condition if present)
    Object when = root.removeAttribute( "when");
    script = document.createScript( "source");
    if ( when != null) root.setAttribute( "when", when);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    long t0 = System.nanoTime();
    
    Object[] result = script.run( context);
    
    long t1 = System.nanoTime();
    int elapsed = (int)(t1 - t0);
    
    // store elapsed time in variable
    IVariableScope scope = null;
    if ( scope == null)
    {
      scope = context.getScope();
      if ( scope == null)
        throw new IllegalArgumentException( 
          "ProfileAction context does not have a variable scope: "+this);
    }
    
    scope.set( variable, elapsed);
    
    return result;
  }

  private String variable;
  private ScriptAction script;
}
