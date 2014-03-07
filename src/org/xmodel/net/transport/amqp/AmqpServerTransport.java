package org.xmodel.net.transport.amqp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmodel.net.IXioPeerRegistry;
import org.xmodel.net.XioPeer;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ServerAction.IServerTransport;
import org.xmodel.xpath.expression.IContext;

public class AmqpServerTransport extends AmqpTransport implements IServerTransport
{
  public AmqpServerTransport()
  {
    super( Role.server);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.ServerAction.IServerTransport#listen(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction, 
   * org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction, org.xmodel.xaction.IXAction)
   */
  @Override
  public IXioPeerRegistry listen( IContext context, IXAction onConnect, IXAction onDisconnect, IXAction onRegister, IXAction onUnregister)
    throws IOException
  {
    AmqpXioPeer peer = connect( context, null, onRegister, onUnregister);
    peers.add( peer);
    return peer.getPeerRegistry();
  }

  private List<XioPeer> peers = new ArrayList<XioPeer>();
  
//  private static Log log = Log.getLog( AmqpServerTransport.class);
}
