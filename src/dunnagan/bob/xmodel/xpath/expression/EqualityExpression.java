/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.List;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.xpath.function.NumberFunction;
import dunnagan.bob.xmodel.xpath.function.StringFunction;

/**
 * An implementation of IExpression which represents an X-Path 1.0 equality expression. 
 */
public class EqualityExpression extends AbstractBinaryBooleanExpression
{
  public enum Operator { EQ, NEQ};
  
  /**
   * Create a EqualityExpression with the given operator.
   * @param operator The type of logical expression.
   */
  public EqualityExpression( Operator operator)
  {
    this.operator = operator;
  }
  
  /**
   * Create a EqualityExpression with the given operator, lhs and rhs expressions.
   * @param operator The type of logical expression.
   * @param lhs The left-hand-side of the expression.
   * @param rhs The right-hand-side of the expression.
   */
  public EqualityExpression( Operator operator, IExpression lhs, IExpression rhs)
  {
    this.operator = operator;
    addArgument( lhs);
    addArgument( rhs);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "equality";
  }

  /**
   * Returns the operator.
   * @return Returns the operator.
   */
  public Operator getOperator()
  {
    return operator;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.AbstractBinaryBooleanExpression#evaluate(
   * dunnagan.bob.xmodel.xpath.expression.IContext, dunnagan.bob.xmodel.xpath.expression.IExpression, 
   * dunnagan.bob.xmodel.xpath.expression.IExpression)
   */
  protected boolean evaluate( IContext context, IExpression lhs, IExpression rhs) throws ExpressionException
  {
    ResultType type0 = lhs.getType( context);
    ResultType type1 = rhs.getType( context);

    if ( type0 == ResultType.NODES)
    {
      if ( type1 == ResultType.BOOLEAN)
        return compareResult( lhs.evaluateBoolean( context), rhs.evaluateBoolean( context));
      else if ( type1 == ResultType.NODES)
        return compareResult( lhs.evaluateNodes( context), rhs.evaluateNodes( context));
      else if ( type1 == ResultType.NUMBER)
        return compareResult( lhs.evaluateNodes( context), rhs.evaluateNumber( context));
      else
        return compareResult( lhs.evaluateNodes( context), rhs.evaluateString( context));
    }
    else if ( type1 == ResultType.NODES)
    {
      if ( type0 == ResultType.BOOLEAN)
        return compareResult( rhs.evaluateBoolean( context), lhs.evaluateBoolean( context));
      else if ( type0 == ResultType.NODES)
        return compareResult( rhs.evaluateNodes( context), lhs.evaluateNodes( context));
      else if ( type0 == ResultType.NUMBER)
        return compareResult( rhs.evaluateNodes( context), lhs.evaluateNumber( context));
      else
        return compareResult( rhs.evaluateNodes( context), lhs.evaluateString( context));
    }  
    else if ( type0 == ResultType.BOOLEAN || type1 == ResultType.BOOLEAN)
    {
      return compareResult( lhs.evaluateBoolean( context), rhs.evaluateBoolean( context));
    }
    else if ( type0 == ResultType.NUMBER || type1 == ResultType.NUMBER)
    {
      return compareResult( lhs.evaluateNumber( context), rhs.evaluateNumber( context));
    }
    else 
    {
      return compareResult( lhs.evaluateString( context), rhs.evaluateString( context));
    }
  }

  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the boolean result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, List<IModelObject> result2)
  {
    for ( int i=0; i<result1.size(); i++)
    {
      IModelObject object1 = (IModelObject)result1.get( i);
      String string1 = StringFunction.stringValue( object1);
      for ( int j=0; j<result2.size(); j++)
      {
        IModelObject object2 = (IModelObject)result2.get( j);
        String string2 = StringFunction.stringValue( object2);
        if ( operator == Operator.EQ)
        {
          if ( string1.equals( string2)) return true;
        }
        else
        {
          if ( !string1.equals( string2)) return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the boolean result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, double result2)
  {
    for ( int i=0; i<result1.size(); i++)
    {
      IModelObject object1 = (IModelObject)result1.get( i);
      String string1 = StringFunction.stringValue( object1);
      double number1 = NumberFunction.numericValue( string1);
      if ( operator == Operator.EQ)
      {
        if ( number1 == result2) return true;
      }
      else
      {
        if ( number1 != result2) return true;
      }
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the boolean result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, String result2)
  {
    for ( int i=0; i<result1.size(); i++)
    {
      IModelObject object1 = (IModelObject)result1.get( i);
      String string1 = StringFunction.stringValue( object1);
      if ( operator == Operator.EQ)
      {
        if ( string1.equals( result2)) return true;
      }
      else
      {
        if ( !string1.equals( result2)) return true;
      }
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the boolean result of the comparison.
   */
  private boolean compareResult( boolean result1, boolean result2)
  {
    boolean result = (result1 == result2);
    if ( operator == Operator.NEQ) result = !result;
    return result;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the boolean result of the comparison.
   */
  private boolean compareResult( double result1, double result2)
  {
    boolean result = (result1 == result2);
    if ( operator == Operator.NEQ) result = !result;
    return result;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the boolean result of the comparison.
   */
  private boolean compareResult( String result1, String result2)
  {
    boolean result = result1.equals( result2);
    if ( operator == Operator.NEQ) result = !result;
    return result;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#createSubtree(dunnagan.bob.xmodel.xpath.expression.IContext, 
   * dunnagan.bob.xmodel.IModelObjectFactory, dunnagan.bob.xmodel.IChangeSet)
   */
  @Override
  public void createSubtree( IContext context, IModelObjectFactory factory, IChangeSet undo)
  {
    // create assignee node
    getArgument( 0).createSubtree( context, factory, undo);
    
    // if operator is equals then evaluate rhs and assign to first lhs node
    if ( operator == Operator.EQ)
    {
      IModelObject lhs = queryFirst( context);
      if ( lhs != null) lhs.setValue( getArgument( 1).evaluateString( context));
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    IExpression lhs = getArgument( 0);
    IExpression rhs = getArgument( 1);
    switch( operator)
    {
      case EQ: return lhs.toString()+" = "+rhs.toString();
      case NEQ: return lhs.toString()+" != "+rhs.toString();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new EqualityExpression( operator);
  }
  
  Operator operator;
}
