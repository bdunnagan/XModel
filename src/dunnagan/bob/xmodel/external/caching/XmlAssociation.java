package dunnagan.bob.xmodel.external.caching;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.xml.XmlIO;

/**
 * An IFileAssociation for xml, xsd, dtd and other well-formed xml extensions.
 */
public class XmlAssociation implements IFileAssociation
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.caching.IFileAssociation#getAssociations()
   */
  public String[] getExtensions()
  {
    return extensions;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.caching.IFileAssociation#apply(dunnagan.bob.xmodel.IModelObject, java.io.File)
   */
  public void apply( IModelObject parent, File file) throws CachingException
  {
    try
    {
      XmlIO xmlIO = new XmlIO();
      IModelObject content = xmlIO.read( new BufferedInputStream( new FileInputStream( file)));
      parent.addChild( content);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to parse xml in file: "+file, e);
    }
  }
  
  private final static String[] extensions = { ".xml", ".xsd", ".dtd"};
}
