package org.xmodel;

import java.util.Collection;
import java.util.List;

import org.xmodel.memento.IMemento;
import org.xmodel.node.IStorage;

/**
 * An implementation of INode with an interchangeable storage representation that allows it to
 * provide very compact storage tailored for the type of data being stored.
 */
public class Node implements INode
{
  public Node( String type)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.INode#clearModel()
   */
  @Override
  public void clearModel()
  {
    if ( storage instanceof IStorage)
      ((IStorage)storage).clearModel( parent);
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getModel()
   */
  @Override
  public IModel getModel()
  {
    if ( storage instanceof IStorage)
      return ((IStorage)storage).getModel( parent);
    else
      return GlobalSettings.getInstance().getModel();
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getType()
   */
  @Override
  public String getType()
  {
    if ( storage instanceof IStorage)
    {
      return ((IStorage)storage).getType( parent);
    }
    else
    {
      parent.getChildren().in
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#isType(java.lang.String)
   */
  @Override
  public boolean isType( String type)
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#isDirty()
   */
  @Override
  public boolean isDirty()
  {
    // TODO Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#setAttribute(java.lang.String, java.lang.Object)
   */
  @Override
  public Object setAttribute( String attrName, Object attrValue)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getAttribute(java.lang.String)
   */
  @Override
  public Object getAttribute( String attrName)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getAttributeNode(java.lang.String)
   */
  @Override
  public INode getAttributeNode( String attrName)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getAttributeNames()
   */
  @Override
  public Collection<String> getAttributeNames()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#removeAttribute(java.lang.String)
   */
  @Override
  public Object removeAttribute( String attrName)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#setValue(java.lang.Object)
   */
  @Override
  public Object setValue( Object value)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getValue()
   */
  @Override
  public Object getValue()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#addChild(org.xmodel.INode)
   */
  @Override
  public void addChild( INode object)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#addChild(org.xmodel.INode, int)
   */
  @Override
  public void addChild( INode object, int index)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#removeChild(int)
   */
  @Override
  public INode removeChild( int index)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#removeChild(org.xmodel.INode)
   */
  @Override
  public void removeChild( INode object)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#removeFromParent()
   */
  @Override
  public void removeFromParent()
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getChild(int)
   */
  @Override
  public INode getChild( int index)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getFirstChild(java.lang.String)
   */
  @Override
  public INode getFirstChild( String type)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getCreateChild(java.lang.String)
   */
  @Override
  public INode getCreateChild( String type)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getChildren()
   */
  @Override
  public List<INode> getChildren()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getChildren(java.lang.String)
   */
  @Override
  public List<INode> getChildren( String type)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getNumberOfChildren()
   */
  @Override
  public int getNumberOfChildren()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#internal_setParent(org.xmodel.INode)
   */
  @Override
  public INode internal_setParent( INode parent)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#internal_addChild(org.xmodel.INode, int)
   */
  @Override
  public void internal_addChild( INode child, int index)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#internal_removeChild(int)
   */
  @Override
  public INode internal_removeChild( int index)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#internal_notifyParent(org.xmodel.INode, org.xmodel.INode)
   */
  @Override
  public void internal_notifyParent( INode newParent, INode oldParent)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#internal_notifyAddChild(org.xmodel.INode, int)
   */
  @Override
  public void internal_notifyAddChild( INode child, int index)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#internal_notifyRemoveChild(org.xmodel.INode, int)
   */
  @Override
  public void internal_notifyRemoveChild( INode child, int index)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getParent()
   */
  @Override
  public INode getParent()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#addModelListener(org.xmodel.IModelListener)
   */
  @Override
  public void addModelListener( IModelListener listener)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#removeModelListener(org.xmodel.IModelListener)
   */
  @Override
  public void removeModelListener( IModelListener listener)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getModelListeners()
   */
  @Override
  public ModelListenerList getModelListeners()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#getPathListeners()
   */
  @Override
  public PathListenerList getPathListeners()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#cloneObject()
   */
  @Override
  public INode cloneObject()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#cloneTree()
   */
  @Override
  public INode cloneTree()
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#revertUpdate(org.xmodel.memento.IMemento)
   */
  @Override
  public void revertUpdate( IMemento memento)
  {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see org.xmodel.INode#restoreUpdate(org.xmodel.memento.IMemento)
   */
  @Override
  public void restoreUpdate( IMemento memento)
  {
    // TODO Auto-generated method stub
    
  }
  
  private INode parent;
  private Object storage;
}
