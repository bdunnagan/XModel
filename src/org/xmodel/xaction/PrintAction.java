/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xml.XmlIO;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


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
    sourceExpr = document.getExpression( document.getRoot());
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    XmlIO xmlIO = new XmlIO();
    xmlIO.setOutputStyle( Style.printable);
    switch( sourceExpr.getType( context))
    {
      case NODES:
        List<IModelObject> sources = sourceExpr.query( context, null);
        for( IModelObject source: sources) System.out.println( xmlIO.write( source));
        break;
      
      case STRING:
        System.out.println( sourceExpr.evaluateString( context));
        break;
      
      case BOOLEAN:
        System.out.println( sourceExpr.evaluateBoolean( context));
        break;
      
      case NUMBER:
        System.out.println( sourceExpr.evaluateNumber( context));
        break;
    }
  }

  private IExpression sourceExpr;
}
