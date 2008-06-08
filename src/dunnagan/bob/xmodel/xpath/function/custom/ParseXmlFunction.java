/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.function.custom;

import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.expression.ExpressionException;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.function.Function;

/**
 * A custom xpath function which takes a single string argument, parses it as xml and returns
 * the root of the xml fragment.  If an error occurs during parsing then an empty set is returned.
 */
public class ParseXmlFunction extends Function
{
  public final static String name = "parse-xml";
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#evaluateNodes(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 1, 1);
    assertType( context, ResultType.STRING);

    try
    {
      String string = getArgument( 0).evaluateString( context);
      IModelObject root = xmlIO.read( string);
      return Collections.singletonList( root);
    }
    catch( XmlException e)
    {
      throw new ExpressionException( this, "XML parsing error.", e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext, java.lang.String, java.lang.String)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    try
    {
      IModelObject oldRoot = xmlIO.read( oldValue);
      IModelObject newRoot = xmlIO.read( newValue);
      if ( differ.diff( oldRoot, newRoot, null))
      {
        getParent().notifyRemove( this, context, Collections.singletonList( oldRoot));
        getParent().notifyAdd( this, context, Collections.singletonList( newRoot));
      }
    }
    catch( XmlException e)
    {
      getParent().handleException( this, context, e);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#notifyChange(dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  public void notifyChange( IExpression expression, IContext context)
  {
    try
    {
      context.getModel().revert();
      String oldValue = expression.evaluateString( context);
      context.getModel().restore();
      String newValue = expression.evaluateString( context);
      notifyChange( expression, context, newValue, oldValue);
    }
    catch( ExpressionException e)
    {
      getParent().handleException( this, context, e);
    }
  }
  
  XmlIO xmlIO = new XmlIO();
  XmlDiffer differ = new XmlDiffer();
}
