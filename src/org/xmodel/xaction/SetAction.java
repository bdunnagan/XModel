/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SetAction.java
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
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;


/**
 * An IXAction which sets the value of the nodes defined by the target expression to the string
 * result of the source expression.
 */
public class SetAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression( "source", true);
    targetExpr = document.getExpression( "target", true);
    
    // alternate source or target location
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    if ( targetExpr == null) targetExpr = document.getExpression();
    
    factory = Conventions.getFactory( document.getRoot());
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected Object[] doAction( IContext context)
  {
    // handle java.lang.Object transfer correctly
    Object value = null;
    if ( sourceExpr != null)
    {
      ResultType type = sourceExpr.getType( context);
      switch( type)
      {
        case NODES:
        {
          IModelObject node = sourceExpr.queryFirst( context);
          if ( node != null) value = node.getValue();
          break;
        }
        
        case BOOLEAN: value = sourceExpr.evaluateBoolean( context); break;
        case STRING:  value = sourceExpr.evaluateString( context); break;
        case NUMBER:  value = sourceExpr.evaluateNumber( context); break;
        default: break;
      }
    }
    
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) 
    {
      ModelAlgorithms.createPathSubtree( context, targetExpr, factory, null, value);
    }
    else
    {
      for( IModelObject target: targets) 
        target.setValue( value);
    }
    
    return null;
  }

  private IModelObjectFactory factory;
  private IExpression sourceExpr;
  private IExpression targetExpr;
}
