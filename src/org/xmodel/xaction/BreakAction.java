package org.xmodel.xaction;

import org.xmodel.xpath.expression.IContext;

/**
 * An XAction that behaves like the continue programming construct in a for loop.
 */
public class BreakAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( parent != null) parent.doBreak();
    return new Object[ 0];
  }
  
  /**
   * Set the parent ForAction.
   * @param parent The parent.
   */
  protected void setFor( ForAction parent)
  {
    this.parent = parent;
  }

  private ForAction parent;
}
