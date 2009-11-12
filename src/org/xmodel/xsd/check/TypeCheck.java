/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TypeCheck.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
