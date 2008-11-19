package org.xmodel.xaction.trigger;

import org.xmodel.Xlate;
import org.xmodel.xaction.XActionDocument;

/**
 * An base implementation of the ITrigger interface.
 */
public abstract class AbstractTrigger implements ITrigger
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    this.document = document;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.trigger.ITrigger#getDocument()
   */
  public XActionDocument getDocument()
  {
    return document;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return Xlate.get( document.getRoot(), "name", (String)null);
  }
  
  protected XActionDocument document;
}
