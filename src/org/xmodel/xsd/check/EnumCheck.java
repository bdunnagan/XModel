/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.Xlate;
import org.xmodel.xpath.XPath;


/**
 * A string enumeration check.
 */
public class EnumCheck extends AbstractCheck
{
  public EnumCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    entries = schemaLocus.getChildren( "value");
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    String value = Xlate.get( documentLocus, "");
    for( IModelObject entry: entries)
      if ( value.equals( Xlate.get( entry, "")))
        return true;
    return false;
  }

  private static IPath entryPath = XPath.createPath( "enum/value");
  private List<IModelObject> entries;
}
