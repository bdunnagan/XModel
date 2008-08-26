package dunnagan.bob.xmodel.external.caching;

import java.io.File;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.CachingException;

/**
 * An IFileAssociation for various text file associations including <i>.txt</i> and associations for various 
 * programming language files such as html, java, perl and python.
 */
public class TxtAssociation implements IFileAssociation
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
      
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable read text file: "+file, e);
    }
  }
  
  private final static String[] extensions = { 
    ".txt", ".css", ".html", ".htm", ".java", ".rtf", ".pl", ".py"};
}
