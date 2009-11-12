/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SetCheck.java
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
 * A ConstraintCheck which validates a set constraint.
 */
public class SetCheck extends ConstraintCheck
{
  public SetCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ConstraintCheck#validateOnce(org.xmodel.IModelObject, int, int)
   */
  @Override
  public boolean validateOnce( IModelObject documentLocus, int start, int end)
  {
    // init
    if ( unsatisfiedConstraints != null) unsatisfiedConstraints.clear();
    
    // create a list of the constraints which can be culled
    List<ConstraintCheck> remaining = new ArrayList<ConstraintCheck>();
    for( int i=0; i<constraints.length; i++) remaining.add( constraints[ i]);
    
    // match all constraints to a consequetive subset of the children
    index = start; // bdunnagan: added 082808
    while( remaining.size() > 0 && index < end) 
    {
      int matching = findMatching( remaining, documentLocus, index, end);
      if ( matching < 0) return false;
      ConstraintCheck constraint = remaining.remove( matching);
      index = constraint.getIndex();
    }
    
    // make sure all required constraints are matched
    for( int i=0; i<remaining.size(); i++)
      if ( remaining.get( i).getMinOccurrences() > 0)
      {
        if ( unsatisfiedConstraints == null) unsatisfiedConstraints = new ArrayList<ConstraintCheck>();
        unsatisfiedConstraints.add( remaining.get( i));
      }
    
    // backup index to last satisifed constraint
    if ( unsatisfiedConstraints != null && unsatisfiedConstraints.size() > 0)
    {
      // this causes redundant error reporting
      //errorLocus = documentLocus;
      return false;
    }
    
    return (errored == null || errored.size() == 0);
  }

  /**
   * Find one of the remaining constraints that matches the child at the specified index.
   * @param remaining The list of remaining constraints.
   * @param documentLocus The document locus (parent).
   * @param index The index of the child.
   * @param end The index of the last child plus one.
   * @return Returns the index of the constraint or -1.
   */
  private int findMatching( List<ConstraintCheck> remaining, IModelObject documentLocus, int index, int end)
  {
    for( int i=0; i<remaining.size(); i++)
    {
      ConstraintCheck check = remaining.get( i);
      if ( check.validate( documentLocus, index, end) && check.getOccurrences() > 0)
        return i;
      
      // add failed constraint when occurrence count > 0
      if ( check.getOccurrences() > 0)
        addFailed( check);
    }
    return -1;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    super.getErrors( errors);
    
    // create error for incomplete set
    if ( errorLocus != null)
      errors.add( new SchemaError( Type.illegalElement, getSchemaLocus(), errorLocus));
    
    // create errors for unsatisfied constraints
    if ( unsatisfiedConstraints != null)
      for( ConstraintCheck constraint: unsatisfiedConstraints)
        errors.add( new SchemaError( Type.missingElement, constraint.getSchemaLocus(), errorLocus));
  }
  
  private List<ConstraintCheck> unsatisfiedConstraints;
}
