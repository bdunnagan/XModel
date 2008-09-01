/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xpath.function;

import java.util.Hashtable;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.custom.*;
import org.xmodel.xsd.SchemaFunction;

import static java.lang.System.err;

/**
 * The default IFunctionFactory implementation which will generate all of the
 * X-Path 1.0 function objects except local-name() and namespace-uri().
 */
public class FunctionFactory implements IFunctionFactory
{
  @SuppressWarnings("unchecked")
  private void addBaseFunctions()
  {
    Class[] classes = {
      BooleanFunction.class,
      CeilingFunction.class,
      CollectionFunction.class,
      ConcatFunction.class,
      ContainsFunction.class,
      CountFunction.class,
      CreatePathFunction.class,
      DereferenceFunction.class,
      DistinctValuesFunction.class,
      DocFunction.class,
      EvaluateFunction.class,
      FalseFunction.class,
      FloorFunction.class,
      IDFunction.class,
      IndexOfFunction.class,
      LastFunction.class,
      LowercaseFunction.class,
      MatchesFunction.class,
      NotFunction.class,
      NumberFunction.class,
      NameFunction.class,
      NosyncFunction.class,
      ParseXmlFunction.class,
      PositionFunction.class,
      PrintfFunction.class,
      ReplaceFunction.class,
      ReverseFunction.class,
      RoundFunction.class,
      SchemaFunction.class,
      SortFunction.class,
      StartsWithFunction.class,
      StaticFunction.class,
      StringFunction.class,
      StringJoinFunction.class,
      StringLengthFunction.class,
      SubstringAfterFunction.class,
      SubstringBeforeFunction.class,
      SubstringFunction.class,
      SumFunction.class,
      TranslateFunction.class,
      TraceFunction.class,
      TrueFunction.class,
      UppercaseFunction.class
    };
    
    classRegistry = new Hashtable<String, Class>();
    for ( int i=0; i<classes.length; i++)
    {
      try
      {
        IExpression function = (IExpression)classes[ i].newInstance();
        classRegistry.put( function.getName(), classes[ i]);
        classRegistry.put( "fn:"+function.getName(), classes[ i]);
      }
      catch( Exception e)
      {
        err.println( e);
      }
    }
    
    objectRegistry = new Hashtable<String, Function>();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.function.IFunctionFactory#register(java.lang.String, java.lang.Class)
   */
  public void register( String functionName, Class<? extends Function> clss)
  {
    objectRegistry.remove( functionName);
    classRegistry.put( functionName, clss);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.function.IFunctionFactory#register(java.lang.String, org.xmodel.xpath.function.Function)
   */
  public void register( String functionName, Function prototype)
  {
    classRegistry.remove( functionName);
    objectRegistry.put( functionName, prototype);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.function.IFunctionFactory#createFunction(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  public IExpression createFunction( String functionName)
  {
    try
    {
      // create from registered class
      Class functionClass = classRegistry.get( functionName);
      if ( functionClass != null) return (IExpression)functionClass.newInstance();
      
      // create from registered prototype
      Function function = objectRegistry.get( functionName);
      if ( function != null) return (IExpression)function.clone();
      
      return null;
    }
    catch( InstantiationException e)
    {
      err.println( e);
      return null;
    }
    catch( IllegalAccessException e)
    {
      err.println( e);
      return null;
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    for( String key: classRegistry.keySet())
    {
      builder.append( key);
      builder.append( " = ");
      builder.append( classRegistry.get( key));
      builder.append( '\n');
    }

    for( String key: objectRegistry.keySet())
    {
      builder.append( key);
      builder.append( " = ");
      builder.append( objectRegistry.get( key));
      builder.append( '\n');
    }
    
    return builder.toString();
  }

  /**
   * Returns the singleton instance.
   * @return Returns the singleton instance.
   */
  static public FunctionFactory getInstance()
  {
    if ( instance == null) 
    {
      instance = new FunctionFactory();
      instance.addBaseFunctions();
    }
    return instance;
  }
  
  private static FunctionFactory instance;
  private Hashtable<String, Function> objectRegistry;
  
  @SuppressWarnings("unchecked")
  private Hashtable<String, Class> classRegistry;
}
