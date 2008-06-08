/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.robust;

import java.util.Random;


/**
 * An object which simulates a poor network connection by periodically closing the
 * session socket.  This has the effect of forcing the session to reestablish itself.
 */
public class Simulator implements Runnable
{
  public Simulator( ISession session)
  {
    this.session = session;
  }
  
  /**
   * Start the simulator.
   */
  public void start()
  {
    Thread thread = new Thread( this, "Simulator");
    thread.start();
  }
  
  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  public void run()
  {
    Random random = new Random();
    while( true)
    {
      int time = random.nextInt( 3000);
      try { Thread.sleep( time);} catch( Exception e) {}
      session.bounce();
    }
  }
  
  private ISession session;
}
