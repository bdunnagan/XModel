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
import org.xmodel.xpath.expression.IExpression.ResultType;

/**
 * An xaction that returns from within a script executed with the InvokeAction. One or more
 * values can be returned per the contract of the invocation.
 */
public class ReturnAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    List<IModelObject> resultNodes = document.getRoot().getChildren( "result");
    resultExprs = new IExpression[ resultNodes.size()];
    for( int i=0; i<resultExprs.length; i++)
    {
      resultExprs[ i] = Xlate.get( resultNodes.get( i), (IExpression)null);
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
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
    
  }
  
  private IExpression[] resultExprs;
}
