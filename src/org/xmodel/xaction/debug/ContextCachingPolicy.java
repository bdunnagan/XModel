package org.xmodel.xaction.debug;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.external.AbstractCachingPolicy;
import org.xmodel.external.CachingException;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An ICachingPolicy that accesses the content of a context variable on demand.
 */
public class ContextCachingPolicy extends AbstractCachingPolicy
{
  public ContextCachingPolicy( IContext context)
  {
    super( new UnboundedCache());
    this.context = context;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void sync( IExternalReference reference) throws CachingException
  {
    IVariableScope scope = context.getScope();
    String variable = reference.getID();
    IModelObject element = new ModelObject( reference.getType(), variable);
    switch( scope.getType( variable))
    {
      case NODES:
      {
        List<IModelObject> nodes = (List<IModelObject>)scope.get( variable); 
        for( IModelObject node: nodes)
        {
          element.addChild( node.cloneTree());
        }
      }
      break;
      
      case STRING:
      case NUMBER:
      case BOOLEAN: 
      {
        element.setValue( scope.get( variable));
      } 
      break;
    }
    
    update( reference, element);
  }
  
  private IContext context;
}
