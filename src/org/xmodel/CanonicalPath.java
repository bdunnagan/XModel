/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * CanonicalPath.java
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
package org.xmodel;

import org.xmodel.xpath.expression.IExpression;

/**
 * An implementation of AbstractPath whose IPathElement list is constructed manually or from
 * another IPath.  The <code>compile</code> method is unsupported.
 */
public class CanonicalPath extends AbstractPath
{
  /**
   * Create a new empty path.
   */
  public CanonicalPath()
  {
  }

  /**
   * Create a new path with all the elements of the given path.
   * @param path The path to be copied.
   */
  public CanonicalPath( IPath path)
  {
    this( path, 0, path.length());
  }
  
  /**
   * Create a new path consisting of elements of the given path starting with the given index.
   * @param path The path to be copied.
   * @param start The index of the first element to be copied.
   */
  public CanonicalPath( IPath path, int start)
  {
    this( path, start, -1);
  }
  
  /**
   * Create a new path from the given path segment.
   * @param path The path to be copied.
   * @param start The index of the first element to copy.
   * @param end The index of the last element plus one or -1 for the remainder.
   */
  public CanonicalPath( IPath path, int start, int end)
  {
    if ( end < 0 || end > path.length()) end = path.length();
    for ( int i=start; i<end; i++)
    {
      addElement( path.getPathElement( i).clone());
    }
  }
  
  /**
   * Create a CanonicalPath which is part of the specified expression tree.
   * @param parent The parent expression.
   */
  public CanonicalPath( IExpression parent)
  {
    setParent( parent);
  }
  
  /**
   * Create a new path with all the elements of the given path belonging to the specified expression tree.
   * @param parent The parent expression.
   * @param path The path to be copied.
   */
  public CanonicalPath( IExpression parent, IPath path)
  {
    this( parent, path, 0, path.length());
  }
  
  /**
   * Create a new path consisting of elements of the given path starting with the given index.
   * @param parent The parent expression.
   * @param path The path to be copied.
   * @param start The index of the first element to be copied.
   */
  public CanonicalPath( IExpression parent, IPath path, int start)
  {
    this( parent, path, start, -1);
  }
  
  /**
   * Create a new path from the given path segment.
   * @param parent The parent expression.
   * @param path The path to be copied.
   * @param start The index of the first element to copy.
   * @param end The index of the last element plus one or -1 for the remainder.
   */
  public CanonicalPath( IExpression parent, IPath path, int start, int end)
  {
    setParent( parent);
    if ( end < 0 || end > path.length()) end = path.length();
    for ( int i=start; i<end; i++)
    {
      IPathElement clone = path.getPathElement( i).clone();
      clone.setParent( this);
      addElement( clone);
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return ModelAlgorithms.pathToString( this);
  }
}
