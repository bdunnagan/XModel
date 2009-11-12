/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XmlConverter.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.util.Fifo;

/**
 * @deprecated
 */
@SuppressWarnings("unchecked")
public class XmlConverter implements IXmlConverter
{
  /**
   * Create an XmlConverter which uses BasicConversion with the default IModelObjectFactory.
   */
  public XmlConverter()
  {
    conversion = new NamespaceConversion();
    //conversion = new BasicConversion();
  }

  /**
   * Create an XmlConverter which uses the given IXmlConversion.
   * @param conversion The type of conversion to use.
   */
  public XmlConverter( IXmlConversion conversion)
  {
    this.conversion = conversion;
  }
  
  /**
   * Create an XmlConverter which uses BasicConversion with the given IModelObjectFactory.
   * @param factory The factory to use with BasicConversion.
   */
  public XmlConverter( IModelObjectFactory factory)
  {
    conversion = new NamespaceConversion( factory);
    //conversion = new BasicConversion( factory);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlConverter#convert(org.jdom.Document)
   */
  public IModelObject convert( Document document)
  {
    List consumed = new ArrayList();
    Element rootElement = document.getRootElement();
    IModelObject model = conversion.transform( rootElement, consumed);
    
    // build model
    Fifo<Object> fifo = new Fifo<Object>();
    fifo.push( rootElement);
    fifo.push( model);
    while( !fifo.empty())
    {
      Element element = (Element)fifo.pop();
      IModelObject object = (IModelObject)fifo.pop();
      if ( !consumed.remove( element))
      {
        Iterator iter = element.getChildren().iterator();
        while( iter.hasNext())
        {
          Element childElement = (Element)iter.next();
          IModelObject child = conversion.transform( childElement, consumed);
          if ( child != null)
          {
            object.addChild( child);
            fifo.push( childElement);
            fifo.push( child);
          }
        }
      }
    }
    return model;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlConverter#convert(org.xmodel.IModelObject)
   */
  public Document convert( IModelObject object)
  {
    List consumed = new ArrayList();
    Element element = conversion.transform( object, consumed);
    Document document = new Document( element);
    
    // build model
    Fifo<Object> fifo = new Fifo<Object>();
    fifo.push( element);
    fifo.push( object);
    while( !fifo.empty())
    {
      element = (Element)fifo.pop();
      object = (IModelObject)fifo.pop();
      if ( !consumed.remove( element))
      {
        Iterator iter = object.getChildren().iterator();
        while( iter.hasNext())
        {
          IModelObject childObject = (IModelObject)iter.next();
          Element childElement = conversion.transform( childObject, consumed);
          if ( childElement != null)
          {
            element.addContent( childElement);
            fifo.push( childElement);
            fifo.push( childObject);
          }
        }
      }
    }
    return document;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xml.IXmlConverter#getConversion()
   */
  public IXmlConversion getConversion()
  {
    return conversion;
  }
  
  IXmlConversion conversion;
}
