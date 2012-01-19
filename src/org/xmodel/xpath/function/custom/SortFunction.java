/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SortFunction.java
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
package org.xmodel.xpath.function.custom;

import java.util.*;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.*;
import org.xmodel.xpath.function.Function;

/**
 * A custom XPath function which sorts a node-set according to one or more keys. The first
 * argument to the function is the node-set to be sorted. The second and subsequent arguments
 * are the primary, secondary and subsequent keys used to perform the sort. Each key is a
 * node-set expression relative to each node in the node-set being sorted. Each key is 
 * converted to a string argument and the sort is performed lexically. The sort direction may
 * be reversed by enclosing the entire sort function with the <i>reverse</i> function.
 */
public class SortFunction extends Function
{
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "sort";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 2, Integer.MAX_VALUE);
    assertType( context, 0, ResultType.NODES);
    
    IExpression arg0 = getArgument( 0);
    
    List<IModelObject> nodes = arg0.evaluateNodes( context);
    List<IContext> contexts = new ArrayList<IContext>( nodes.size());
    int size = nodes.size();
    for ( int i=0; i<size; i++) contexts.add( new Context( nodes.get( i), i+1, size));
    
    Collections.sort( contexts, new NodeComparator());
    for ( int i=0; i<size; i++) nodes.set( i, contexts.get( i).getObject());
    
    return nodes;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#bind(org.xmodel.xpath.expression.IContext)
   */
  public void bind( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    arg0.bind( context);
    List<IModelObject> nodes = arg0.evaluateNodes( context);
    bindKeys( context, nodes);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#unbind(org.xmodel.xpath.expression.IContext)
   */
  public void unbind( IContext context)
  {
    IExpression arg0 = getArgument( 0);
    arg0.unbind( context);
    List<IModelObject> nodes = arg0.evaluateNodes( context);
    unbindKeys( context, nodes);
  }

  /**
   * Bind key arguments to the specified nodes.
   * @param context The context.
   * @param nodes The nodes.
   */
  private void bindKeys( IContext context, List<IModelObject> nodes)
  {
    List<IExpression> args = getArguments();
    for( int i=0; i<nodes.size(); i++)
    {
      IModelObject node = nodes.get( i);
      for( int j=1; j<args.size(); j++)
        args.get( j).bind( new SubContext( context, node, i+1, nodes.size()));
    }
  }
  
  /**
   * Unbind key arguments from the specified nodes.
   * @param context The context.
   * @param nodes The nodes.
   */
  private void unbindKeys( IContext context, List<IModelObject> nodes)
  {
    List<IExpression> args = getArguments();
    for( int i=0; i<nodes.size(); i++)
    {
      IModelObject node = nodes.get( i);
      for( int j=1; j<args.size(); j++)
        args.get( j).unbind( new SubContext( context, node, i+1, nodes.size()));
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyAdd(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( expression == getArgument( 0))
    {
      bindKeys( context, nodes);
      getParent().notifyChange( this, context);
    }
    else
    {
      getParent().notifyChange( this, context.getParent());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyRemove(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  @Override
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    if ( expression == getArgument( 0))
    {
      unbindKeys( context, nodes);
      getParent().notifyChange( this, context);
    }
    else
    {
      getParent().notifyChange( this, context.getParent());
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    getParent().notifyChange( this, context.getParent());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    getParent().notifyChange( this, context.getParent());
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#notifyChange(
   * org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    getParent().notifyChange( this, context.getParent());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#requiresValueNotification(
   * org.xmodel.xpath.expression.IExpression)
   */
  @Override
  public boolean requiresValueNotification( IExpression argument)
  {
    return argument != getArgument( 0);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#notifyValue(java.util.Collection, 
   * org.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    getParent().notifyChange( this, contexts[ 0].getParent());
  }
  
  private class NodeComparator implements Comparator<IContext>
  {
    public int compare( IContext lhs, IContext rhs)
    {
      try
      {
        int result = 0;
        List<IExpression> arguments = getArguments();
        for( int i=1; i<arguments.size(); i++)
        {
          IExpression argument = arguments.get( i);
          // assume type is the same for lhs and rhs evalutions
          switch( argument.getType( lhs))
          {
            case NODES:
            {
              List<IModelObject> lnodes = argument.evaluateNodes( lhs);
              List<IModelObject> rnodes = argument.evaluateNodes( rhs);
              if ( lnodes.size() == 0) return 1;
              if ( rnodes.size() == 0) return -1;
              String lstring = Xlate.get( lnodes.get( 0), "");
              String rstring = Xlate.get( rnodes.get( 0), "");
              result = compare( lstring, rstring);
              if ( result != 0) return result;
              break;
            }
            
            case STRING:
            {
              String lstring = argument.evaluateString( lhs);
              String rstring = argument.evaluateString( rhs);
              result = compare( lstring, rstring);
              if ( result != 0) return result;
              break;
            }
              
            case NUMBER:
            {
              double lnumber = argument.evaluateNumber( lhs);
              double rnumber = argument.evaluateNumber( rhs);
              if ( lnumber < rnumber) return -1;
              if ( lnumber > rnumber) return 1;
              result = 0;
              break;
            }
              
            case BOOLEAN:
            {
              boolean lbool = argument.evaluateBoolean( lhs);
              boolean rbool = argument.evaluateBoolean( rhs);
              if ( lbool && !rbool) return -1;
              if ( lbool && rbool) return 1;
              result = 0;
              break;
            }
          }
        }
        return result;
      }
      catch( ExpressionException e)
      {
      }
      return 0;
    }
    
    private int compare( String lhs, String rhs)
    {
      int lDigitStart = digitSuffixStart( lhs);
      int rDigitStart = digitSuffixStart( rhs);
      if ( lDigitStart != rDigitStart) return lhs.compareTo( rhs);
      
      // compare prefix
      for( int i=0; i<lDigitStart; i++) 
      {
        char lChar = lhs.charAt( i);
        char rChar = rhs.charAt( i);
        if ( lChar != rChar)
        {
          int lVal = Character.getNumericValue( lChar);
          int rVal = Character.getNumericValue( rChar);
          if ( lVal < rVal) return -1;
          if ( lVal > rVal) return 1;
          return 0;
        }
      }
      
      // compare suffix
      double lSuffix = 0;
      double rSuffix = 0;
      if ( lDigitStart < lhs.length()) lSuffix = Double.valueOf( lhs.substring( lDigitStart));
      if ( rDigitStart < rhs.length()) rSuffix = Double.valueOf( rhs.substring( rDigitStart));
      if ( lSuffix < rSuffix) return -1;
      if ( lSuffix > rSuffix) return 1;
      return 0;
    }
    
    private int digitSuffixStart( String string)
    {
      int j;
      for( int i=string.length()-1; i>=0; i--)
      {
        char c = string.charAt( i);
        for( j=0; j<digits.length; j++) if ( c == digits[ j]) break;
        if ( j == digits.length) return i+1;
      }
      return 0;
    }
  }
  
  final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
}
