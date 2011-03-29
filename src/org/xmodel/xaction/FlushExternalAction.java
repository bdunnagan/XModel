/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FlushExternalAction.java
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
import org.xmodel.log.Log;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which calls the <code>flush</code> method of the ICachingPolicy associated
 * with each element identified by the source expression.
 * @deprecated Use FlushAction instead.
 */
public class FlushExternalAction extends GuardedAction
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
          ((IExternalReference)source).flush();
        }
        catch( Exception e)
        {
          System.err.println( "Unable to flush caching policy for reference: "+source);
          log.exception( e);
        }
      }
    }
    
    return null;
  }

  private static Log log = Log.getLog( "org.xmodel.xaction");
  
  private IExpression sourceExpr;
}
