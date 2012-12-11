/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DependencySorter.java
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
package org.xmodel.dependency;

import java.util.*;

/**
 * An implementation of IDependencySorter which uses Jeff Ortel's algorithm.
 */
public class DependencySorter implements IDependencySorter
{
  public DependencySorter()
  {
    rules = new ArrayList<IDependency>( 2);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDependencySorter#add(org.xmodel.IDependency)
   */
  public void add( IDependency rule)
  {
    if ( !rules.contains( rule)) rules.add( rule);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IDependencySorter#remove(org.xmodel.IDependency)
   */
  public void remove( IDependency rule)
  {
    rules.remove( rule);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IDependencySorter#count()
   */
  public int count()
  {
    return rules.size();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IDependencySorter#sort(java.util.Collection)
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
        Iterator<?> iter = sourceList.iterator();
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
