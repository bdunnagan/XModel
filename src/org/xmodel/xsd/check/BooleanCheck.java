/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import org.xmodel.IModelObject;
import org.xmodel.Xlate;

public class BooleanCheck extends AbstractCheck
{
  public BooleanCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    String value = Xlate.get( documentLocus, "");
    return value.equals( "true") || value.equals( "false");
  }
}
