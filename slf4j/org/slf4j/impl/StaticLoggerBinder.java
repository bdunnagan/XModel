package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.xmodel.log.slf4j.Slf4jLoggerFactory;

public class StaticLoggerBinder
{
  public static String REQUESTED_API_VERSION = "1.7";
  
  public ILoggerFactory getLoggerFactory()
  {
    return new Slf4jLoggerFactory();
  }
  
  public String getLoggerFactoryClassStr()
  {
    return Slf4jLoggerFactory.class.getCanonicalName();
  }
  
  public static StaticLoggerBinder getSingleton()
  {
    return instance;
  }
  
  private static StaticLoggerBinder instance = new StaticLoggerBinder();
}
