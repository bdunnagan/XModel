package org.xmodel.script;

import org.xmodel.INode;
import org.xmodel.xpath.expression.IContext;

public interface IClassFactory
{
  /**
   * Compile a class from the specified element.
   * @param context The compilation context.
   * @param element An element containing a class definition.
   * @return Returns the compiled class.
   */
  public IClass compile( IContext context, INode element) throws CompileException;
  
  /**
   * Returns the class that was previously compiled, or is being compiled for the specified element.
   * @param element An element containing a class definition.
   * @return Returns null or the class.
   */
  public IClass classFor( INode element);
}
