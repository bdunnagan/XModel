/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;


/**
 * An ICheck used by other ICheck implementations that validate element children.
 */
public abstract class ConstraintCheck extends AbstractCheck
{
  public ConstraintCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    
    constraints = getConstraints();
    min = Xlate.get( schemaLocus, "min", 1);
    String maxString = Xlate.get( schemaLocus, "max", "1"); 
    max = maxString.equals( "unbounded")? -1: Integer.parseInt( maxString);
  }

  /**
   * Returns the minimum allowed occurrences of this constraint.
   * @return Returns the minimum allowed occurrences of this constraint.
   */
  public int getMinOccurrences()
  {
    return min;
  }
  
  /**
   * Returns the maximum allowed occurrences of this constraint.
   * @return Returns the maximum allowed occurrences of this constraint.
   */
  public int getMaxOccurrences()
  {
    return max;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    return validate( documentLocus, 0, documentLocus.getNumberOfChildren());
  }
  
  /**
   * Validate the cardinality of this constraint.
   * @param documentLocus The document locus.
   * @param start The index of the first child to be tested.
   * @param end The index of the last child to be tested.
   * @return Returns true if the constraint is satisfied.
   */
  public boolean validate( IModelObject documentLocus, int start, int end)
  {
    errorLocus = null;
    if ( errored != null) errored.clear();
    
    index = start;
    occurrences = 0;
    while( max < 0 || occurrences <= max)
    {
      start = index;
      if ( !validateOnce( documentLocus, index, end)) break;

      // constraint is satisfied if validation did not consume children
      if ( getIndex() == start) return true;
      
      // increment occurrences
      occurrences++;
    }

    // return true if occurrences in range
    if ( min <= occurrences && (max < 0 || occurrences <= max)) 
    {
      errored = null;
      return true;
    }
    
    // ensure index points to validation error
    if ( max >= 0 && occurrences > max) index--;
    
    // set error locus
    if ( errorLocus == null) errorLocus = documentLocus.getChild( index);
    return false;
  }
  
  /**
   * Validate as subset of the children of the specified document locus starting at the specified child.
   * @param documentLocus The document locus.
   * @param start The index of the first child to be tested.
   * @param end The index of the last child to be tested.
   * @return Returns true if the constraint is satisfied.
   */
  protected abstract boolean validateOnce( IModelObject documentLocus, int start, int end);
  
  /**
   * Returns the index of the last child to be tested during the previous call to <code>validate</code>.
   * @return Returns the index of the last child to be tested during the previous call to <code>validate</code>.
   */
  public int getIndex()
  {
    return index;
  }
  
  /**
   * Returns the number of occurrences of this constraint found during the last call to validate.
   * @return Returns the number of occurrences of this constraint found during the last call to validate.
   */
  public int getOccurrences()
  {
    return occurrences;
  }

  /**
   * Returns the number of children which would be matched by an <i>any</i> constraint. 
   * -1 indicates that this constraint check does not represent an any constraint.
   * @return Returns the number of children which would be matched by an <i>any</i> constraint.
   */
  public int anyCount()
  {
    return -1;
  }
  
  /**
   * Returns the constraints that are given by the children of the associated schema locus.
   * @return Returns the constraints that are given by the children of the associated schema locus.
   */
  private ConstraintCheck[] getConstraints()
  {
    List<IModelObject> children = getSchemaLocus().getChildren();
    ConstraintCheck[] result = new ConstraintCheck[ children.size()];
    for( int i=0; i<children.size(); i++)
      result[ i] = getConstraint( children.get( i));
    return result;
  }
  
  /**
   * Returns the constraint for the specified schema locus.
   * @param schemaLocus The schema locus.
   * @return
   */
  private ConstraintCheck getConstraint( IModelObject schemaLocus)
  {
    String type = schemaLocus.getType();
    if ( type.equals( "child"))
    {
      return new ChildCheck( schemaLocus);
    }
    else if ( type.equals( "constraint"))
    {
      return new RootConstraint( schemaLocus);
    }
    else if ( type.equals( "list"))
    {
      return new ListCheck( schemaLocus);
    }
    else if ( type.equals( "set"))
    {
      return new SetCheck( schemaLocus);
    }
    else if ( type.equals( "choice"))
    {
      return new ChoiceCheck( schemaLocus);
    }
    else if ( type.equals( "any"))
    {
      return new AnyCheck( schemaLocus);
    }
    
    throw new IllegalStateException( "Unrecognized constraint type.");
  }
  
  private int min;
  private int max;
  protected int index;
  protected ConstraintCheck[] constraints;
  protected int occurrences;
}
