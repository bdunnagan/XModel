/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AbstractCheck.java
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
import org.xmodel.xml.XmlIO;
import org.xmodel.xml.IXmlIO.Style;


public abstract class AbstractCheck implements ICheck
{
  public AbstractCheck( INode schemaLocus)
  {
    this.schemaLocus = schemaLocus;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.ICheck#validate(org.xmodel.IModelObject)
   */
  public boolean validate( INode documentLocus)
  {
    errorLocus = null; 
    if ( errored != null) errored.clear();
    if ( !validateImpl( documentLocus) && errorLocus == null) errorLocus = documentLocus;
    return errorLocus == null;
  }
  
  /**
   * This method should be overridden to perform the validation in the subclass.
   * @param documentLocus The document locus.
   * @return Returns true if the document locus is valid.
   */
  protected abstract boolean validateImpl( INode documentLocus);

  /**
   * Add an ICheck to the errored list.
   * @param check The check.
   */
  protected void addFailed( ICheck check)
  {
    if ( errored == null) errored = new ArrayList<ICheck>();
    errored.add( check);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#getSchemaLocus()
   */
  public INode getSchemaLocus()
  {
    return schemaLocus;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.ICheck#getErrors(java.util.List)
   */
  public void getErrors( List<SchemaError> errors)
  {
    List<ICheck> failed = getFailed();
    if ( failed != null && failed.size() > 0)
      for( ICheck check: failed) 
        check.getErrors( errors);
  }
  
  /**
   * Returns a list of ICheck instances that failed during the last validation.
   * @return Returns a list of ICheck instances that failed during the last validation.
   */
  public List<ICheck> getFailed()
  {
    return errored;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    XmlIO xmlIO = new XmlIO();
    xmlIO.setOutputStyle( Style.printable);
    if ( errorLocus == null)
    {
      StringBuilder builder = new StringBuilder();
      builder.append( "PASS:\n");
      builder.append( xmlIO.write( schemaLocus));
      builder.append( '\n');
      return builder.toString();
    }
    else if ( errored != null && errored.size() > 0)
    {
      StringBuilder builder = new StringBuilder();
      for( ICheck check: errored) builder.append( check.toString());
      return builder.toString();
    }
    else
    {
      StringBuilder builder = new StringBuilder();
      builder.append( "FAIL:\n"); 
      builder.append( xmlIO.write( schemaLocus)); 
      builder.append( '\n');
      builder.append( xmlIO.write( errorLocus)); 
      builder.append( '\n');
      return builder.toString();
    }
  }

  private INode schemaLocus;
  protected INode errorLocus;
  protected List<ICheck> errored;
}
