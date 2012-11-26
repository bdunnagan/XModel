package org.xmodel.script;

import org.xmodel.INode;
import org.xmodel.xpath.expression.IContext;

public interface IClass
{
  /**
   * Compile the specified element into a class.
   * @param context The compilation context.
   * @param factory The factory being used.
   * @param element An element containing a class definition.
   * @return Returns the compiled class.
   */
  public void compile( IContext context, IClassFactory factory, INode element) throws CompileException;
}
