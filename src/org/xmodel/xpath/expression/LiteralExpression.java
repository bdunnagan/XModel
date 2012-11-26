/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * LiteralExpression.java
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
package org.xmodel.xpath.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.INode;


/**
 * A literal value expression. The type of the expression is defined by what type of value
 * is assigned to the literal. If the value is a <code>java.util.List</code> then the type
 * will be <i>NODES</i>. If the value is a <code>java.lang.Double</code> then the type will
 * be <i>NUMBER</i>. If the value is a <code>java.lang.Boolean</code> then the type will be
 * <i>BOOLEAN</i>. All other values are treated as strings. A null value is converted to an
 * empty string.
 */
@SuppressWarnings("unchecked")
public class LiteralExpression extends Expression
{
  public LiteralExpression()
  {
    setValue( "");
  }
  
  /**
   * Create and assign a LiteralExpression.
   * @param value The literal value.
   */
  public LiteralExpression( Object value)
  {
    setValue( value);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  public String getName()
  {
    return "literal";
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  public ResultType getType()
  {
    return type;
  }

  /**
   * Set the literal value. Note that this method does not perform notification. Use the
   * <code>VariableExpression</code> class if that behavior is required.
   * @param value The literal value.
   */
  public void setValue( Object value)
  {
    // unbind if necessary
    Set<IContext> bindings = null;
    if ( type == ResultType.NODES && parent != null && parent.requiresValueNotification( this))
    {
      bindings = new HashSet<IContext>();
      for( INode node: (List<INode>)this.value)
      {
        List<LeafValueListener> listeners = LeafValueListener.findListeners( node, this);
        for( LeafValueListener listener: listeners) 
        {
          node.removeModelListener( listener);
          bindings.add( listener.getContext());
        }
      }
    }
    
    // set value
    if ( value == null) value = "";
    this.value = value;
    
    // set type
    if ( value instanceof List)
    {
      type = ResultType.NODES;
      if ( parent != null && parent.requiresValueNotification( this) && bindings != null && bindings.size() > 0)
      {
        for( IContext binding: bindings)
        {
          LeafValueListener listener = new LeafValueListener( this, binding);
          for( INode node: (List<INode>)value)
            node.addModelListener( listener);
        }
      }
    }
    else if ( value instanceof Number)
      type = ResultType.NUMBER;
    else if ( value instanceof Boolean)
      type = ResultType.BOOLEAN;
    else
      type = ResultType.STRING;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<INode> evaluateNodes( IContext context) throws ExpressionException
  {
    if ( type == ResultType.NODES) return (List<INode>)value;
    return super.evaluateNodes( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateBoolean(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public boolean evaluateBoolean( IContext context) throws ExpressionException
  {
    if ( type == ResultType.BOOLEAN) return (Boolean)value;
    return super.evaluateBoolean( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNumber(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public double evaluateNumber( IContext context) throws ExpressionException
  {
    if ( type == ResultType.NUMBER) return ((Number)value).doubleValue();
    return super.evaluateNumber( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateString(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public String evaluateString( IContext context) throws ExpressionException
  {
    if ( type == ResultType.STRING) return value.toString();
    return super.evaluateString( context);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#bind(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void bind( IContext context)
  {
    if ( getType( context) == ResultType.NODES && parent != null && parent.requiresValueNotification( this))
    {
      LeafValueListener listener = new LeafValueListener( this, context);
      List<INode> nodes = (List<INode>)value;
      for( INode node: nodes) node.addModelListener( listener);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#unbind(
   * org.xmodel.xpath.expression.IContext)
   */
  @Override
  public void unbind( IContext context)
  {
    if ( getType( context) == ResultType.NODES && parent != null && parent.requiresValueNotification( this))
    {
      LeafValueListener listener = null;
      List<INode> nodes = (List<INode>)value;
      for( INode node: nodes) 
      {
        if ( listener == null) listener = LeafValueListener.findListener( node, this, context);
        if ( listener != null) node.removeModelListener( listener);
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#cloneOne()
   */
  @Override
  protected IExpression cloneOne()
  {
    LiteralExpression clone = new LiteralExpression();
    clone.setValue( value);
    return clone;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    if ( type == null) return "";
    switch( type)
    {
      case NODES:
      {
        List<INode> nodes = (List<INode>)value;
        StringBuilder builder = new StringBuilder();
        builder.append( "{ ");
        for( int i=0; i<nodes.size(); i++)
        {
          builder.append( nodes.get( i).toString());
          if ( i < (nodes.size() - 1)) builder.append( ", ");
        }
        builder.append( '}');
        return builder.toString();
      }
      
      case NUMBER:
        return value.toString();
      
      default: 
        return (value != null)? "'"+value.toString()+"'": "''";
    }
  }

  ResultType type;
  Object value;
}
