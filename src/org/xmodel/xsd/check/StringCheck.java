/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * StringCheck.java
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
import org.xmodel.Xlate;

/**
 * A string value check.
 */
public class StringCheck extends AbstractCheck
{
  public StringCheck( INode schemaLocus)
  {
    super( schemaLocus);
    INode minObject = schemaLocus.getFirstChild( "min");
    minLength = (minObject != null)? Xlate.get( minObject, 0): -1;
    INode maxObject = schemaLocus.getFirstChild( "max");
    maxLength = (maxObject != null)? Xlate.get( maxObject, 0): -1;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( INode documentLocus)
  {
    String value = Xlate.get( documentLocus, "");
    if ( minLength >= 0 && value.length() < minLength) return false;
    if ( maxLength >= 0 && value.length() > maxLength) return false;
    return true;
  }
  
  private int minLength;
  private int maxLength;
}
