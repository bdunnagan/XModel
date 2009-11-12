/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * BuildReferencesAction.java
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
  protected Object[] doAction( IContext context)
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
    
    return null;
  }
  
  private String variable;
  private IExpression sourceExpr;
  private IModelObjectFactory factory;
}
