package org.xmodel.script;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

public interface IScript
{
  /**
   * Compile a script from the specified element.
   * @param context The compilation context.
   * @param factory The script factory.
   * @param element The element.
   */
  public void compile( IContext context, IScriptFactory factory, IModelObject element) throws CompileException;
  
  /**
   * Execute this script.
   * @param context The execution context.
   * @return Returns the result of the script.
   */
  public Object execute( IContext context) throws ExecuteException;
}
