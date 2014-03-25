package org.xmodel.net.connection;

import java.util.concurrent.atomic.AtomicReference;

import org.xmodel.future.AsyncFuture;

public class RequestFuture extends AsyncFuture<Object>
{
  public RequestFuture( AbstractNetworkConnection connection, Object request, Object correlation)
  {
    super( request);
    
    this.correlation = correlation;
    this.connection = connection;
    this.responseRef = new AtomicReference<Object>();
  }
  
  /**
   * Set the response message.
   * @param response The response message.
   */
  void setResponse( Object response)
  {
    responseRef.set( response);
  }
  
  /**
   * @return Returns null or the response message.
   */
  public Object getResponse()
  {
    return responseRef.get();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.future.AsyncFuture#cancel()
   */
  @Override
  public void cancel()
  {
    if ( correlation != null) connection.removeRequestFuture( correlation);
    super.cancel();
  }
 
  private Object correlation;
  private final AbstractNetworkConnection connection;
  private AtomicReference<Object> responseRef;
}