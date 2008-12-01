/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.Xlate;
import org.xmodel.xpath.XPath;
import org.xmodel.xsd.check.SchemaError.Type;


/**
 * An implementation of ICheck for validating an element.
 */
public class ElementCheck extends AbstractCheck
{
  public ElementCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    
    // attributes
    valueChecks = new ArrayList<ValueCheck>();
    attributes = schemaAttributePath.query( schemaLocus, null);
    for( IModelObject attribute: attributes) valueChecks.add( new ValueCheck( attribute));

    // element value
    IModelObject value = schemaLocus.getFirstChild( "value");
    if ( value != null) valueChecks.add( new ValueCheck( value));
    
    // constraint check
    IModelObject constraint = schemaLocus.getFirstChild( "constraint");
    if ( constraint != null) constraintCheck = new RootConstraint( constraint);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    // init
    if ( illegalAttributes != null) illegalAttributes.clear();
    
    // check attributes and value
    for( ValueCheck valueCheck: valueChecks)
      if ( !valueCheck.validate( documentLocus))
        addFailed( valueCheck);
    
    // reject extra attributes
    Collection<String> attrNames = documentLocus.getAttributeNames();
    for( String attrName: attrNames)
      if ( attrName.length() > 0 && !attrName.startsWith( "xmlns") && !isDefinedAttribute( attrName))
      {
        if ( illegalAttributes == null) illegalAttributes = new ArrayList<IModelObject>();
        illegalAttributes.add( documentLocus.getAttributeNode( attrName));
        return false;
      }
      
    // check child constraints
    if ( constraintCheck != null && !constraintCheck.validate( documentLocus))
      addFailed( constraintCheck);
    
    // recursively check children
    List<IModelObject> children = documentLocus.getChildren();
    for( IModelObject child: children)
    {
      if ( child.getType().charAt( 0) == '?') continue;
      ElementCheck elementCheck = getElementCheck( child.getType());
      if ( elementCheck != null && !elementCheck.validate( child)) 
        addFailed( elementCheck);
    }

    return errored == null || errored.size() == 0;
  }
  
  /**
   * Returns true if the specified attribute name is defined in the schema.
   * @param name The name of the attribute.
   * @return Returns true if the specified attribute name is defined in the schema.
   */
  private boolean isDefinedAttribute( String name)
  {
    for( IModelObject attribute: attributes)
      if ( Xlate.get( attribute, "name", (String)null).equals( name))
        return true;
    return false;
  }
  
  /**
   * Returns an ElementCheck for the specified child.
   * @param name The name of the child element.
   * @return Returns an ElementCheck for the specified child.
   */
  private ElementCheck getElementCheck( String name)
  {
    childSchemaPath.setVariable( "name", name);
    IModelObject elementSchema = childSchemaPath.queryFirst( getSchemaLocus());
    return (elementSchema != null)? new ElementCheck( elementSchema): null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    super.getErrors( errors);
    
    // create errors for illegal children
    if ( illegalAttributes != null)
      for( IModelObject illegalAttribute: illegalAttributes)
        errors.add( new SchemaError( Type.illegalAttribute, getSchemaLocus(), illegalAttribute));
  }
  
  private IPath schemaAttributePath = XPath.createPath(
    "attributes/attribute");

  private IPath childSchemaPath = XPath.createPath(
    "children/element[ @name = $name]");
  
  private List<ValueCheck> valueChecks;
  private List<IModelObject> attributes;
  private RootConstraint constraintCheck;
  private List<IModelObject> illegalAttributes;
}
