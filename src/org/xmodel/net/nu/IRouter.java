package org.xmodel.net.nu;

import java.util.Iterator;

public interface IRouter
{
  public void addRoute( String route, ITransport transport);
  
  public void removeRoute( String route, ITransport transport);
  
  public Iterator<String> removeRoutes( ITransport transport);
  
  public boolean hasRoute( String route);
  
  public Iterator<ITransport> resolve( String route);
}
