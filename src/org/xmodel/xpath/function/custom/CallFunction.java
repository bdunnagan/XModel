package org.xmodel.xpath.function.custom;

import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionException;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.function.Function;

public class CallFunction extends Function
{
  public final static String name = "call"; 
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  @Override
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  @Override
  public ResultType getType()
  {
    return ResultType.UNDEFINED;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#getType(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public ResultType getType( IContext context)
  {
    IModelObject element = getArgument( 0).queryFirst( context);
    script = new ScriptAction( element);
    params = Xlate.get( element, "params", "").split( "\\s*,\\s*");
    String spec = Xlate.get( element, "returns", (String)null);
    return ResultType.valueOf( spec.toUpperCase());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    Object[] result = script.run( createCallContext( context));
    if ( result == null) throw new ExpressionException( this, "Return value is null.");
    return (List<IModelObject>)result[ 0];
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    Object[] result = script.run( createCallContext( context));
    if ( result == null) throw new ExpressionException( this, "Return value is null.");
    return ((Number)result[ 0]).doubleValue();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    Object[] result = script.run( createCallContext( context));
    if ( result == null) throw new ExpressionException( this, "Return value is null.");
    return result[ 0].toString();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    Object[] result = script.run( createCallContext( context));
    if ( result == null) throw new ExpressionException( this, "Return value is null.");
    return ((Boolean)result[ 0]);
  }

  /**
   * Create the calling context and set parameters.
   * @param context The context.
   * @return Returns the calling context.
   */
  private StatefulContext createCallContext( IContext context)
  {
    StatefulContext callContext = new StatefulContext( context);
    
    for( int i=0; i<params.length; i++)
    {
      IExpression arg = getArgument( i+1);
      switch( arg.getType( context))
      {
        case NODES:   callContext.set( params[ i], arg.evaluateNodes( context)); break;
        case STRING:  callContext.set( params[ i], arg.evaluateString( context)); break;
        case NUMBER:  callContext.set( params[ i], arg.evaluateNumber( context)); break;
        case BOOLEAN: callContext.set( params[ i], arg.evaluateBoolean( context)); break;
        case UNDEFINED: 
        {
          throw new XActionException( String.format(
            "Argument %d has undefined type in %s",
            i-1, this));
        }
      }
    }
    
    return callContext;
  }
  
  private IXAction script;
  private String[] params;
}
