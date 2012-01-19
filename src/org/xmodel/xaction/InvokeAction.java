package org.xmodel.xaction;

import org.xmodel.log.Log;

/**
 * @deprecated Use RunAction instead.
 */
public class InvokeAction extends RunAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.RunAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    log.warn( "<invoke> is deprecated, use <run> instead.");
  }
  
  private static Log log = Log.getLog( "org.xmodel.xaction");
}
