package org.xmodel.node;

import org.xmodel.IModel;
import org.xmodel.INode;

/**
 * An interface for storage classes for the Node class.
 */
public interface IStorage
{
  /**
   * Clear the cached IModel instance if applicable.
   * @param parent The parent of the node to which this storage object belongs.
   */
  public void clearModel( INode parent);
  
  /**
   * Returns the IModel instance to which this node belongs.
   * @param parent The parent of the node to which this storage object belongs.
   * @return Returns the IModel to which this node belongs.
   */
  public IModel getModel( INode parent);

  /**
   * Return the type of this node.
   * @param parent The parent of the node to which this storage object belongs.
   * @return Return the type of this node.
   */
  public String getType( INode parent);
}
