package org.xmodel.net.transport.amqp;

import java.io.IOException;

import org.xmodel.net.XioPeer;
import org.xmodel.xaction.ClientAction.IClientTransport;
import org.xmodel.xaction.IXAction;
import org.xmodel.xpath.expression.IContext;

public class AmqpClientTransport extends AmqpTransport implements IClientTransport
{
  public AmqpClientTransport()
  {
    super( Role.client);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ClientAction.IClientTransport#connect(org.xmodel.xpath.expression.IContext, java.lang.String, 
   * org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction)
   */
  @Override
  public XioPeer connect( 
      final IContext context, 
      final String name, 
      final IXAction onConnect, 
      final IXAction onDisconnect, 
      final IXAction onError, 
      final IXAction onRegister, 
      final IXAction onUnregister) throws IOException
  {
    return connect( context, name, onRegister, onUnregister);
  }

//  private static Log log = Log.getLog( AmqpClientTransport.class);
}
