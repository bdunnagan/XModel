/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import dunnagan.bob.xmodel.NullObject;

/**
 * An empty context.
 */
public class NullContext extends Context
{
  protected NullContext()
  {
    super( new NullObject());
  }
  
  public static NullContext getInstance()
  {
    NullContext instance = instances.get();
    if ( instance == null)
    {
      instance = new NullContext();
      instances.set( instance);
    }
    return instance;
  }
  
  private static ThreadLocal<NullContext> instances = new ThreadLocal<NullContext>();
}
