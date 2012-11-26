/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ElementCheck.java
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
import java.util.Collection;
import java.util.List;
import org.xmodel.INode;
import org.xmodel.IPath;
import org.xmodel.Xlate;
import org.xmodel.xpath.XPath;
import org.xmodel.xsd.check.SchemaError.Type;


/**
 * An implementation of ICheck for validating an element.
 */
public class ElementCheck extends AbstractCheck
{
  public ElementCheck( INode schemaLocus)
  {
    super( schemaLocus);
    
    // attributes
    valueChecks = new ArrayList<ValueCheck>();
    attributes = schemaAttributePath.query( schemaLocus, null);
    for( INode attribute: attributes) valueChecks.add( new ValueCheck( attribute));

    // element value
    INode value = schemaLocus.getFirstChild( "value");
    if ( value != null) valueChecks.add( new ValueCheck( value));
    
    // constraint check
    INode constraint = schemaLocus.getFirstChild( "constraint");
    if ( constraint != null) constraintCheck = new RootConstraint( constraint);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( INode documentLocus)
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
        if ( illegalAttributes == null) illegalAttributes = new ArrayList<INode>();
        illegalAttributes.add( documentLocus.getAttributeNode( attrName));
        return false;
      }
      
    // check child constraints
    if ( constraintCheck != null && !constraintCheck.validate( documentLocus))
      addFailed( constraintCheck);
    
    // recursively check children
    List<INode> children = documentLocus.getChildren();
    for( INode child: children)
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
    for( INode attribute: attributes)
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
    INode elementSchema = childSchemaPath.queryFirst( getSchemaLocus());
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
      for( INode illegalAttribute: illegalAttributes)
        errors.add( new SchemaError( Type.illegalAttribute, getSchemaLocus(), illegalAttribute));
  }
  
  private IPath schemaAttributePath = XPath.createPath(
    "attributes/attribute");

  private IPath childSchemaPath = XPath.createPath(
    "children/element[ @name = $name]");
  
  private List<ValueCheck> valueChecks;
  private List<INode> attributes;
  private RootConstraint constraintCheck;
  private List<INode> illegalAttributes;
}
