/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xml;

import org.jdom.Document;
import dunnagan.bob.xmodel.IModelObject;

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
