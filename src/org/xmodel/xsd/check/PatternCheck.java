/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PatternCheck.java
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
