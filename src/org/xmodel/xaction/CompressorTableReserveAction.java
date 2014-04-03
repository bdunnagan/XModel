package org.xmodel.xaction;

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
        for( IModelObject node: tagsExpr.evaluateNodes( context))
        {
          String tag = Xlate.get( node, (String)null);
          if ( tag != null) TabularCompressor.reserveGlobalTag( tag);
        }
        break;
      }
      
      case STRING:
      {
        String tags = tagsExpr.evaluateString( context);
        for( String tag: tags.split( "\\s*,\\s*"))
        {
          if ( tag != null) TabularCompressor.reserveGlobalTag( tag);
        }
        break;
      }
      
      default:
        break;
    }
    
    return null;
  }

  private IExpression tagsExpr;
}
