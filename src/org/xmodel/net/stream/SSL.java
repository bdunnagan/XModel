package org.xmodel.net.stream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.TrustManagerFactory;

/**
 * Convenience class for handling SSL.
 */
public class SSL
{
  public enum Authentication { none, requested, required};
  private final static String keyManagerAlgorithm = "SunX509";
  private final static String sslProtocol = "TLS";
  
  /**
   * Create an SSL instance with the specified CA instances, CA password, and client authentication.
   * @param keyStore The private key CA.
   * @param keyStorePass The password to the key-store.
   * @param trustStore The third-party CA.
   * @param trustStorePass The password to the trust-store.
   * @param auth Client authentication mode.
   */
  public SSL( String keyStore, String keyStorePass, String trustStore, String trustStorePass, Authentication auth) 
  throws GeneralSecurityException, IOException
  {
    this( new FileInputStream( keyStore), keyStorePass, new FileInputStream( trustStore), trustStorePass, auth);
  }
  
  /**
   * Create an SSL instance with the specified CA instances, CA password, and client authentication.
   * @param keyStore The private key CA.
   * @param keyStorePass The password to the key-store.
   * @param trustStore The third-party CA.
   * @param trustStorePass The password to the trust-store.
   * @param auth Client authentication mode.
   */
  public SSL( InputStream keyStore, String keyStorePass, InputStream trustStore, String trustStorePass, Authentication auth) 
  throws GeneralSecurityException, IOException
  {
    KeyStore ks = KeyStore.getInstance( "JKS");
    KeyStore ts = KeyStore.getInstance( "JKS");

    ks.load( keyStore, keyStorePass.toCharArray());
    ts.load( trustStore, trustStorePass.toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance( keyManagerAlgorithm);
    kmf.init( ks, keyStorePass.toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance( keyManagerAlgorithm);
    tmf.init( ts);

    SSLContext sslContext = SSLContext.getInstance( sslProtocol);
    sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    
    engine = sslContext.createSSLEngine();
    switch( auth)
    {
      case none:      break;
      case requested: engine.setWantClientAuth( true); break;
      case required:  engine.setNeedClientAuth( true); break;
    }
  }
  
  /**
   * Returns the SSLEngine so that parameters can be configured, such as client-mode and client-authentication.
   * @return Returns the SSLEngine.
   */
  public SSLEngine getSSLEngine()
  {
    return engine;
  }

  /**
   * Read data from specified channel and handle SSL handshaking.
   * @param channel The channel.
   * @param connection The connection that is being read.
   * @return Returns the number of bytes read;
   */
  public int read( SocketChannel channel, Connection connection) throws IOException
  {
    if ( buffer == null)
    {
      buffer = ByteBuffer.allocate( engine.getSession().getPacketBufferSize());
    }
    
    int nread = channel.read( buffer);
    if ( nread == -1)
    {
      engine.closeInbound();
      return -1;
    }

    buffer.flip();
    SSLEngineResult sslResult = engine.unwrap( buffer, connection.buffer);
    runDelegatedTasks( sslResult);
    
    buffer.flip();
    connection.buffer.flip();
    
    switch( sslResult.getStatus()) 
    {
      case BUFFER_OVERFLOW:
      {
        int required = engine.getSession().getApplicationBufferSize();
        ByteBuffer larger = ByteBuffer.allocate( required + connection.buffer.position());
        connection.buffer.flip();
        larger.put( connection.buffer);
        connection.buffer = larger;
        return read( channel, connection);
      }
      
      case BUFFER_UNDERFLOW:
      {
        int required = engine.getSession().getPacketBufferSize();
        if ( required > buffer.remaining()) 
        {
          ByteBuffer larger = ByteBuffer.allocate( buffer.position() + required);
          buffer.flip();
          larger.put( buffer);
          buffer = larger;
        }
        return 0;
      }
      
      case CLOSED:
      {
        return -1;
      }
    }

    sslResult = engine.wrap( connection.buffer, buffer);
    
    return sslResult.bytesProduced();
  }
  
  /**
   * Write data to the specified channel and handle SSL handshaking.
   * @param channel The channel.
   * @param data The data to be sent.
   */
  public void write( SocketChannel channel, ByteBuffer data) throws IOException
  {
    if ( buffer == null)
    {
      buffer = ByteBuffer.allocate( engine.getSession().getPacketBufferSize());
    }
    
    SSLEngineResult sslResult = engine.wrap( data, buffer);
    runDelegatedTasks( sslResult);
    
    switch( sslResult.getStatus()) 
    {
      case BUFFER_OVERFLOW:
      {
        int required = engine.getSession().getPacketBufferSize();
        ByteBuffer larger = ByteBuffer.allocate( buffer.position() + required);
        buffer.flip();
        larger.put( buffer);
        buffer = larger;
        
        write( channel, data);
        return;
      }
      
      case CLOSED:
      {
        return;
      }
    }
    
    buffer.flip();
    channel.write( buffer);
  }
  
  /**
   * Handle SSL tasks.
   * @param result The SSLEngine result from the last read.
   */
  private void runDelegatedTasks( SSLEngineResult result)
  {
    if ( result.getHandshakeStatus() == HandshakeStatus.NEED_TASK)
    {
      Runnable runnable;
      while ( (runnable = engine.getDelegatedTask()) != null)
      {
        runnable.run();
      }
    }
  }

  public boolean isClosed()
  {
    return (engine.isOutboundDone() && engine.isInboundDone());
  }

  private SSLEngine engine;
  private ByteBuffer buffer;
}
