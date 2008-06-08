/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd.check;

import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IPath;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.XPath;

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
   * @see dunnagan.bob.xmodel.xsd.nu.ICheck#validateImpl(dunnagan.bob.xmodel.IModelObject)
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
