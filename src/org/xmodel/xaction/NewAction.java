package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that can create an instance of any class that implements the org.xmodel.IConfigurable interface.
 */
public class NewAction extends GuardedAction
{
  /**
   * An interface for classes that can be created and configured with NewAction.
   */
  public interface IConfigurable
  {
    /**
     * Configure the new class with the specified configuration element.
     * @param element The configuration element.
     */
    public void configure( IModelObject element);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    var = Conventions.getVarName( document.getRoot(), true);
    classNameExpr = document.getExpression( "class", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String className = classNameExpr.evaluateString( context);
    try
    {
      Class<?> clazz = getClass().getClassLoader().loadClass( className);
      
      Object instance = clazz.newInstance();
      ((IConfigurable)instance).configure( document.getRoot());
      
      Conventions.putCache( context, var, instance);
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    return null;
  }

  private String var;
  private IExpression classNameExpr;
}
