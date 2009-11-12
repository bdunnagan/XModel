/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Simulator.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.net.robust;

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
