/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;
import dunnagan.bob.xmodel.xsd.Schema;
import dunnagan.bob.xmodel.xsd.check.SchemaError;

/**
 * An action which validates an element against its simplified schema.
 */
public class ValidateAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.GuardedAction#configure(dunnagan.bob.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    sourceExpr = document.getExpression( "source", false);
    schemaRootExpr = document.getExpression( "schema", false);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
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
          IModelObject element = new ModelObject( "error");
          element.setValue( error.toString());
        }
    }
    
    IVariableScope scope = context.getScope();
    if ( scope == null)
      throw new IllegalArgumentException(
        "Action executed in scope which does not support variable assignment: "+this);
    
    scope.set( variable, result);
  }
  
  private String variable;
  private IExpression sourceExpr;
  private IExpression schemaRootExpr;
}
