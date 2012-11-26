package org.xmodel.net;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;

/**
 * Factory for SSLContext for XIO protocol.
 */
class SSLContextFactory
{
  public SSLContextFactory()
  {
  }
  
  /**
   * Configure this factory from the specified NetworkCachingPolicy annotation.
   * @param annotation The annotation
   */
  public void configure( IModelObject annotation) throws Exception
  {
    IModelObject config = annotation.getFirstChild( "ssl");
    
    // algorithm
    String algorithm = Xlate.get( config, "algorithm", Xlate.childGet( config, "algorithm", "SunX509"));
    
    // keystore
    String file = Xlate.childGet( config, "keystore", ".keystore");
    String password = Xlate.get( config.getFirstChild( "keystore"), "password", (String)null);
    KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType());    
    keystore.load( new FileInputStream( file), password.toCharArray());

    // key manager factory
    String serverCert
    
    String certPassword = Xlate.childGet( config, "certificate",  )
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( algorithm);
    keyManagerFactory.init( keystore, SecureChatKeyStore.getCertificatePassword());

    // initialize ssl context
    sslContext = SSLContext.getInstance( "TLS");
    sslContext.init(keyManagerFactory.getKeyManagers(), null, null);  
  }
}
