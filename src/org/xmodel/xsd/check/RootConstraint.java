/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * RootConstraint.java
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

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xsd.check.SchemaError.Type;


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
   * @see org.xmodel.xsd.check.ConstraintCheck#validate(org.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validate( IModelObject documentLocus, int start, int end)
  {
    return validateOnce( documentLocus, start, end);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#validate(org.xmodel.IModelObject, int, int)
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
   * @see org.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
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
