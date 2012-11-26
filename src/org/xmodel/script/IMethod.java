package org.xmodel.script;

import org.xmodel.INode;
import org.xmodel.xpath.expression.IContext;

public interface IMethod
{
  /**
   * Compile a method from the specified element.
   * @param context The compilation context.
   * @param factory The factory being used.
   * @param element The element.
   */
  public void compile( IContext context, IMethodFactory factory, INode element) throws CompileException;
  
  /**
   * Execute this method.
   * @param context The execution context.
   * @return Returns the result of the script.
   */
  public Object execute( IContext context) throws ExecuteException;
}
