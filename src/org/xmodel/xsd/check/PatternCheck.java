/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xsd.check;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;


/**
 * A string regular expression check.
 */
public class PatternCheck extends AbstractCheck
{
  public PatternCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    String spec = Xlate.get( schemaLocus, "");
    pattern = Pattern.compile( spec);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    String value = Xlate.get( documentLocus, "");
    Matcher matcher = pattern.matcher( value);
    return matcher.matches();
  }
  
  private Pattern pattern;
}
