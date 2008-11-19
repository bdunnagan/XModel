/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
  protected void doAction( IContext context)
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
          sb.append( "\n");
        }
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
  }

  private String variable;
  private String style;
  private IExpression sourceExpr;
}
