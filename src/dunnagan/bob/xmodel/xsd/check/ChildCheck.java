/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xsd.check.SchemaError.Type;

public class ChildCheck extends ConstraintCheck
{
  public ChildCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    childType = Xlate.get( schemaLocus, "");
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ConstraintCheck#validateOnce(dunnagan.bob.xmodel.IModelObject, int, int)
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
   * @see dunnagan.bob.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
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
