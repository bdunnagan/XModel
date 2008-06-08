/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.util.List;

import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableSource;

/**
 * This interface represents a pre-compiled path through the domain object tree. The path consists
 * of a sequence of <i>location steps</i> as defined by the X-Path 1.0 specification.
 * <p>
 * <b>Note:</b> When an IPathListener is added to an IPath, it is associated to a particular
 * instance of IContext. Currently, IContext cannot be modified and therefore the association
 * assumes that the context position and size will not change. If the context position or size does
 * change, it is the responsibility of the client to reinstall the listener with the correct
 * IContext.
 */
public interface IPath
{
  /**
   * Set the parent expression of this path.
   * @param parent The parent expression.
   */
  public void setParent( IExpression parent);
  
  /**
   * Returns the parent expression of this path (possibly null).
   * @return Returns the parent expression of this path (possibly null).
   */
  public IExpression getParent();
  
  /**
   * Returns true if this path is absolute. The first element in an absolute path is always anchored 
   * at the root of the domain model.
   * @param context Null or the context to be tested.
   * @return Returns true if this path is absolute.
   */
  public boolean isAbsolute( IContext context);
  
  /**
   * Returns null or the path element at the specified index.
   * @param index The index of the path element.
   * @return Returns null or the path element at the specified index.
   */
  public IPathElement getPathElement( int index);
  
  /**
   * Return true if the specified object is a leaf of the path.
   * @param object The object to be tested.
   * @return Returns true if the object is a leaf of the path.
   */
  public boolean isLeaf( IModelObject object);
  
  /**
   * Return true if the specified object is a node on the path.
   * @param object The object to be tested.
   * @return Returns true if the object is a node on the path.
   */
  public boolean isNode( IModelObject object);
  
  /**
   * Returns a new path which is the inverse of this path.  The inverse path
   * navigates in the opposite direction from its inverse.  Note that an 
   * inverse path cannot reproduce the root of a query of the path from which
   * it was derived because that information is not available.
   * @return Returns the inverse of this path.
   */
  public IPath invertPath();
  
  /**
   * Create the subtree represented by this path beneath the specified root. Only the elements
   * which do not already exist are created. The first leaf of the subtree is returned.
   * @param root The root of the new subtree.
   * @return Returns the first leaf of the subtree.
   */
  public IModelObject createSubtree( IModelObject root);

  /**
   * Returns the variable source for this path.
   * @return Returns the variable source for this path.
   */
  public IVariableSource getVariableSource();
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   */
  public void setVariable( String name, String value);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   */
  public void setVariable( String name, Number value);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param value The value of the variable.
   */
  public void setVariable( String name, Boolean value);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param node A node.
   */
  public void setVariable( String name, IModelObject node);
  
  /**
   * Set a local variable.
   * @param name The name of the variable.
   * @param nodes A list of nodes.
   */
  public void setVariable( String name, List<IModelObject> nodes);
  
  /**
   * Set the variable to the result of the specified expression.
   * @param name The name of the variable.
   * @param expression The expression.
   */
  public void setVariable( String name, IExpression expression);

  /**
   * Return the leaf objects which are selected by this path.
   * @param object The root of the path.
   * @param result Null or a list where the objects are stored.
   * @return Returns the leaf objects of this path.
   */
  public List<IModelObject> query( IModelObject object, List<IModelObject> result);

  /**
   * Return the leaf objects which are selected by the path segment which begins with
   * the first element of this path and includes 'length' number of elements.  If the given
   * length is longer than the length of the path, then the entire path is used.
   * @param object The root of the path.
   * @param length The length of the subpath to query.
   * @param result Null or a list where the objects are stored.
   * @return Returns the leaf objects selected by the given subpath.
   */
  public List<IModelObject> query( IModelObject object, int length, List<IModelObject> result);
  
  /**
   * Return the leaf objects which are selected by this path.
   * @param context The parent context or null.
   * @param result Null or a list where the objects are stored.
   * @return Returns the leaf objects of this path.
   */
  public List<IModelObject> query( IContext context, List<IModelObject> result);

  /**
   * Return the leaf objects which are selected by the path segment which begins with
   * the first element of this path and includes 'length' number of elements.  If the given
   * length is longer than the length of the path, then the entire path is used.
   * @param parent The parent context or null.
   * @param object The root of the path.
   * @param length The length of the subpath to query.
   * @param result Null or a list where the objects are stored.
   * @return Returns the leaf objects selected by the given subpath.
   */
  public List<IModelObject> query( IContext context, int length, List<IModelObject> result);
  
  /**
   * Return the first leaf object which is selected by this path.
   * @param object The root of the path.
   * @return Null or the first selected object.
   */
  public IModelObject queryFirst( IModelObject object);
  
  /**
   * Return the number of elements that make up this path.
   * @return Return the number of elements that make up this path.
   */
  public int length();

  /**
   * Create the listener structure rooted on the given object for the given IPathListener. The
   * listener will receive initial notifications for each object on the path. Thereafter, the
   * listener will be updated whenever an object is added or removed from the path. Listener
   * notifications will be provided at the default priority of 0.
   * @param context The root of the path.
   * @param listener The listener which will receive notifications.
   */
  public void addPathListener( IContext context, IPathListener listener);

  /**
   * Remove the listener structure rooted on the given object for the given IPathListener.
   * @param context The root of the path.
   * @param listener The listener which was receiving notifications.
   */
  public void removePathListener( IContext context, IPathListener listener);
  
  /**
   * Returns a clone of this path including indexes.
   * @return Returns a clone of this path.
   */
  public IPath clone();
}
