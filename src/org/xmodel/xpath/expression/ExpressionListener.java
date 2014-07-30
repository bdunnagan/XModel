/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ExpressionListener.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xpath.expression;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.GlobalSettings;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.diff.ListDiffer;
import org.xmodel.diff.ListDiffer.Change;
import org.xmodel.log.Log;

/**
 * An implementation of IExpressionListener with empty method stubs except for the indeterminate
 * callback method <code>notifyChange( IExpression, IContext)</code> which reevaluates the 
 * expression once with the last update reverted and once with the update restored so that it
 * can call one of the other notification methods.
 */
public class ExpressionListener implements IExpressionListener
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext)
   */
  public void notifyChange( IExpression expression, IContext context)
  {
    try
    {
      IModel model = GlobalSettings.getInstance().getModel();
      switch( expression.getType( context))
      {
        case NODES:
        {
          // revert and reevaluate
          model.revert();
          List<IModelObject> oldNodes = expression.evaluateNodes( context);
  
          // restore and reevaluate
          model.restore();
          List<IModelObject> newNodes = expression.evaluateNodes( context);

          // diff
          List<IModelObject> inserts = null;
          List<IModelObject> deletes = null;
          
          ListDiffer differ = new ListDiffer();
          differ.diff( oldNodes, newNodes);
          List<Change> changes = differ.getChanges();
          for( Change change: changes)
          {
            if ( change.rIndex >= 0)
            {
              if ( inserts == null) inserts = new ArrayList<IModelObject>();
              for( int i=0; i<change.count; i++)
                inserts.add( newNodes.get( change.rIndex + i));
            }
            else
            {
              if ( deletes == null) deletes = new ArrayList<IModelObject>();
              for( int i=0; i<change.count; i++)
                deletes.add( oldNodes.get( change.lIndex + i));
            }
          }
          
          // notify nodes removed
          if ( deletes != null) notifyRemove( expression, context, deletes);
          
          // notify nodes added
          if ( inserts != null) notifyAdd( expression, context, inserts);
        }
        break;
        
        case NUMBER:
        {
          // revert and reevaluate
          model.revert();
          double oldValue = expression.evaluateNumber( context);
  
          // restore and reevaluate
          model.restore();
          double newValue = expression.evaluateNumber( context);
          
          if ( newValue != oldValue) notifyChange( expression, context, newValue, oldValue);
        }
        break;
        
        case BOOLEAN:
        {
          // revert and reevaluate
          model.revert();
          boolean oldValue = expression.evaluateBoolean( context);
  
          // restore and reevaluate
          model.restore();
          boolean newValue = expression.evaluateBoolean( context);
          
          if ( newValue != oldValue) notifyChange( expression, context, newValue);
        }
        break;
        
        case STRING:
        {
          // revert and reevaluate
          model.revert();
          String oldValue = expression.evaluateString( context);
  
          // restore and reevaluate
          model.restore();
          String newValue = expression.evaluateString( context);
          
          if ( !newValue.equals( oldValue)) notifyChange( expression, context, newValue, oldValue);
        }
        break;
        
        default: break;
      }      
    }
    catch( ExpressionException e)
    {
      handleException( expression, context, e);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#requiresValueNotification()
   */
  public boolean requiresValueNotification()
  {
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#notifyValue(
   * org.xmodel.xpath.expression.IExpression, java.util.Collection, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    // legacy semantic: convert value notification to string notification.
    // this may be okay since an implicit cast for the purpose of notification is not ambiguous
    for( IContext context: contexts)
    {
      String newResult = (newValue != null)? newValue.toString(): "";
      String oldResult = (oldValue != null)? oldValue.toString(): "";
      notifyChange( expression, context, newResult, oldResult);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpressionListener#handleException(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.Exception)
   */
  public void handleException( IExpression expression, IContext context, Exception e)
  {
    System.err.println( "Expression Error: "+expression+", "+context);
    log.exception( e);
  }
  
  private static Log log = Log.getLog( ExpressionListener.class);
}
