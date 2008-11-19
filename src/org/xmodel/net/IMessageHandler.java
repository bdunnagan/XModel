/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
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
