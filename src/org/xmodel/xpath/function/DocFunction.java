/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.external.CachingException;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;


/**
 * An implementation of the XPath 2.0 fn:doc function. This function is backed by the <code>query</code> method
 * of the <code>IModel</code> interface which takes a URI. The URI schemes which are handled by this method are
 * determined by which implementations of the IExternalSpace interface are registered with the model.
 */
public class DocFunction extends Function
{
  public final static String name = "doc";
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);

    try
    {
      String spec = getArgument( 0).evaluateString( context);
      URI uri = new URI( spec);
      List<IModelObject> result = context.getModel().query( uri);
      if ( result != null) return result;
      return Collections.emptyList();
    }
    catch( URISyntaxException e)
    {
      throw new ExpressionException( this, "Unable to evaluate doc function:", e);
    }
    catch( CachingException e)
    {
      throw new ExpressionException( this, "Unable to evaluate doc function:", e);
    }
  }
}