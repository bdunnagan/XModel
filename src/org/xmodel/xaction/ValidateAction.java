/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.Xlate;
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
    factory = getFactory( document.getRoot());
    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    sourceExpr = document.getExpression( "source", false);
    schemaRootExpr = document.getExpression( "schema", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
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
    
    scope.set( variable, result);
  }
  
  private IModelObjectFactory factory;
  private String variable;
  private IExpression sourceExpr;
  private IExpression schemaRootExpr;
}
