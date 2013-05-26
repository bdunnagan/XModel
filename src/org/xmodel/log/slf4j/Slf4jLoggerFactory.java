package org.xmodel.log.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.xmodel.log.Log;

public class Slf4jLoggerFactory implements ILoggerFactory
{
  /* (non-Javadoc)
   * @see org.slf4j.ILoggerFactory#getLogger(java.lang.String)
   */
  @Override
  public Logger getLogger( String arg0)
  {
    return new Slf4jLogger( Log.getLog( arg0));
  }
}
