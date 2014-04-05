package org.xmodel.log.slf4j;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.xmodel.log.Log;

public class Slf4jLogger extends MarkerIgnoringBase
{
  private static final long serialVersionUID = 1414314618184177049L;

  public Slf4jLogger( Log log)
  {
    this.log = log;
  }
  
  /* (non-Javadoc)
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void debug( String arg0, Object arg1, Object arg2)
  {
    log.debugf( arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
   */
  @Override
  public void debug( String arg0, Object... arg1)
  {
    log.debugf( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
   */
  @Override
  public void debug( String arg0, Object arg1)
  {
    log.debugf( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void debug( String arg0, Throwable arg1)
  {
    log.error( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#debug(java.lang.String)
   */
  @Override
  public void debug( String arg0)
  {
    log.debug( arg0);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void error( String arg0, Object arg1, Object arg2)
  {
    log.errorf( arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
   */
  @Override
  public void error( String arg0, Object... arg1)
  {
    log.errorf( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
   */
  @Override
  public void error( String arg0, Object arg1)
  {
    log.errorf( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void error( String arg0, Throwable arg1)
  {
    log.error( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#error(java.lang.String)
   */
  @Override
  public void error( String arg0)
  {
    log.error( arg0);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void info( String arg0, Object arg1, Object arg2)
  {
    log.infof( arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
   */
  @Override
  public void info( String arg0, Object... arg1)
  {
    log.infof( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
   */
  @Override
  public void info( String arg0, Object arg1)
  {
    log.infof( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void info( String arg0, Throwable arg1)
  {
    log.error( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#info(java.lang.String)
   */
  @Override
  public void info( String arg0)
  {
    log.infof( arg0);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#isDebugEnabled()
   */
  @Override
  public boolean isDebugEnabled()
  {
    return log.debug();
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#isErrorEnabled()
   */
  @Override
  public boolean isErrorEnabled()
  {
    return log.error();
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#isInfoEnabled()
   */
  @Override
  public boolean isInfoEnabled()
  {
    return log.info();
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#isTraceEnabled()
   */
  @Override
  public boolean isTraceEnabled()
  {
    return log.verbose();
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#isWarnEnabled()
   */
  @Override
  public boolean isWarnEnabled()
  {
    return log.warn();
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void trace( String arg0, Object arg1, Object arg2)
  {
    log.verbosef( arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
   */
  @Override
  public void trace( String arg0, Object... arg1)
  {
    log.verbosef( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
   */
  @Override
  public void trace( String arg0, Object arg1)
  {
    log.verbosef( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void trace( String arg0, Throwable arg1)
  {
    log.error( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#trace(java.lang.String)
   */
  @Override
  public void trace( String arg0)
  {
    log.verbosef( arg0);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object, java.lang.Object)
   */
  @Override
  public void warn( String arg0, Object arg1, Object arg2)
  {
    log.warnf( arg0, arg1, arg2);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
   */
  @Override
  public void warn( String arg0, Object... arg1)
  {
    log.warnf( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
   */
  @Override
  public void warn( String arg0, Object arg1)
  {
    log.warnf( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
   */
  @Override
  public void warn( String arg0, Throwable arg1)
  {
    log.error( arg0, arg1);
  }

  /* (non-Javadoc)
   * @see org.slf4j.Logger#warn(java.lang.String)
   */
  @Override
  public void warn( String arg0)
  {
    log.warnf( arg0);
  }
  
  private Log log;
}
