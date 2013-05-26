/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AssignAction.java
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

import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * Evaluate an expression and optionally assign the result to a variable.  EvalAction is similar
 * to AssignAction, but it only supports assignment from the result of an expression.
 */
public class EvalAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    IModelObject config = document.getRoot();
    var = Conventions.getVarName( config, false);

    sourceExpr = document.getExpression();
    if ( sourceExpr == null) sourceExpr = document.getExpression( "source", true);
    
    // flags
    append = Xlate.get( config, "append", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    IVariableScope scope = (var != null)? context.getScope(): null;

    switch( sourceExpr.getType( context))
    {
      case NODES: 
      {
        setVariable( scope, sourceExpr.evaluateNodes( context)); 
        break;
      }
        
      case STRING:
      {
        String result = sourceExpr.evaluateString( context);
        if ( scope != null) scope.set( var, result); 
        break;
      }
        
      case NUMBER:
      {
        double result = sourceExpr.evaluateNumber( context);
        if ( scope != null) scope.set( var, result); 
        break;
      }
        
      case BOOLEAN: 
      {
        boolean result = sourceExpr.evaluateBoolean( context);
        if ( scope != null) scope.set( var, result); 
        break;
      }
        
      case UNDEFINED: throw new XActionException( "Expression type is undefined: "+sourceExpr);
    }
    
    return null;
  }
  
  /**
   * Replace or append variable node-set depending on the <i>append</i> flag.
   * @param scope The variable scope.
   * @param list The new elements.
   */
  @SuppressWarnings("unchecked")
  private void setVariable( IVariableScope scope, List<IModelObject> list)
  {
    if ( scope == null) return;
    
    if ( append)
    {
      List<IModelObject> oldList = (List<IModelObject>)scope.get( var);
      List<IModelObject> newList = new ArrayList<IModelObject>( oldList);
      newList.addAll( list);
    }
    else
    {
      scope.set( var, list);
    }
  }
  private String var;
  private boolean append;
  private IExpression sourceExpr;
}
