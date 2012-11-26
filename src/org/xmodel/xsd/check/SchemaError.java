/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SchemaError.java
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

import org.xmodel.INode;
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
  public SchemaError( Type type, INode schemaLocus, INode documentLocus)
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
  public INode getSchemaLocus()
  {
    return schemaLocus;
  }

  /**
   * Returns the document locus where the error occurred.
   * @return Returns the document locus where the error occurred.
   */
  public INode getDocumentLocus()
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
  private INode schemaLocus;
  private INode documentLocus;
}
