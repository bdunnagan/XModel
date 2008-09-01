/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.xml.XmlIO;
import org.xmodel.xml.IXmlIO.Style;


public abstract class AbstractCheck implements ICheck
{
  public AbstractCheck( IModelObject schemaLocus)
  {
    this.schemaLocus = schemaLocus;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.check.ICheck#validate(org.xmodel.IModelObject)
   */
  public boolean validate( IModelObject documentLocus)
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
  protected abstract boolean validateImpl( IModelObject documentLocus);

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
  public IModelObject getSchemaLocus()
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
    xmlIO.setOutputStyle( Style.debug);
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

  private IModelObject schemaLocus;
  protected IModelObject errorLocus;
  protected List<ICheck> errored;
}
