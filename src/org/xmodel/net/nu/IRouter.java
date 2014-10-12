package org.xmodel.net.nu;

import java.util.Iterator;

public interface IRouter
{
  public void addRoute( String route, ITransport transport);
  
  public void removeRoute( String route, ITransport transport);
  
  public void removeRoutes( ITransport transport);
  
  public Iterator<ITransport> resolve( String route);
}
