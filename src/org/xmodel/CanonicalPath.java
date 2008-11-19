/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
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
