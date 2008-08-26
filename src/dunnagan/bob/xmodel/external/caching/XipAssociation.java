package dunnagan.bob.xmodel.external.caching;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.compress.TabularCompressor;
import dunnagan.bob.xmodel.external.CachingException;

/**
 * An IFileAssociation for the XModel <i>.xip</i> extension associated with the TabularCompressor.
 */
public class XipAssociation implements IFileAssociation
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
      TabularCompressor compressor = new TabularCompressor();
      IModelObject content = compressor.decompress( new BufferedInputStream( new FileInputStream( file)));
      parent.addChild( content);
    }
    catch( Exception e)
    {
      throw new CachingException( "Unable to parse xml in compressed file: "+file, e);
    }
  }
  
  private final static String[] extensions = { ".xip"};
}
