package org.xmodel.net;

import java.io.IOException;

import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;

/**
 * A class that represents a protocol session. 
 */
public final class Session
{
  public Session( Protocol protocol, ILink link, int session)
  {
    this.protocol = protocol;
    this.link = link;
    this.session = session;
  }
  
  /**
   * Attach to the element on the specified xpath.
   * @param xpath The XPath expression.
   * @param reference The reference.
   */
  public void attach( String xpath, IExternalReference reference) throws IOException
  {
    protocol.attach( link, session, xpath, reference);
  }

  /**
   * Detach the element that was previously attached.
   */
  public void detach() throws IOException
  {
    protocol.detach( link, session);
  }

  /**
   * Perform a remote invocation of the specified script.
   * @param script The script to be executed.
   * @param timeout The timeout to wait for a response.
   * @return Returns null or the response.
   */
  public Object execute( IModelObject script, int timeout) throws IOException
  {
    return protocol.execute( link, session, script, timeout);
  }
  
  /**
   * Send a debug step message.
   */
  public final void debugStepIn() throws IOException
  {
    protocol.sendDebugStepIn( link, session);
  }
  
  /**
   * Send a debug step message.
   */
  public final void debugStepOut() throws IOException
  {
    protocol.sendDebugStepOut( link, session);
  }
  
  /**
   * Send a debug step message.
   */
  public final void debugStepOver() throws IOException
  {
    protocol.sendDebugStepOver( link, session);
  }
  
  /**
   * Close this session.
   */
  public void close()
  {
    try { protocol.closeSession( link, session);} catch( IOException e) {}
  }

  private Protocol protocol;
  private ILink link;
  private int session;
}
