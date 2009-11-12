/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * IXmlConverter.java
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
package org.xmodel.xml;

import org.jdom.Document;
import org.xmodel.IModelObject;

/**
 * An interface for converting between JDOM and XModel.  Implementations visit nodes in
 * the JDOM or XModel tree with an implementation of the IXmlConversion interface which
 * is responsible for converting a neighborhood of elements.
 * @deprecated
 */
public interface IXmlConverter
{
  /**
   * Convert the given JDOM document into an XModel.  The root of the XModel is returned.
   * @param document The JDOM document to be converted.
   * @return Returns the root of the XModel.
   */
  public IModelObject convert( Document document);
  
  /**
   * Convert an XModel rooted on the given object into a JDOM document.
   * @param object The root of the XModel to be converted.
   * @return Returns a new JDOM document.
   */
  public Document convert( IModelObject object);
  
  /**
   * Return the IXmlConversion which is used to convert elements and objects.
   * @return Return the IXmlConversion which is used to convert elements and objects.
   */
  public IXmlConversion getConversion();
}
