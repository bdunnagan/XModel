/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import dunnagan.bob.xmodel.IModelObject;

public class ChoiceCheck extends ConstraintCheck
{
  public ChoiceCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ConstraintCheck#validateOnce(dunnagan.bob.xmodel.IModelObject, int, int)
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
