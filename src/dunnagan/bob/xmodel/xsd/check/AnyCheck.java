/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import dunnagan.bob.xmodel.IModelObject;

public class AnyCheck extends ConstraintCheck
{
  public AnyCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ConstraintCheck#validateOnce(dunnagan.bob.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( IModelObject documentLocus, int start, int end)
  {
    // TODO: need to handle different types of any constraints
    count = end - start; 
    return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ConstraintCheck#anyCount()
   */
  @Override
  public int anyCount()
  {
    return count;
  }
  
  private int count;
}
