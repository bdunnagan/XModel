/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xsd.check.SchemaError.Type;

/**
 * A constraint class for the root constraint of an element schema.
 */
public class RootConstraint extends ConstraintCheck
{
  public RootConstraint( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.check.ConstraintCheck#validate(dunnagan.bob.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validate( IModelObject documentLocus, int start, int end)
  {
    return validateOnce( documentLocus, start, end);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ConstraintCheck#validate(dunnagan.bob.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( IModelObject documentLocus, int start, int end)
  {
    if ( illegalChildren != null) illegalChildren.clear();
    
    ConstraintCheck constraint = constraints[ 0];
    if ( constraint.validate( documentLocus, start, end))
    {
      index = constraint.getIndex();
      if ( index < end)
      { 
        for( int i=index; i<end; i++) 
        {
          if ( illegalChildren == null) illegalChildren = new ArrayList<IModelObject>();
          illegalChildren.add( documentLocus.getChild( i));
        }
        return false;
      }
      return true;
    }
    
    addFailed( constraint);
    return false;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    super.getErrors( errors);
    
    // report occurrence errors
    if ( errorLocus != null)
    {
      if ( constraints[ 0].getOccurrences() < constraints[ 0].getMinOccurrences())
      {
        errors.add( new SchemaError( Type.missingElement, getSchemaLocus(), errorLocus));
      }
      else if ( constraints[ 0].getOccurrences() > constraints[ 0].getMaxOccurrences())
      {
        errors.add( new SchemaError( Type.illegalElement, getSchemaLocus(), errorLocus));
      }
    }
    
    // create errors for illegal children
    if ( illegalChildren != null)
      for( IModelObject illegalChild: illegalChildren)
        errors.add( new SchemaError( Type.illegalElement, getSchemaLocus(), illegalChild));
  }
  
  private List<IModelObject> illegalChildren;
}
