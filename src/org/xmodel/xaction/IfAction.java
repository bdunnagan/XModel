/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IfAction.java
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
import java.util.Iterator;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class IfAction extends CompoundAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.CompoundAction#configure(org.xmodel.xaction.XActionDocument, java.util.Iterator)
   */
  @Override
  public void configure( XActionDocument document, Iterator<IModelObject> iterator)
  {
    while( iterator.hasNext())
    {
      IModelObject element = iterator.next();
      if ( element.isType( "elseif"))
      {
        if ( elseIfActions == null) elseIfActions = new ArrayList<ElseifAction>( 1);
        ElseifAction action = (ElseifAction)document.getAction( element);
        if ( action != null) elseIfActions.add( action);
      }
      else if ( element.isType( "else"))
      {
        elseAction = document.createScript( element);
      }
      else
      {
        break;
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    condition = document.getExpression( "true", true);
    if ( condition == null)
    {
      condition = document.getExpression( "test", true);
      if ( condition == null)
      {
        condition = document.getExpression( "false", true);
        negate = true;
      }
    }

    script = document.createScript( "true", "false", "test");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.XAction#doRun(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public Object[] doRun( IContext context)
  {
    boolean test = condition.evaluateBoolean( context);
    if ( negate ^ test) 
    {
      return script.run( context);
    }
    else
    {
      if ( elseIfActions != null)
      {
        for( ElseifAction elseIfAction: elseIfActions)
        {
          ElseifAction.Result result = elseIfAction.runTest( context);
          if ( result.test) return result.returned; 
        }
      }
      
      if ( elseAction != null) return elseAction.run( context);
    }
    
    return null;
  }

  protected IExpression condition;
  protected ScriptAction script;
  protected boolean negate;
  private List<ElseifAction> elseIfActions;
  private IXAction elseAction;
}
