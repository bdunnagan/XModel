/**
 * 
 */
package org.xmodel.xpath.function;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;

/**
 * An implementation of the XPath 2.0 <code>fn:deep-equal</code> function (without the fn: prefix).
 * TODO: verify semantics against XPath 2.0 function.
 */
public class DeepEqualFunction extends Function
{
  public DeepEqualFunction()
  {
    differ = new XmlDiffer( new DefaultXmlMatcher( true));
  }
  
  public final static String name = "deep-equal";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.BOOLEAN;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    assertArgs( 2, 2);
    assertType( context, ResultType.NODES);
    
    List<IModelObject> nodes1 = getArgument( 0).evaluateNodes( context);
    List<IModelObject> nodes2 = getArgument( 1).evaluateNodes( context);
    
    // rule: non-equal list
    if ( nodes1.size() != nodes2.size()) return false;
    
    // rule: empty lists
    if ( nodes1.size() == 0) return true;
    
    // unsure if these semantics exactly match
    for( int i=0; i<nodes1.size(); i++)
    {
      if ( !differ.diff( nodes1.get( i), nodes2.get( i), null)) return false;
    }
    
    return true;
  }
  
  
  private XmlDiffer differ;
}
