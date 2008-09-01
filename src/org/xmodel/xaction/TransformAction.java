/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
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

    fromLeftScript = document.createScript( fromLeftActionExpr);
    fromRightScript = document.createScript( fromRightActionExpr);

    List<IModelObject> preNodes = new ArrayList<IModelObject>();
    List<IModelObject> postNodes = new ArrayList<IModelObject>();
    boolean pre = true;
    for( IModelObject child: document.getRoot().getChildren())
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
    
    preScript = document.createScript( preNodes);
    postScript = document.createScript( postNodes);
  }

  /* (non-Javadoc)
   * @see com.stonewall.cornerstone.cpmi.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    IVariableScope scope = context.getScope();        
    Object value = scope.get( "transform_direction");
    if ( value == null)
    {
      throw new IllegalArgumentException( 
        "Transform direction must be defined in variable, $transform_direction:"+this);
    }

    // run pre-script
    preScript.run( context);

    // transform
    String direction = value.toString();
    if ( direction.equals( "fromLeft"))
    {
      fromLeftScript.run( context);
    }
    else if ( direction.equals( "fromRight"))
    {
      fromRightScript.run( context);
    }
    else
    {
      throw new IllegalArgumentException( 
        "Transform direction value must be one of: \"fromLeft\", \"fromRight\": "+this);
    }

    // run post script
    postScript.run( context);
  }
  
  private final static IExpression fromLeftActionExpr = XPath.createExpression(
    "fromLeft/*");
  
  private final static IExpression fromRightActionExpr = XPath.createExpression(
    "fromRight/*");
  
  private ScriptAction fromLeftScript;
  private ScriptAction fromRightScript;
  private ScriptAction preScript;
  private ScriptAction postScript;
}
