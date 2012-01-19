/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ValidateAction.java
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
import org.xmodel.IModelObjectFactory;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xsd.Schema;
import org.xmodel.xsd.check.SchemaError;

/**
 * An action which validates an element against its simplified schema.
 */
public class ValidateAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    IModelObject config = document.getRoot();
    factory = getFactory( config);
    var = Conventions.getVarName( config, true, "assign");    
    sourceExpr = document.getExpression( "source", false);
    schemaRootExpr = document.getExpression( "schema", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject schemaRoot = schemaRootExpr.queryFirst( context);
    if ( schemaRoot == null)
      throw new IllegalArgumentException( 
        "Schema root is null in ValidateAction: "+this);

    List<IModelObject> result = new ArrayList<IModelObject>();
    for( IModelObject document: sourceExpr.query( context, null))
    {
      List<SchemaError> errors = Schema.validate( schemaRoot, document, false);
      if ( errors != null)
        for( SchemaError error: errors)
        {
          IModelObject element = factory.createObject( null, "error");
          element.setValue( error.toString());
        }
    }
    
    IVariableScope scope = context.getScope();
    if ( scope == null)
      throw new IllegalArgumentException(
        "Action executed in scope which does not support variable assignment: "+this);
    
    scope.set( var, result);
    
    return null;
  }
  
  private IModelObjectFactory factory;
  private String var;
  private IExpression sourceExpr;
  private IExpression schemaRootExpr;
}
