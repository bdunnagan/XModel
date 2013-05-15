/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XAction.java
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
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.xaction.debug.Debugger;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * Abstract base class that implements XAction debugging semantics.
 */
public abstract class XAction implements IXAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    this.document = document;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#run()
   */
  public IVariableScope run()
  {
    StatefulContext context = new StatefulContext( new ModelObject( "root"));
    run( context);
    return context.getScope();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public final Object[] run( IContext context)
  {
    return doRun( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public abstract Object[] doRun( IContext context);
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#setDocument(org.xmodel.xaction.XActionDocument)
   */
  public final void setDocument( XActionDocument document)
  {
    this.document = document;
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#getViewModel()
   */
  public final XActionDocument getDocument()
  {
    if ( document == null) document = new XActionDocument();
    return document;
  }
  
  /**
   * @return Returns the debugger or null.
   */
  public final static synchronized Debugger getDebugger()
  {
    return debugger;
  }
  
  /**
   * @return Returns true if a debugger is present.
   */
  public final static boolean isDebugging()
  {
    return debugger != null;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    XActionDocument document = getDocument();
    if ( document == null) return "(No document)";
    
    IModelObject root = document.getRoot();
    if ( root == null) return "(No root)";

    IPath path = ModelAlgorithms.createIdentityPath( root, true);
    return path.toString(); 
  }

  private final static Debugger debugger = (System.getProperty( "xaction.debug") != null)? new Debugger(): null;
  
  protected XActionDocument document;
}
