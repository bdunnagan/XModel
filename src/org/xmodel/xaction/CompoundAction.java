package org.xmodel.xaction;

import java.util.Iterator;

import org.xmodel.IModelObject;

/**
 * Base class for actions that involve a sequence of two or more elements in a script.
 */
public abstract class CompoundAction extends XAction
{
  /**
   * Configure this action by consuming elements from the iterator.
   * @param document The document.
   * @param iterator The script element iterator.
   */
  public abstract void configure( XActionDocument document, Iterator<IModelObject> iterator);
}
