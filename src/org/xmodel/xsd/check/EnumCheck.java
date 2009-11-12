/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * EnumCheck.java
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

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;

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

  private List<IModelObject> entries;
}
