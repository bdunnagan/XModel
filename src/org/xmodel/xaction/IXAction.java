/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * An interface for the implementation of an action defined in a viewmodel.
 */
public interface IXAction
{
  /**
   * Set the document from which this action will be configured.
   * @param document The document.
   */
  public void setDocument( XActionDocument document);
  
  /**
   * Returns the viewmodel for this action.
   * @return Returns the viewmodel for this action.
   */
  public XActionDocument getDocument();
  
  /**
   * Configure the action from the viewmodel.
   * @param document The viewmodel.
   */
  public void configure( XActionDocument document);

  /**
   * Run the specified action given its viewmodel. If the return value is non-null then this
   * will be the last action that is run before returning to the enclosing InvokeAction 
   * (except for actions defined in the finally clause of TryAction).
   * @param context The adapter context.
   * @return Returns null or the return value.
   */
  public Object[] run( IContext context);
  
  /**
   * Run this action with an arbitrary context and return its variable scope.
   * @return Returns the variable scope of the context so variables can be examined.
   */
  public IVariableScope run();
}
