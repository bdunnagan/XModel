package org.xmodel.net;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * A trust store that trusts all certificates.  Useful for one-way, client authentication.
 */
public class OpenTrustStore implements X509TrustManager
{
  /* (non-Javadoc)
   * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
   */
  @Override
  public void checkClientTrusted( X509Certificate[] certificate, String authType) throws CertificateException
  {
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
   */
  @Override
  public void checkServerTrusted( X509Certificate[] certificate, String authType) throws CertificateException
  {
  }

  /* (non-Javadoc)
   * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
   */
  @Override
  public X509Certificate[] getAcceptedIssuers()
  {
    return null;
  }
}
