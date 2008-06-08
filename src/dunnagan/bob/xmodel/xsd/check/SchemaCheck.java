/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.xpath.XPath;

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
   * @see dunnagan.bob.xmodel.xsd.nu.ICheck#validateImpl(dunnagan.bob.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    globalElementFinder.setVariable( "name", documentLocus.getType());
    IModelObject elementSchemaLocus = globalElementFinder.queryFirst( getSchemaLocus());
    if ( elementSchemaLocus == null) return false;
    
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
