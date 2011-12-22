/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FlushAction.java
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

import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that flushes changes made to an IExternalReference.
 */
public class FlushAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    sourceExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    for( IModelObject source: sourceExpr.query( context, null))
    {
      if ( source instanceof IExternalReference)
      {
        try
        {
          IExternalReference reference = (IExternalReference)source;
        }
        catch( Exception e)
        {
          throw new XActionException( String.format( "Unable to flush changes to reference, %s.", source.getType()), e);
        }
      }
    }
    
    return null;
  }
  
  private IExpression sourceExpr;
}
