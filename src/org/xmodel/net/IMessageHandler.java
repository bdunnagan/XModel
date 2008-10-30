package org.xmodel.net;

import org.xmodel.IModelObject;
import org.xmodel.net.robust.ISession;

/**
 * An interface which handles messages of a specified type. The interface is registered
 * with either a ModelServer or ModelClient.
 */
public interface IMessageHandler
{
  /**
   * Called when a message is received for the specified session.
   * @param session The session that received the message.
   * @param message The message that was received.
   */
  public void notifyMessage( ISession session, IModelObject message);
}
