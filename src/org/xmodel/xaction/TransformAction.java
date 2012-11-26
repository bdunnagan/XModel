/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TransformAction.java
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
import org.xmodel.INode;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which performs a bidirectional transform. When this action is invoked, the direction of
 * the transform must either be specified in the <i>direction</i> element, or it must be defined in 
 * the <i>transform_direction</i> variable in the context. The variable should be assigned one of the
 * two following strings: <i>fromLeft</i> or <i>fromRight</i>.
 */
public class TransformAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#configure(
   * com.stonewall.cornerstone.cpmi.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    fromLeftScript = document.createChildScript( "fromLeft");
    fromRightScript = document.createChildScript( "fromRight");

    List<INode> preNodes = new ArrayList<INode>();
    List<INode> postNodes = new ArrayList<INode>();
    boolean pre = true;
    for( INode child: document.getRoot().getChildren())
    {
      if ( child.isType( "fromLeft") || child.isType( "fromRight"))
      {
        pre = false;
        continue;
      }

      // ignore nested transforms and package declarations
      if ( child.isType( "transform") || child.isType( "package")) continue;

      // add child to appropriate list
      if ( pre) preNodes.add( child); else postNodes.add( child);
    }
    
    preActions = new ArrayList<IXAction>();
    for( INode node: preNodes)
    {
      IXAction action = document.getAction( node);
      if ( action != null) preActions.add( action);
    }
    
    postActions = new ArrayList<IXAction>();
    for( INode node: postNodes)
    {
      IXAction action = document.getAction( node);
      if ( action != null) postActions.add( action);
    }
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    IVariableScope scope = context.getScope();        
    Object value = scope.get( "transform_direction");
    if ( value == null)
    {
      throw new IllegalArgumentException( 
        "Transform direction must be defined in variable, $transform_direction:"+this);
    }

    // run pre-script
    for( IXAction action: preActions)
    {
      Object[] result = action.run( context);
      if ( result != null) return result;
    }

    // transform
    String direction = value.toString();
    if ( direction.equals( "fromLeft"))
    {
      if ( fromLeftScript != null) return fromLeftScript.run( context);
    }
    else if ( direction.equals( "fromRight"))
    {
      if ( fromRightScript != null) return fromRightScript.run( context);
    }
    else
    {
      throw new IllegalArgumentException( 
        "Transform direction value must be one of: \"fromLeft\", \"fromRight\": "+this);
    }

    // run post script
    for( IXAction action: postActions)
    {
      Object[] result = action.run( context);
      if ( result != null) return result;
    }
    
    return null;
  }
  
  private ScriptAction fromLeftScript;
  private ScriptAction fromRightScript;
  private List<IXAction> preActions;
  private List<IXAction> postActions;
}
