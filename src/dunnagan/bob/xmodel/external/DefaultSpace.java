/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.xml.XmlIO;

/**
 * The default external space which uses URL.openStream() to parse the XML.
 */
public class DefaultSpace implements IExternalSpace
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalSpace#contains(java.net.URI)
   */
  public boolean contains( URI uri)
  {
    return true;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IExternalSpace#query(java.net.URI)
   */
  public List<IModelObject> query( URI uri) throws CachingException
  {
    try
    {
      XmlIO xmlIO = new XmlIO();
      IModelObject element = xmlIO.read( uri.toURL().openStream());
      return Collections.singletonList( element);
    }
    catch( MalformedURLException e)
    {
      throw new CachingException( "Unable to perform URI query:", e);
    }
    catch( Exception e)
    {
      return null;
    }
  }
}
