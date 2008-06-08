/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.AttributeNode;
import dunnagan.bob.xmodel.xpath.TextNode;
import dunnagan.bob.xmodel.xsd.check.SchemaError.Type;

/**
 * An implementation of ICheck for validating an attribute.
 */
public class ValueCheck extends AbstractCheck
{
  public ValueCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    
    attrName = ""; required = false;
    if ( schemaLocus.getType().equals( "attribute"))
    {
      attrName = Xlate.get( schemaLocus, "name", (String)null);
      required = Xlate.get( schemaLocus, "use", "optional").equals( "required");
    }
    
    IModelObject typeNode = schemaLocus.getFirstChild( "type");
    if ( typeNode != null)
    {
      List<IModelObject> validations = typeNode.getChildren();
      typeChecks = new TypeCheck[ validations.size()];
      for( int i=0; i<typeChecks.length; i++)
        typeChecks[ i] = new TypeCheck( validations.get( i));
    }
    else
    {
      typeChecks = new TypeCheck[ 0];
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.nu.ICheck#validateImpl(dunnagan.bob.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    if ( !(documentLocus instanceof AttributeNode) && !(documentLocus instanceof TextNode))
    {
      IModelObject node = documentLocus.getAttributeNode( attrName);
      if ( node == null && attrName.length() == 0)
      {
        documentLocus.setValue( "");
        node = documentLocus.getAttributeNode( attrName);
      }
      documentLocus = node;
    }
    
    String attrValue = Xlate.get( documentLocus, "");
    if ( attrName.length() > 0)
    {
      if ( attrValue == null && required) return false;
      if ( attrValue != null)
      {
        for( int i=0; i<typeChecks.length; i++)
          if ( !typeChecks[ i].validate( documentLocus))
          {
            addFailed( typeChecks[ i]);
            return false;
          }
      }
    }
    else 
    {
      for( int i=0; i<typeChecks.length; i++)
        if ( !typeChecks[ i].validate( documentLocus))
        {
          addFailed( typeChecks[ i]);
          return false;
        }
    }
    
    return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xsd.check.AbstractCheck#getErrors(java.util.List)
   */
  @Override
  public void getErrors( List<SchemaError> errors)
  {
    super.getErrors( errors);
    
    // create missing attribute error
    List<ICheck> failed = getFailed();
    if ( failed == null || failed.size() == 0)
      errors.add( new SchemaError( Type.missingAttribute, getSchemaLocus(), errorLocus));
  }
  
  private String attrName;
  private boolean required;
  private TypeCheck[] typeChecks;
}
