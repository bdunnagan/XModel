/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import org.xmodel.IModelObject;
import org.xmodel.xpath.AttributeNode;

/**
 * Base class for schema errors.
 */
public class SchemaError
{
  public enum Type 
  { 
    missingElement,
    illegalElement,
    missingAttribute, 
    illegalAttribute,
    invalidValue
  };
  
  /**
   * Create a schema error of the specified type.
   * @param type The type of schema error.
   */
  public SchemaError( Type type, IModelObject schemaLocus, IModelObject documentLocus)
  {
    this.type = type;
    this.schemaLocus = schemaLocus;
    this.documentLocus = documentLocus;
  }
  
  /**
   * Create an annotation for this error on the associated document locus.
   */
  public void annotate()
  {
    throw new UnsupportedOperationException();
  }
  
  /**
   * Returns true if this error has the specified type.
   * @param type The type to test.
   * @return Returns true if this error has the specified type.
   */
  public boolean isType( Type type)
  {
    return getType().equals( type);
  }
  
  /**
   * Returns the type of the schema error.
   * @return Returns the type of the schema error.
   */
  public Type getType()
  {
    return type;
  }
  
  /**
   * Returns the schema locus where the error occurred.
   * @return Returns the schema locus where the error occurred.
   */
  public IModelObject getSchemaLocus()
  {
    return schemaLocus;
  }

  /**
   * Returns the document locus where the error occurred.
   * @return Returns the document locus where the error occurred.
   */
  public IModelObject getDocumentLocus()
  {
    return documentLocus;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( type); builder.append( ": ");
    if ( documentLocus instanceof AttributeNode)
    {
      if ( documentLocus.getType().length() == 0)
      {
        builder.append( documentLocus.getParent().getType());
        builder.append( '='); builder.append( documentLocus.getValue());
      }
      else
      {
        builder.append( '@'); builder.append( documentLocus.getType());
        builder.append( '='); builder.append( documentLocus.getValue());
      }
    }
    else
    {
      builder.append( documentLocus);
    }
    return builder.toString();
  }
  
  private Type type;
  private IModelObject schemaLocus;
  private IModelObject documentLocus;
}
