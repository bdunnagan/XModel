package org.xmodel.xpath.expression.nu;

import java.util.List;
import org.xmodel.xpath.expression.IContext;

/**
 * An interface for receiving notification when the result of expression has changed.
 * The listener is guaranteed to receive notifications only once per atomic update.
 * The interface provides two types of notification. Since many simple expressions
 * are only changed by the addition or removal of results for a given atomic update,
 * two methods are provided which give that information without incurring a 
 * performance penalty. The receiver can use this information to maintain an
 * unordered version of the result set.
 * <p>
 * When the expression tree cannot provide the above information, the second type
 * of notification is performed. This type of notification only indicates that the
 * expression has changed in some way. It is then the responsibility of the receiver
 * to reevaluate the expression. The state of the model is preserved at the point
 * that the notification method is called. This allows the expression to be 
 * reevaluated against the state of the model before the update was made, as well
 * as reevaluated against the state of the model after the update. The differencing
 * engine can be used to perform a shallow, ordered diff of the two result sets 
 * which will yield precise, ordered notification of the changes.
 */
public interface IExpressionListener
{
  /**
   * Called when one or more values are added to the result of the expression. XPath 2.0
   * results are ordered, but in some cases the receiver is not interested in the order.
   * This method will be called when this information is available at no performance cost.
   * The receiver can then opt to re-evaluate the expression before and after the update
   * to determine exactly how the sequence has changed.
   * @param expression The expression.
   * @param context The context.
   * @param added The results that were added.
   */
  public void notifyAdd( IExpression expression, IContext context, List<Object> added);
  
  /**
   * Called when one or more values are removed from the result of the expression. XPath 2.0
   * results are ordered, but in some cases the receiver is not interested in the order.
   * This method will be called when this information is available at no performance cost.
   * The receiver can then opt to re-evaluate the expression before and after the update
   * to determine exactly how the sequence has changed.
   * @param expression The expression.
   * @param context The context.
   * @param removed The results that were removed.
   */
  public void notifyRemove( IExpression expression, IContext context, List<Object> removed);
  
  /**
   * Called when the result of the expression has changed but no information about the change
   * is available without re-evaluating the expression. The receiver can choose the tradeoff
   * between detail and performance by choosing how to re-evaluate the expression.
   * @param expression The expression.
   * @param context The context.
   */
  public void notifyChange( IExpression expression, IContext context);
}
