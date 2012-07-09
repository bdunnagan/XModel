package org.xmodel.net;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.external.IExternalReference;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

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
   * @return Returns the session number.
   */
  public int getID()
  {
    return session;
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
   * Perform a remote query.
   * @param context The local context.
   * @param query The query.
   * @param timeout The timeout.
   * @return Returns the query result.
   */
  public Object query( IContext context, String query, int timeout) throws IOException
  {
    return protocol.query( link, session, context, query, timeout);
  }
  
  /**
   * Perform a synchronous remote invocation of the specified script.
   * @param context The local execution context.
   * @param variables The variables to be passed.
   * @param script The script to be executed.
   * @param timeout The timeout to wait for a response.
   * @return Returns null or the response.
   */
  public Object[] execute( StatefulContext context, String[] variables, IModelObject script, int timeout) throws IOException
  {
    return protocol.execute( link, session, context, variables, script, timeout);
  }
  
  /**
   * Perform an asynchronous remote invocation of the specified script.
   * @param context The local execution context.
   * @param variables The variables to be passed.
   * @param script The script to be executed.
   * @param callback The async callback interface.
   * @param timeout The timeout to wait for a response.
   * @return Returns null or the response.
   */
  public void execute( StatefulContext context, String[] variables, IModelObject script, ICallback callback, int timeout) throws IOException
  {
    protocol.execute( link, session, context, variables, script, callback, timeout);
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
