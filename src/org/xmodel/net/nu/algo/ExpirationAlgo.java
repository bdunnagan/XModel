package org.xmodel.net.nu.algo;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.xmodel.IModelObject;
import org.xmodel.log.Log;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransportImpl;

public class ExpirationAlgo extends DefaultEventHandler
{
  public ExpirationAlgo( ITransportImpl transport)
  {
    this.transport = transport;
    this.dateFormat = new SimpleDateFormat();
  }
  
  @Override
  public boolean notifyReceive( IModelObject envelope)
  {
    long expiry = transport.getProtocol().envelope().getExpiration( envelope);
    if ( expiry >= 0 && expiry < System.currentTimeMillis())
    {
      log.warnf( "Received expired message, date=%s", dateFormat.format( new Date( expiry)));
      return true;
    }
    
    return false;
  }

  public final static Log log = Log.getLog( ExpirationAlgo.class);
  
  private ITransportImpl transport;
  private SimpleDateFormat dateFormat;
}
