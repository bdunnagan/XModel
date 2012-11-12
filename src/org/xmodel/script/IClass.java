package org.xmodel.script;

import org.xmodel.IModelObject;
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
  public void compile( IContext context, IClassFactory factory, IModelObject element) throws CompileException;
}
