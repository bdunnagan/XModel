package org.xmodel.xaction;

import java.lang.management.ManagementFactory;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import org.xmodel.log.SLog;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * Start a custom JMX agent to facilitate remote monitoring through a firewall.
 * The code used herein is adapted from code under the following copyright:
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 */
public class JmxServerAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    addressExpr = document.getExpression( "address", true);
    portExpr = document.getExpression( "port", true);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    try
    {
      String address = addressExpr.evaluateString( context);
      int port = (int)portExpr.evaluateNumber( context);
      startAgent( address, port);
    }
    catch( Exception e)
    {
      throw new XActionException( e);
    }
    
    return null;
  }
  
  private void startAgent( String address, int port) throws Exception
  {
    // Ensure cryptographically strong random number generator used
    // to choose the object number - see java.rmi.server.ObjID
    //
    System.setProperty("java.rmi.server.randomIDs", "true");    
    
    SLog.infof( this, "Create RMI registry on port: %d", port);
    LocateRegistry.createRegistry( port);
      
    // Retrieve the PlatformMBeanServer.
    //
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

    // Create an RMI connector server.
    //
    // As specified in the JMXServiceURL the RMIServer stub will be
    // registered in the RMI registry running in the local host on
    // port 3000 with the name "jmxrmi". This is the same name the
    // out-of-the-box management agent uses to register the RMIServer
    // stub too.
    //
    // The port specified in "service:jmx:rmi://"+hostname+":"+port
    // is the second port, where RMI connection objects will be exported.
    // Here we use the same port as that we choose for the RMI registry. 
    // The port for the RMI registry is specified in the second part
    // of the URL, in "rmi://"+hostname+":"+port
    //
    SLog.infof( this, "Create an RMI connector server");
    JMXServiceURL url = new JMXServiceURL( String.format( "service:jmx:rmi://%s:%d/jndi/rmi://%s:%d/jmxrmi", address, port, address, port));
    
    // Now create the server from the JMXServiceURL
    //
    Map<String,Object> env = new HashMap<String, Object>();
    JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer( url, env, mbs);

    // Start the RMI connector server.
    //
    SLog.infof( this, "Start the RMI connector server on port: %d", port);
    connectorServer.start();  
  }

  private IExpression addressExpr;
  private IExpression portExpr;
}
