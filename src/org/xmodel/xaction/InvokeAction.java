/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.expression.IExpression.ResultType;

/**
 * An XAction that provides improved semantics for invoking scripts.
 */
public class InvokeAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    IModelObject node = document.getRoot().getAttributeNode( "script");
    if ( node == null) node = document.getRoot().getFirstChild( "script");
    script = document.createScript( node);
    
    List<IModelObject> argNodes = document.getRoot().getChildren( "arg");
    argExprs = new IExpression[ argNodes.size()];
    for( int i=0; i<argExprs.length; i++)
    {
      argExprs[ i] = Xlate.get( argNodes.get( i), (IExpression)null);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    StatefulContext local = new StatefulContext( context, context.getObject());
    
    // evaluate args
    for( int i=0; i<argExprs.length; i++)
    {
      ResultType type = argExprs[ i].getType( context);
      switch( type)
      {
        case NODES: local.set( "arg"+i, argExprs[ i].evaluateNodes( context)); break;
        case NUMBER: local.set( "arg"+i, argExprs[ i].evaluateNumber( context)); break;
        case STRING: local.set( "arg"+i, argExprs[ i].evaluateString( context)); break;
        case BOOLEAN: local.set( "arg"+i, argExprs[ i].evaluateBoolean( context)); break;
      }
    }
    
    script.run( local);
  }
  
  private ScriptAction script;
  private IExpression[] argExprs;
}
