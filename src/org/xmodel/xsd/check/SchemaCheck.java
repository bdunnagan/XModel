/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xsd.check;

import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.xpath.XPath;

/**
 * An implementation of ICheck which validates an entire schema.
 */
public class SchemaCheck extends AbstractCheck
{
  public SchemaCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    globalElementFinder.setVariable( "name", documentLocus.getType());
    IModelObject elementSchemaLocus = globalElementFinder.queryFirst( getSchemaLocus());
    if ( elementSchemaLocus == null) 
    {
      addFailed( this);
      return false;
    }
    
    ElementCheck elementCheck = new ElementCheck( elementSchemaLocus);
    if ( !elementCheck.validate( documentLocus))
    {
      addFailed( elementCheck);
      return false;
    }
    return true;
  }
  
  private static IPath globalElementFinder = XPath.createPath(
    "element[ @global = 'true' and @name = $name]");
}
