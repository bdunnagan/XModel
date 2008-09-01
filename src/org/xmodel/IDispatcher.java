/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel;

/**
 * An interface which the server uses to pass queries to the xmodel thread.
 */
public interface IDispatcher
{
  /**
   * Execute the specified runnable in the xmodel thread.
   * @param runnable The runnable.
   */
  public void execute( Runnable runnable);
}
