/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.expression;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xpath.function.BooleanFunction;
import org.xmodel.xpath.function.NumberFunction;
import org.xmodel.xpath.function.StringFunction;


/**
 * An implementation of IExpression which represents an X-Path 1.0 relational expression.
 */
public class RelationalExpression extends AbstractBinaryBooleanExpression
{
  public enum Operator { GT, GE, LT, LE};
  
  /**
   * Create a RelationalExpression with the given operator.
   * @param operator The type of logical expression.
   */
  public RelationalExpression( Operator operator)
  {
    this.operator = operator;
  }
  
  /**
   * Create a RelationalExpression with the given operator, lhs and rhs expressions.
   * @param operator The type of logical expression.
   * @param lhs The left-hand-side of the expression.
   * @param rhs The right-hand-side of the expression.
   */
  public RelationalExpression( Operator operator, IExpression lhs, IExpression rhs)
  {
    this.operator = operator;
    addArgument( lhs);
    addArgument( rhs);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "relational";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.AbstractBinaryBooleanExpression#evaluate(
   * org.xmodel.xpath.expression.IContext, org.xmodel.xpath.expression.IExpression, 
   * org.xmodel.xpath.expression.IExpression)
   */
  public boolean evaluate( IContext context, IExpression lhs, IExpression rhs) throws ExpressionException
  {
    ResultType type0 = lhs.getType( context);
    ResultType type1 = rhs.getType( context);

    if ( type0 == ResultType.NODES)
    {
      if ( type1 == ResultType.BOOLEAN)
        return compareResult( lhs.evaluateNodes( context), rhs.evaluateBoolean( context));
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
        return compareResult( rhs.evaluateNodes( context), lhs.evaluateBoolean( context));
      else if ( type0 == ResultType.NODES)
        return compareResult( lhs.evaluateNodes( context), rhs.evaluateNodes( context));
      else if ( type0 == ResultType.NUMBER)
        return compareResult( lhs.evaluateNumber( context), rhs.evaluateNodes( context));
      else
        return compareResult( lhs.evaluateString( context), rhs.evaluateNodes( context));
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
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, List<IModelObject> result2)
  {
    for ( int i=0; i<result1.size(); i++)
    {
      IModelObject object1 = (IModelObject)result1.get( i);
      String string1 = StringFunction.stringValue( object1);
      for ( int j=0; j<result2.size(); j++)
      {
        IModelObject object2 = (IModelObject)result2.get( i);
        String string2 = StringFunction.stringValue( object2);
        switch( operator)
        {
          case GT:
            if ( string1.compareTo( string2) > 0) return true;
            break;
            
          case GE:
            if ( string1.compareTo( string2) >= 0) return true;
            break;
            
          case LT:
            if ( string1.compareTo( string2) < 0) return true;
            break;
            
          case LE:
            if ( string1.compareTo( string2) <= 0) return true;
            break;
        }
      }
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, double result2)
  {
    for ( int i=0; i<result1.size(); i++)
    {
      IModelObject object1 = (IModelObject)result1.get( i);
      String string1 = StringFunction.stringValue( object1);
      double number1 = NumberFunction.numericValue( string1);
      switch( operator)
      {
        case GT:
          if ( number1 > result2) return true;
          break;
        
        case GE:
          if ( number1 >= result2) return true;
          break;
        
        case LT:
          if ( number1 < result2) return true;
          break;
        
        case LE:
          if ( number1 <= result2) return true;
          break;
      }
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, String result2)
  {
    for ( int i=0; i<result1.size(); i++)
    {
      IModelObject object1 = (IModelObject)result1.get( i);
      String string1 = StringFunction.stringValue( object1);
      switch( operator)
      {
        case GT:
          if ( string1.compareTo( result2) > 0) return true;
          break;
        
        case GE:
          if ( string1.compareTo( result2) >= 0) return true;
          break;
        
        case LT:
          if ( string1.compareTo( result2) < 0) return true;
          break;
        
        case LE:
          if ( string1.compareTo( result2) <= 0) return true;
          break;
      }
    }
    return false;
  }

  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( List<IModelObject> result1, boolean result2)
  {
    boolean boolean1 = BooleanFunction.booleanValue( result1);
    switch( operator)
    {
      case GT: return (boolean1 && !result2);
      case GE: return (boolean1 && !result2) || (boolean1 == result2);
      case LT: return (!boolean1 && result2);
      case LE: return (!boolean1 && result2) || (boolean1 == result2);
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( double result1, List<IModelObject> result2)
  {
    for ( int i=0; i<result2.size(); i++)
    {
      IModelObject object2 = (IModelObject)result2.get( i);
      String string2 = StringFunction.stringValue( object2);
      double number2 = NumberFunction.numericValue( string2);
      switch( operator)
      {
        case GT:
          if ( result1 > number2) return true;
          break;
        
        case GE:
          if ( result1 >= number2) return true;
          break;
        
        case LT:
          if ( result1 < number2) return true;
          break;
        
        case LE:
          if ( result1 <= number2) return true;
          break;
      }
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( String result1, List<IModelObject> result2)
  {
    for ( int i=0; i<result2.size(); i++)
    {
      IModelObject object2 = (IModelObject)result2.get( i);
      String string2 = StringFunction.stringValue( object2);
      switch( operator)
      {
        case GT:
          if ( result1.compareTo( string2) > 0) return true;
          break;
        
        case GE:
          if ( result1.compareTo( string2) >= 0) return true;
          break;
        
        case LT:
          if ( result1.compareTo( string2) < 0) return true;
          break;
        
        case LE:
          if ( result1.compareTo( string2) <= 0) return true;
          break;
      }
    }
    return false;
  }

  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( boolean result1, boolean result2)
  {
    switch( operator)
    {
      case GT: return (result1 && !result2);
      case GE: return (result1 && !result2) || (result1 == result2);
      case LT: return (!result1 && result2);
      case LE: return (!result1 && result2) || (result1 == result2);
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( double result1, double result2)
  {
    switch( operator)
    {
      case GT: return (result1 > result2);
      case GE: return (result1 >= result2);
      case LT: return (result1 < result2);
      case LE: return (result1 <= result2);
    }
    return false;
  }
  
  /**
   * Compare results of the two expressions and set the result member.
   * @param result1 One side of the operation.
   * @param result2 One side of the operation.
   * @return Returns the result of the comparison.
   */
  private boolean compareResult( String result1, String result2)
  {
    double value1 = NumberFunction.numericValue( result1);
    double value2 = NumberFunction.numericValue( result2);
    switch( operator)
    {
      case GT: return (value1 > value2);
      case GE: return (value1 >= value2);
      case LT: return (value1 < value2);
      case LE: return (value1 <= value2);
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    return new RelationalExpression( operator);
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
      case GT: return lhs.toString()+" > "+rhs.toString();
      case GE: return lhs.toString()+" >= "+rhs.toString();
      case LT: return lhs.toString()+" < "+rhs.toString();
      case LE: return lhs.toString()+" <= "+rhs.toString();
    }
    return null;
  }

  Operator operator;
}
