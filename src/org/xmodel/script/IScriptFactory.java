package org.xmodel.script;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;

/**
 * Instances of IScriptFactory determine how script elements are compiled.  For instance, a debugger can
 * be implemented by specify a factory that delegates to another factory, but wraps each script within a
 * debugging script.  Or, a factory can generate code in a different language such as javascript.  In any
 * case, the factory must respect the semantics defined by the script element.
 */
public interface IScriptFactory
{
  /**
   * Compile the specified element.
   * @param context The compilation context.
   * @param element The element.
   * @return Returns the compiled script.
   */
  public IScript compile( IContext context, IModelObject element) throws CompileException;
  
  /**
   * Compile the specified elements.
   * @param context The compilation context.
   * @param elements The elements.
   * @return Returns the compiled script.
   */
  public IScript compile( IContext context, List<IModelObject> elements) throws CompileException;
  
  /**
   * Returns the script that was previously, or is currently, being compiled for the specifed element.
   * @param element The element.
   * @return Returns null or the script associated with the element.
   */
  public IScript getScriptByElement( IModelObject element);
}
