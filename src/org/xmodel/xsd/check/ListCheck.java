/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ListCheck.java
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
import org.xmodel.INode;
import org.xmodel.xsd.check.SchemaError.Type;


/**
 * A ConstraintCheck which validates a list constraint.
 */
public class ListCheck extends ConstraintCheck
{
  public ListCheck( INode schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#validateOnce(org.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( INode documentLocus, int start, int end)
  {
    // init
    if ( unsatisfiedConstraints != null) unsatisfiedConstraints.clear();
    
    index = start;
    int i=0;
    for( ; i<constraints.length && index < end; i++)
    {
      ConstraintCheck constraint = constraints[ i];
      if ( constraint.validate( documentLocus, index, end))
      {
        // handle constraint preceded by ANY
        int anyCount = constraint.anyCount();
        if ( anyCount >= 0)
        {
          boolean found = false;
          for( int j=++i; !found && j<constraints.length; j++)
          {
            constraint = constraints[ j];
            for( int k=0; k<anyCount; k++)
            {
              if ( constraint.validate( documentLocus, index+k, end))
              {
                if ( constraint.getIndex() > (index+k)) found = true;
                break;
              }
            }
          }
          if ( !found)
          {
            index += anyCount;
            addFailed( constraint);
            return false;
          }
        }
        
        // advance the child index
        index = constraint.getIndex();
      }
      else
      {
        addFailed( constraint);
        return false;
      }
    }

    // return false if any of the remaining constraints have min occurrence > 0
    for( int j=i; j<constraints.length; j++)
      if ( constraints[ j].getMinOccurrences() > 0)
      {
        if ( unsatisfiedConstraints == null) unsatisfiedConstraints = new ArrayList<ConstraintCheck>();
        unsatisfiedConstraints.add( constraints[ j]);
      }
    
    // backup index to last satisifed constraint
    if ( unsatisfiedConstraints != null && unsatisfiedConstraints.size() > 0)
    {
      errorLocus = documentLocus;
      return false;
    }
    
    return (errored == null || errored.size() == 0);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    super.getErrors( errors);
    
    // create errors for unsatisfied constraints
    if ( unsatisfiedConstraints != null)
      for( ConstraintCheck constraint: unsatisfiedConstraints)
        errors.add( new SchemaError( Type.missingElement, constraint.getSchemaLocus(), errorLocus));
  }
  
  private List<ConstraintCheck> unsatisfiedConstraints;
}
