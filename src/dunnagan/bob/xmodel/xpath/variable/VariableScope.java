/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.variable;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelObject;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.Context;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An extension of AbstractVariableScope which stores its scope name and precedence.
 */
public class VariableScope extends AbstractVariableScope
{
  /**
   * Create a VariableScope.
   * @param scopeName The name of the scope.
   * @param precedence The precedence of the scope.
   */
  public VariableScope( String scopeName, int precedence)
  {
    this.scopeName = scopeName;
    this.precedence = precedence;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableScope#getPrecedence()
   */
  public int getPrecedence()
  {
    return precedence;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableScope#getName()
   */
  public String getName()
  {
    return scopeName;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.variable.IVariableScope#cloneOne()
   */
  public IVariableScope cloneOne()
  {
    VariableScope clone = new VariableScope( scopeName, precedence);
    if ( variables != null) clone.copyFrom( this);
    return clone;
  }

  private int precedence;
  private String scopeName;
  
  public static void main( String[] args) throws Exception
  {
    VariableScope scope = new VariableScope( "global", 0);
    IExpression e = XPath.createExpression( "/r/x");

    scope.define( "v", e);
    
    IModelObject root = new ModelObject( "r");
    IContext context = new Context( root);
    scope.addListener( "v", context, new IVariableListener() {
      public void notifyAdd( String name, IVariableScope scope, IContext context, List<IModelObject> nodes)
      {
        System.out.println( "update: name="+name+", scope="+scope.getName()+", add="+nodes);
      }
      public void notifyChange( String name, IVariableScope scope, IContext context, Boolean newValue)
      {
      }
      public void notifyChange( String name, IVariableScope scope, IContext context, Number newValue, Number oldValue)
      {
        System.out.println( "update: name="+name+", scope="+scope.getName()+", new="+newValue+", old="+oldValue);
      }
      public void notifyChange( String name, IVariableScope scope, IContext context, String newValue, String oldValue)
      {
        System.out.println( "update: name="+name+", scope="+scope.getName()+", new="+newValue+", old="+oldValue);
      }
      public void notifyRemove( String name, IVariableScope scope, IContext context, List<IModelObject> nodes)
      {
        System.out.println( "update: name="+name+", scope="+scope.getName()+", del="+nodes);
      }
    });
    
    root.getCreateChild( "x");
    root.removeChildren( "x");
  }
}
