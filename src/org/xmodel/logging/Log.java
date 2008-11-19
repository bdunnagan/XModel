/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.logging;

import org.apache.log4j.Logger;

public class Log
{
  public static Logger getLog( String name)
  {
    return Logger.getLogger( name);
  }
}
