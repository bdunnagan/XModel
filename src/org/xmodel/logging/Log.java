package org.xmodel.logging;

import org.apache.log4j.Logger;

public class Log
{
  public static Logger getLog( String name)
  {
    return Logger.getLogger( name);
  }
}
