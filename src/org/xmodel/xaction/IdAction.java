/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IdAction.java
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
package org.xmodel.xaction;

import java.util.Random;
import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.util.Identifier;
import org.xmodel.util.Radix;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * An XAction which generates a random identifier with default length of 10.
 */
public class IdAction extends GuardedAction
{
  public IdAction()
  {
    random = new Random( System.nanoTime());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    INode config = document.getRoot();
    var = Conventions.getVarName( config, false, "assign");    
    targetExpr = Xlate.get( config, (IExpression)null);
    length = Xlate.get( config, "length", 10);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    String first = null;
    
    if ( targetExpr != null)
    {
      for ( INode node: targetExpr.query( context, null))
      {
        String id = generate( length);
        if ( first == null) first = id;
        node.setValue( id);
      }
    }
    
    if ( var != null)
    {
      if ( first == null) first = generate( length);
      IVariableScope scope = context.getScope();
      if ( scope != null) scope.set( var, first);
    }
    else
    {
      if ( first == null) first = generate( length);
      context.getObject().setValue( first);
    }
    
    return null;
  }
  
  /**
   * Generate an ID of the specified length.
   * @param length The length.
   * @return Returns the ID.
   */
  private final String generate( int length)
  {
    return Identifier.generate( random, length);
  }
  
  private Random random;
  private String var;
  private IExpression targetExpr;
  private int length;
  
  public static void main( String[] args)
  {
    Random random = new Random();
    int length = 3;
    StringBuilder sb = new StringBuilder();
    for( int k=0; k<1000; k++)
    {
      sb.setLength( 0);
      for( int i=0; i<length; i=sb.length())
      {
        long value = random.nextLong();
        sb.append( Radix.convert( value, 36).toUpperCase());
      }
      sb.setLength( length);
      System.out.println( sb);
    }
  }
}
