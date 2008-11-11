package org.xmodel.net.robust;

import java.net.InetSocketAddress;
import java.util.Random;
import org.xmodel.IModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.TabularCompressor.PostCompression;
import org.xmodel.net.robust.XmlServer.IReceiver;
import org.xmodel.util.Radix;

/**
 * A server session for the XmlServer class.
 */
public class XmlServerSession extends ServerSession
{
  public XmlServerSession( Server server, InetSocketAddress address, long sid)
  {
    super( server, address, sid);
    
    addListener( new IListener() {
      public void notifyOpen( ISession session)
      {
        // randomize the message number to minimize likelihood of send-and-wait confusion
        Random random = new Random();
        mid = random.nextLong();
      }
      public void notifyClose( ISession session)
      {
      }
      public void notifyConnect( ISession session)
      {
        compressor = new TabularCompressor( PostCompression.none);
        loop = new ServerMessageLoop(); 
        loop.start();
      }
      public void notifyDisconnect( ISession session)
      {
        loop.stop();
        loop = null;
      }
    });
  }
  
  /**
   * Send a message to the server.
   * @param message The message.
   */
  public void send( IModelObject message)
  {
    message.setID( Radix.convert( mid++, 36));
    byte[] compressed = compressor.compress( message);
    write( compressed);
  }
  
  /**
   * A server session message loop.
   */
  private class ServerMessageLoop extends MessageLoop
  {
    public ServerMessageLoop()
    {
      super( "Message Loop ("+Radix.convert( getSessionNumber(), 36)+")", XmlServerSession.this); 
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.net.robust.MessageLoop#handle(org.xmodel.IModelObject)
     */
    @Override
    public void handle( IModelObject message)
    {
      XmlServer server = (XmlServer)getServer();
      IReceiver receiver = server.getReceiver( message.getType());
      if ( receiver != null) receiver.handle( server, XmlServerSession.this, message);
    }

    /* (non-Javadoc)
     * @see org.xmodel.net.robust.MessageLoop#terminated(java.lang.Throwable)
     */
    @Override
    public void terminated( Throwable t)
    {
      try { close();} catch( Exception e) {}
    }
  }
  
  private long mid;
  private MessageLoop loop;
  private ICompressor compressor;
}
