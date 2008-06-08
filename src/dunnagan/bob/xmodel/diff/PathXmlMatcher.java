/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.diff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dunnagan.bob.xmodel.IChangeSet;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.xpath.XPath;

/**
 * An extension of DefaultXmlMatcher which defines the parts of the tree which should be differenced
 * in terms of XPath expressions relative to the root of the tree. Similarly, the parts of the tree
 * that should be differenced with the ordered diff algorithm are identified by XPath expressions.
 */
public class PathXmlMatcher extends DefaultXmlMatcher
{
  public PathXmlMatcher()
  {
    listPaths = new ArrayList<IPath>();
    ignorePaths = new ArrayList<IPath>();
  }
  
  /**
   * Define a part of the tree which should be differenced as a list. By default, the unordered
   * diff algorithm is used throughout the tree.
   * @param path A path to the parent of a list.
   */
  public void defineList( String path)
  {
    defineList( XPath.createPath( path));
  }
  
  /**
   * Define a part of the tree which should be differenced as a list. By default, the unordered
   * diff algorithm is used throughout the tree.
   * @param path A path to the parent of a list.
   */
  public void defineList( IPath path)
  {
    if ( !listPaths.contains( path))
      listPaths.add( path);
  }
  
  /**
   * Define a part of the tree which should be ignored by the differencing algorithm.
   * @param path A path to the nodes which will be ignored.
   */
  public void ignorePath( String path)
  {
    ignorePath( XPath.createPath( path));
  }
  
  /**
   * Define a part of the tree which should be ignored by the differencing algorithm.
   * @param path A path to the nodes which will be ignored.
   */
  public void ignorePath( IPath path)
  {
    if ( !ignorePaths.contains( path))
      ignorePaths.add( path);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.DefaultXmlMatcher#startDiff(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IChangeSet)
   */
  @Override
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    ignoreNodes = new HashSet<IModelObject>();
    listNodes = new HashSet<IModelObject>();
    
    for ( IPath listPath: listPaths)
    {
      List<IModelObject> leftNodes = listPath.query( lhs, null);
      listNodes.addAll( leftNodes);
      List<IModelObject> rightNodes = listPath.query( rhs, null);
      listNodes.addAll( rightNodes);
    }

    for ( IPath ignorePath: ignorePaths)
    {
      List<IModelObject> leftNodes = ignorePath.query( lhs, null);
      ignoreNodes.addAll( leftNodes);
      List<IModelObject> rightNodes = ignorePath.query( rhs, null);
      ignoreNodes.addAll( rightNodes);
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.DefaultXmlMatcher#endDiff(dunnagan.bob.xmodel.IModelObject, 
   * dunnagan.bob.xmodel.IModelObject, dunnagan.bob.xmodel.IChangeSet)
   */
  @Override
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    ignoreNodes = null;
    listNodes = null;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.nu.DefaultXmlMatcher#isList(dunnagan.bob.xmodel.IModelObject)
   */
  @Override
  public boolean isList( IModelObject parent)
  {
    return listNodes.contains( parent);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.IXmlMatcher#shouldDiff(dunnagan.bob.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return !ignoreNodes.contains( object);
  }
  
  List<IPath> listPaths;
  List<IPath> ignorePaths;
  Set<IModelObject> ignoreNodes;
  Set<IModelObject> listNodes;
}
