/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * MakeDirAction.java
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

import java.io.File;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction that creates one or more directories.
 */
public class MakeDirAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    recurseExpr = document.getExpression( "recurse", true);
    pathExpr = document.getExpression( "path", true);
    if ( pathExpr == null) pathExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    File file = new File( pathExpr.evaluateString( context));
    if ( !file.exists()) 
    {
      if ( recurseExpr == null || !recurseExpr.evaluateBoolean( context))
      {
        file.mkdir();
      }
      else
      {
        file.mkdirs();
      }
    }
    
    return null;
  }

  private IExpression pathExpr;
  private IExpression recurseExpr;
}
