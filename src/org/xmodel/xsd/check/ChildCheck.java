/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.xsd.check.SchemaError.Type;


public class ChildCheck extends ConstraintCheck
{
  public ChildCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    childType = Xlate.get( schemaLocus, "");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#validateOnce(org.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( IModelObject documentLocus, int start, int end)
  {
    index = start;
    IModelObject child = documentLocus.getChild( index);
    if ( child != null && child.isType( childType)) 
    {
      index++;
      return true;
    }
    return false;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    if ( errorLocus != null)
    {
      if ( occurrences < getMinOccurrences()) 
        errors.add( new SchemaError( Type.missingElement, getSchemaLocus(), errorLocus));
      if ( occurrences > getMaxOccurrences()) 
        errors.add( new SchemaError( Type.illegalElement, getSchemaLocus(), errorLocus));
    }
  }
  
  private String childType;
}
