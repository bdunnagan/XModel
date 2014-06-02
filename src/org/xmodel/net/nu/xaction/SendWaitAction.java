package org.xmodel.net.nu.xaction;

import org.xmodel.xpath.expression.IContext;

public class SendWaitAction extends SendAction
{
  @Override
  protected Object[] doAction( IContext context)
  {
    return send( context, true);
  }
}
