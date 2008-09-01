/*
 * Stonewall Networks, Inc.
 *
 *   Project: XModel
 *   Author:  bdunnagan
 *   Date:    Apr 28, 2008
 *
 * Copyright 2008.  Stonewall Networks, Inc.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.external.caching.AnnotationTransform;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * An XAction which executes the AnnotationTransform on one or more elements.
 */
public class BuildReferencesAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    factory = getFactory( document.getRoot());
    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    sourceExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    AnnotationTransform transform = new AnnotationTransform();
    transform.setFactory( factory);
    transform.setClassLoader( document.getClassLoader());
    transform.setParentContext( context);

    List<IModelObject> result = new ArrayList<IModelObject>();
    for( IModelObject source: sourceExpr.query( context, null))
    {
      IModelObject transformed = transform.transform( source);
      result.add( transformed);
    }
    
    IVariableScope scope = context.getScope();
    if ( scope != null) scope.set( variable, result);
  }
  
  private String variable;
  private IExpression sourceExpr;
  private IModelObjectFactory factory;
}
