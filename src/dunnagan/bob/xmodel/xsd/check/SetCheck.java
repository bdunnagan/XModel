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
 * A ConstraintCheck which validates a set constraint.
 */
public class SetCheck extends ConstraintCheck
{
  public SetCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ConstraintCheck#validateOnce(dunnagan.bob.xmodel.IModelObject, int, int)
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
   * @see dunnagan.bob.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
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
