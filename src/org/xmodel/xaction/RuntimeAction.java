package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;

/**
 * An XAction that returns information about the Java runtime. 
 */
public class RuntimeAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    var = Conventions.getVarName( document.getRoot(), true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject runtimeNode = new ModelObject( "runtime");
    Xlate.set( runtimeNode, "timestamp", System.currentTimeMillis());

    Runtime runtime = Runtime.getRuntime();
    IModelObject memoryNode = runtimeNode.getCreateChild( "memory");
    Xlate.set( memoryNode, "free", runtime.freeMemory());
    Xlate.set( memoryNode, "total", runtime.totalMemory());
    Xlate.set( memoryNode, "max", runtime.maxMemory());

    context.set( var, runtimeNode);
    
    return null;
  }
  
  private String var;
}
