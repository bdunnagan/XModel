/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ForAction.java
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
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * An XAction which executes another action once with each context from a node-set expression.
 * The current node being iterated can be accessed either through the assigned variable or 
 * through the context (if the <i>assign</i> attribute is not specified). In the latter case,
 * a new StatefulContext is created for each iteration and variable assignments within the
 * for loop will be local to the for loop.
 */
public class ForAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // node-set iteration
    IModelObject root = document.getRoot();
    variable = Xlate.get( root, "assign", (String)null);    
    sourceExpr = document.getExpression( "source", true);

    // OR numeric iteration
    fromExpr = document.getExpression( "from", true);
    toExpr = document.getExpression( "to", true);
    byExpr = document.getExpression( "by", true);
        
    // reuse ScriptAction to handle for script (must temporarily remove condition if present)
    Object when = root.removeAttribute( "when");
    script = document.createScript( "source");
    if ( when != null) root.setAttribute( "when", when);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected Object[] doAction( IContext context)
  {
    IVariableScope scope = null;
    if ( scope == null)
    {
      scope = context.getScope();
      if ( scope == null)
        throw new IllegalArgumentException( 
          "ForAction context does not have a variable scope: "+this);
    }
    
    // node-set iteration 
    if ( sourceExpr != null)
    {
      List<IModelObject> nodes = sourceExpr.evaluateNodes( context);
      for( int i=0; i<nodes.size(); i++)
      {
        // store the current element in either a variable or the context
        if ( variable != null) scope.set( variable, nodes.get( i));
        else context = new StatefulContext( context, nodes.get( i), i+1, nodes.size());
        
        Object[] result = script.run( context);
        if ( result != null) return result;
      }
    }
    
    // numeric iteration
    if ( variable != null && fromExpr != null && toExpr != null && byExpr != null)
    {
      double from = fromExpr.evaluateNumber( context);
      double to = toExpr.evaluateNumber( context);
      double by = byExpr.evaluateNumber( context);
      if ( by >= 0)
      {
        for( double i = from; i <= to; i += by)
        {
          scope.set( variable, i);
          Object[] result = script.run( context);
          if ( result != null) return result;
        }
      }
      else
      {
        for( double i = from; i >= to; i += by)
        {
          scope.set( variable, i);
          Object[] result = script.run( context);
          if ( result != null) return result;
        }
      }
    }
    
    return null;
  }

  private String variable;
  private IExpression sourceExpr;
  private IExpression fromExpr;
  private IExpression toExpr;
  private IExpression byExpr;
  private ScriptAction script;
}
