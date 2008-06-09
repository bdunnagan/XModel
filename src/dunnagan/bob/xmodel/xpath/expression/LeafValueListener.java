/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelListener;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelListener;
import dunnagan.bob.xmodel.ModelListenerList;

/**
 * An IModelListener which provides notification when the value of a leaf of a PathExpression or
 * VariableExpression has changed. Expressions which depend on the value of a node in a
 * PathExpression or VariableExpression need to install a listener to detect when the value of the
 * node changes. For example, in the following expression, the first node in the node-set of the
 * left-hand-side of the equality expression will be converted to a string as if by calling the
 * string function:
 * <p>
 * step[ lhs = '1']
 * <p>
 * If the value of the first node in the node-set <i>lhs</i> changes then the result of the
 * expression will change.
 * <p>
 * This class contains a single expression whose parent will receive notification when the value
 * of the target is updated.  In some cases an expression tree may contain two expressions which
 * install a LeafValueListener on the same node resulting in two notification paths when the value
 * of the node changes.  However, the framework prevents duplicate notifications from reaching the
 * end listener (See ExpressionListener).
 */
public class LeafValueListener extends ModelListener
{
  /**
   * Create a LeafValueListener for the specified expression.
   * @param expression The expression.
   * @param context The context.
   */
  public LeafValueListener( IExpression expression, IContext context)
  {
    this.expression = expression;
    this.context = context;
  }
   
  /**
   * Returns the context in which this listener was installed.
   * @return Returns the context in which this listener was installed.
   */
  public IContext getContext()
  {
    return context;
  }
  
  /**
   * Returns the expression which installed this listener.
   * @return Returns the expression which installed this listener.
   */
  public IExpression getExpression()
  {
    return expression;
  }
  
  /**
   * Find the LeafValueListener which has the specified expression and context.
   * @param object The object.
   * @param expression The expression.
   * @param context The context.
   * @return Returns the LeafValueListener found or null.
   */
  static public LeafValueListener findListener( IModelObject object, IExpression expression, IContext context)
  {
    ModelListenerList listeners = object.getModelListeners();
    if ( listeners == null) return null;
    for( IModelListener listener: listeners.getListeners())
      if ( listener instanceof LeafValueListener)
      {
        LeafValueListener leafValueListener = (LeafValueListener)listener;
        if ( leafValueListener.expression.equals( expression) && leafValueListener.context.equals( context))
          return leafValueListener;
      }
    return null;
  }
  
  /**
   * Find all the LeafValueListeners which have the specified expression.
   * @param object The object.
   * @param expression The expression.
   * @return Returns a list (possibly empty) of LeafValueListeners.
   */
  static public List<LeafValueListener> findListeners( IModelObject object, IExpression expression)
  {
    List<LeafValueListener> result = new ArrayList<LeafValueListener>();
    ModelListenerList listeners = object.getModelListeners();
    if ( listeners == null) return result;
    for( IModelListener listener: listeners.getListeners())
      if ( listener instanceof LeafValueListener)
      {
        LeafValueListener leafValueListener = (LeafValueListener)listener;
        if ( leafValueListener.expression.equals( expression))
          result.add( leafValueListener);
      }
    return result;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyChange(
   * dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.Object, java.lang.Object)
   */
  public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
  {
    if ( attrName.length() > 0) return;
    
    // context array is passed here for legacy reasons
    IContext[] contexts = { context};
    expression.notifyValue( expression, contexts, object, newValue, oldValue);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyClear(
   * dunnagan.bob.xmodel.IModelObject, java.lang.String, java.lang.Object)
   */
  public void notifyClear( IModelObject object, String attrName, Object oldValue)
  {
    if ( attrName.length() > 0) return;
    if ( oldValue != null)
    {
      // context array is passed here for legacy reasons
      IContext[] contexts = { context};
      expression.notifyValue( expression, contexts, object, "", oldValue.toString());
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ModelListener#notifyDirty(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  @Override
  public void notifyDirty( IModelObject object, boolean dirty)
  {
    // resync if necessary
    if ( dirty) object.getValue();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "{ "); sb.append( expression); sb.append( "}, ");
    sb.append( context);
    return sb.toString();
  }
  
  private IExpression expression;
  private IContext context;
}
