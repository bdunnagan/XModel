/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ConfiguredXmlMatcher.java
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
package org.xmodel.diff;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.xmodel.INode;


/**
 * An IXmlMatcher which provides methods to customize the behavior of a matcher precisely.
 * Elements and attributes from the left-hand-side and right-hand-side of the diff, which
 * are to be ignored, are explicitly specified, as are elements whose children are ordered.
 */
public class ConfiguredXmlMatcher extends DefaultXmlMatcher
{
  public ConfiguredXmlMatcher()
  {
    ignoreSet = new HashSet<INode>();
    orderedSet = new HashSet<INode>();
  }
  
  /**
   * Specify an element or attribute to be ignored.
   * @param node The element or attribute.
   */
  public void ignore( INode node)
  {
    ignoreSet.add( node);
  }
  
  /**
   * Specify an element or attribute to be regarded (cancels previous <code>ignore</code>).
   * @param node The element or attribute.
   */
  public void regard( INode node)
  {
    ignoreSet.remove( node);
  }
  
  /**
   * Specify elements and/or attributes to be ignored.
   * @param nodes The elements and/or attributes.
   */
  public void ignore( List<INode> nodes)
  {
    ignoreSet.addAll( nodes);
  }
  
  /**
   * Specify elements and/or attributes to be regarded (cancels previous <code>ignore</code>).
   * @param nodes The elements and/or attributes.
   */
  public void regard( List<INode> nodes)
  {
    ignoreSet.removeAll( nodes);
  }
  
  /**
   * Specify that the children of the specified parent are ordered.
   * @param parent The parent whose children are ordered.
   */
  public void setOrdered( INode parent)
  {
    orderedSet.add( parent);
  }
  
  /**
   * Specify that the children of the specified parent are unordered (cancels previous <code>setOrdered</code>).
   * @param parent The parent whose children are unordered.
   */
  public void setUnordered( INode parent)
  {
    orderedSet.remove( parent);
  }
  
  /**
   * Specify that the children of the specified parents are ordered.
   * @param parents The parents whose children are ordered.
   */
  public void setOrdered( List<INode> parents)
  {
    orderedSet.addAll( parents);
  }
  
  /**
   * Specify that the children of the specified parents are unordered (cancels previous <code>setOrdered</code>).
   * @param parents The parents whose children are unordered.
   */
  public void setUnordered( List<INode> parents)
  {
    orderedSet.removeAll( parents);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.DefaultXmlMatcher#isList(org.xmodel.IModelObject)
   */
  @Override
  public boolean isList( INode parent)
  {
    if ( orderedSet != null && orderedSet.contains( parent)) return true;
    return super.isList( parent);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.DefaultXmlMatcher#shouldDiff(org.xmodel.IModelObject, boolean)
   */
  @Override
  public boolean shouldDiff( INode object, boolean lhs)
  {
    if ( ignoreSet != null) return !ignoreSet.contains( object);
    return super.shouldDiff( object, lhs);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.DefaultXmlMatcher#shouldDiff(org.xmodel.IModelObject, java.lang.String, boolean)
   */
  @Override
  public boolean shouldDiff( INode object, String attrName, boolean lhs)
  {
    if ( ignoreSet != null)
    {
      if ( attrName == null) return !ignoreSet.contains( null);
      INode node = object.getAttributeNode( attrName);
      return !ignoreSet.contains( node);
    }
    return super.shouldDiff( object, attrName, lhs);
  }

  private Set<INode> ignoreSet;
  private Set<INode> orderedSet;
}
