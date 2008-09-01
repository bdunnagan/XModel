/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import org.xmodel.IModelObject;

public class ChoiceCheck extends ConstraintCheck
{
  public ChoiceCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#validateOnce(org.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( IModelObject documentLocus, int start, int end)
  {
    index = start;
    for( int i=0; i<constraints.length; i++)
    {
      ConstraintCheck choice = constraints[ i];
      if ( choice.validateOnce( documentLocus, index, end)) 
      {
        index = choice.getIndex();
        return true;
      }
    }
    return false;
  }
}
