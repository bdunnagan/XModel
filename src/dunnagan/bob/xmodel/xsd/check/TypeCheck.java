/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xsd.check.SchemaError.Type;

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
   * @see dunnagan.bob.xmodel.xsd.nu.ICheck#validateImpl(dunnagan.bob.xmodel.IModelObject)
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
   * @see dunnagan.bob.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    if ( errorLocus != null) 
      errors.add( new SchemaError( Type.invalidValue, check.getSchemaLocus(), errorLocus));
  }
  
  private ICheck check;
}
