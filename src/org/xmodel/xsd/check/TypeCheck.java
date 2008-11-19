/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xsd.check.SchemaError.Type;


/**
 * An umbrella class for validating the three primary builtin types: boolean, number and string.
 */
public class TypeCheck extends AbstractCheck
{
  public TypeCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    
    if ( schemaLocus.isType( "boolean")) check = new BooleanCheck( schemaLocus);
    else if ( schemaLocus.isType( "number")) check = new NumberCheck( schemaLocus);
    else if ( schemaLocus.isType( "string")) check = new StringCheck( schemaLocus);
    else if ( schemaLocus.isType( "enum")) check = new EnumCheck( schemaLocus);
    else if ( schemaLocus.isType( "pattern")) check = new PatternCheck( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    if ( !check.validate( documentLocus))
    {
      addFailed( check);
      return false;
    }
    return true;
  }
  

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    if ( errorLocus != null) 
      errors.add( new SchemaError( Type.invalidValue, check.getSchemaLocus(), errorLocus));
  }
  
  private ICheck check;
}
