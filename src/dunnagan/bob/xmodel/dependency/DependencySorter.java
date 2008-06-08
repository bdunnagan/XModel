/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.dependency;

import java.util.*;

/**
 * An implementation of IDependencySorter which uses Jeff Ortel's algorithm.
 */
@SuppressWarnings("unchecked")
public class DependencySorter implements IDependencySorter
{
  public DependencySorter()
  {
    rules = new ArrayList<IDependency>( 2);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IDependencySorter#add(dunnagan.bob.xmodel.IDependency)
   */
  public void add( IDependency rule)
  {
    if ( !rules.contains( rule)) rules.add( rule);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IDependencySorter#remove(dunnagan.bob.xmodel.IDependency)
   */
  public void remove( IDependency rule)
  {
    rules.remove( rule);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IDependencySorter#count()
   */
  public int count()
  {
    return rules.size();
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IDependencySorter#sort(java.util.Collection)
   */
  public List<Object> sort( Collection<Object> objects)
  {
    List<Object> sourceList = new ArrayList<Object>( objects);
    List<Object> resultList = new ArrayList<Object>( objects.size());

    Stack<Object> stack = new Stack<Object>();
    while ( sourceList.size() > 0)
    {
      Object object = sourceList.remove( 0);
      stack.push( object);
      while ( !stack.empty())
      {
        object = stack.peek();
        boolean found = false;
        Iterator iter = sourceList.iterator();
        while ( iter.hasNext())
        {
          Object candidate = iter.next();
          for ( int i=0; i<rules.size(); i++)
          {
            IDependency rule = (IDependency)rules.get( i);
            if ( rule.evaluate( object, candidate))
            {
              found = true;
              stack.push( candidate);
              iter.remove();
              break;
            }
          }
        }

        if ( !found)
        {
          stack.pop();
          resultList.add( object);
        }
      }
    }

    return resultList;
  }
  
  List<IDependency> rules;
}
