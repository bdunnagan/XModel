/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * DescendantAxisListener.java
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
package org.xmodel.path;

import java.util.List;
import org.xmodel.*;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;


/**
 * An implementation of IFanoutListener for an IPathElement with the <i>DESCENDANT</i> axis.
 * <br>
 * <b>Note: The following axis listeners exhibit a subtle flaw: AncestorAxisListener,
 * DescendantAxisListener, NestedAxisListener, FollowingAxisListener and PrecedingAxisListener.
 * These listeners install their associated IModelListener instances after fanout (see code below
 * marked with *). A listener chain whose next to last link is one of these axis listeners will not
 * be able to detect that fanout is required if the client adds an element on the axis during
 * notification.</b>
 */
public class DescendantAxisListener extends FanoutListener
{
  public DescendantAxisListener( IListenerChain chain, int chainIndex)
  {
    super( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#installListeners(org.xmodel.IModelObject)
   */
  protected void installListeners( INode object)
  {
    BreadthFirstIterator iter = new BreadthFirstIterator( object);
    while( iter.hasNext())
    {
      INode descendant = (INode)iter.next();
      descendant.addModelListener( this);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.FanoutListener#uninstallListeners(org.xmodel.IModelObject)
   */
  protected void uninstallListeners( INode object)
  {
    BreadthFirstIterator iter = new BreadthFirstIterator( object);
    while( iter.hasNext())
    {
      INode descendant = (INode)iter.next();
      descendant.removeModelListener( this);
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.IListenerChainLink#cloneOne(org.xmodel.path.IListenerChain)
   */
  public IListenerChainLink cloneOne( IListenerChain chain)
  {
    return new DescendantAxisListener( chain, chainIndex);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyAddChild(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyAddChild( INode parent, INode child, int index)
  {
    // install next link (* see above)
    BreadthFirstIterator iter = new BreadthFirstIterator( child);
    while( iter.hasNext())
    {
      INode descendant = (INode)iter.next();
      if ( fanoutElement.evaluate( null, null, descendant)) 
        getNextListener().incrementalInstall( descendant);
    }
    
    // install my listeners (* see above)
    installListeners( child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.path.ListenerChainLink#notifyRemoveChild(
   * org.xmodel.IModelObject, org.xmodel.IModelObject, int)
   */
  public void notifyRemoveChild( INode parent, INode child, int index)
  {
    // uninstall my listeners
    uninstallListeners( child);

    // uninstall next link
    BreadthFirstIterator iter = new BreadthFirstIterator( child);
    while( iter.hasNext())
    {
      INode descendant = (INode)iter.next();
      if ( fanoutElement.evaluate( null, null, descendant)) 
        getNextListener().incrementalUninstall( descendant);
    }
  }
  
  /**
   * Test case demonstrating flaw mentioned in class comments.
   * @param args Program arguments.
   */
  public static void main( String[] args) throws Exception
  {
    String xml = "<node id='1'><node id='2'></node></node>";
    XmlIO xmlIO = new XmlIO();
    INode root = xmlIO.read( xml);
    
    final IPath path2 = XPath.createPath( "nested::node[ last()]"); 
    
    IPath path1 = XPath.createPath( "nested::node");
    path1.addPathListener( new Context( root), new IPathListener() {
      public void notifyAdd( IContext context, IPath path, int pathIndex, List<INode> nodes)
      {
        // this method will be called three times:
        // 1. leaf is not the same as the nodes.get( 0) so notification never happens (flaw)
        // 2. leaf is the same as nodes.get( 0) so notification is deferred
        // 3. this is the deferred notification
        System.out.println( "node: "+nodes.get( 0));
        INode node = nodes.get( 0);
        INode leaf = path2.queryFirst( node);
        if ( leaf != null)
        {
          node = new ModelObject( "node");
          node.setID( "3");
          leaf.addChild( node);
        }
      }
      public void notifyRemove( IContext context, IPath path, int pathIndex, List<INode> nodes)
      {
      }
    });
  }
}
