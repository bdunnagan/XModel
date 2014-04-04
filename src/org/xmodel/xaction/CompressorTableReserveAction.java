package org.xmodel.xaction;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class CompressorTableReserveAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    tagsExpr = document.getExpression( "tags", true);
    if ( tagsExpr == null) tagsExpr = document.getExpression();
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    switch( tagsExpr.getType( context))
    {
      case NODES: 
      {
        List<IModelObject> nodes = tagsExpr.evaluateNodes( context);
        String[] tags = new String[ nodes.size()];
        for( int i=0; i<nodes.size(); i++)
          tags[ i] = Xlate.get( nodes.get( i), (String)null);
        TabularCompressor.setImplicitTable( tags);
        break;
      }
      
      case STRING:
      {
        String tags = tagsExpr.evaluateString( context);
        TabularCompressor.setImplicitTable( tags.split( "\\s*,\\s*"));
        break;
      }
      
      default:
        break;
    }
    
    return null;
  }

  private IExpression tagsExpr;
}
