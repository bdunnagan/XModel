/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SchemaCheck.java
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
  
  private IPath globalElementFinder = XPath.createPath(
    "element[ @global = 'true' and @name = $name]");
}
