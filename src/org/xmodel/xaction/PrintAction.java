/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PrintAction.java
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

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xml.XmlIO;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


/**
 * An IXAction which outputs the xml representation of one or more elements for debugging.
 */
public class PrintAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    style = Xlate.get( document.getRoot(), "style", "printable");
    sourceExpr = document.getExpression( document.getRoot());
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected Object[] doAction( IContext context)
  {
    XmlIO xmlIO = new XmlIO();
    xmlIO.setOutputStyle( Style.valueOf( style));
    StringBuilder sb = new StringBuilder();
    switch( sourceExpr.getType( context))
    {
      case NODES:
        List<IModelObject> sources = sourceExpr.query( context, null);
        for( IModelObject source: sources) 
        {
          sb.append( xmlIO.write( source));
        }
        
        int last = sb.length() - 1;
        if ( sb.charAt( last) == '\n') sb.setLength( last);
        break;
      
      case STRING:
        sb.append( sourceExpr.evaluateString( context));
        break;
      
      case BOOLEAN:
        sb.append( sourceExpr.evaluateBoolean( context));
        break;
      
      case NUMBER:
        sb.append( sourceExpr.evaluateNumber( context));
        break;
    }
    
    if ( variable != null && context instanceof StatefulContext)
    {
      ((StatefulContext)context).set( variable, sb.toString());
    }
    else
    {
      System.out.println( sb.toString());
    }
    
    return null;
  }

  private String variable;
  private String style;
  private IExpression sourceExpr;
}
