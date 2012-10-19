package org.xmodel.net.execution;

import java.io.IOException;
import java.util.Collections;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.compress.ICompressor;
import org.xmodel.log.SLog;
import org.xmodel.net.Session;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xaction.XActionException;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;

/**
 * This class holds the remote execution protocol state for a channel.
 */
public final class RemoteExecutionSession
{
  public RemoteExecutionSession( Channel channel, IContext context)
  {
    //
    // Here is where a slave context might be created.
    //    
    this.context = context;
  }

  public void sendRequest()
  {
  }
  
  /**
   * Handle a remote execution request.
   * @param received The buffer containing the request.
   */
  public void handleRequest( ChannelBuffer received) throws IOException
  {
    buffer.writeBytes( received);
    
    int length = readLength( buffer);
    if ( length < 0) return;
    
    IModelObject script = ExecutionProtocol.readRequest( compressor.decompress( buffer), context);
    
    // check privilege
    if ( privilege != null && !privilege.isPermitted( context, script))
    {
      try
      {
        sendError( sender, session, correlation, "Script contains restricted operations.");
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
    
    // compile script and execute
    XActionDocument doc = new XActionDocument( script);
    for( String packageName: packageNames)
      doc.addPackage( packageName);

    Object[] results = null;
    try
    {
      IXAction action = doc.getAction( script);
      if ( action == null) 
      {
        throw new XActionException( String.format(
          "Unable to resolve IXAction class: %s.", script.getType()));
      }

      // execute
      results = action.run( context);
    }
    catch( Throwable t)
    {
      SLog.errorf( this, "Execution failed for script: %s", XmlIO.write( Style.compact, script));
      SLog.exception( this, t);
      
      try
      {
        sendError( sender, session, correlation, String.format( "%s: %s", t.getClass().getName(), t.getMessage()));
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
      }
    }
    finally
    {
      // cleanup
      context.set( "session", Collections.<IModelObject>emptyList());
    }
    
    try
    {
      sendExecuteResponse( sender, session, correlation, context, results);
    } 
    catch( IOException e)
    {
      SLog.errorf( this, "Unable to send execution response for script: %s", XmlIO.write( Style.compact, script));
      SLog.exception( this, e);
    }
  }
  
  public void sendResponse()
  {
  }
  
  /**
   * Handle a remote execution response.
   * @param buffer The buffer containing the response.
   */
  public void handleResponse( ChannelBuffer buffer)
  {
  }

  /**
   * Read a message length from the specified buffer.
   * @param buffer The buffer.
   * @return Returns the message length.
   */
  public static int readLength( ChannelBuffer buffer)
  {
    int count = buffer.readableBytes();
    if ( count > 4) count = 4;
    
    int length = 0;
    for( int i=0; i<count; i++)
    {
      byte b = buffer.getByte( buffer.readerIndex() + i);
      if ( (b & 0x80) == 0) 
      {
        buffer.readerIndex( buffer.readerIndex() + i);
        return length + b;
      }
      length += (b & 0x7f);
      length <<= 8;
    }

    return -1;
  }
  
  /**
   * Write a message length to the specified buffer.
   * @param buffer The buffer.
   * @param length The message length.
   */
  public static void writeLength( ChannelBuffer buffer, int length)
  {
    for( int i=0; i<4; i++)
    {
      int b = length & 0x7f;
      if ( length < 128) 
      {
        buffer.writeByte( (byte)b);
        break;
      }
      else
      {
        buffer.writeByte( (byte)(b | 0x80));
      }
      length >>= 7;
    }
  }
  
  private IContext context;
  private ICompressor compressor;
  private ChannelBuffer buffer;
  private ExecutionPrivilege privilege;
}
