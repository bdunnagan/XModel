package dunnagan.bob.xmodel.external.caching;

import java.io.File;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.CachingException;

/**
 * An interface for file extension handlers used by the FileSystemCachingPolicy. An extension handler determines
 * how the content of a file with a particular extension is applied to the file element in the model created by
 * the FileSystemCachingPolicy.
 */
public interface IFileAssociation
{
  /**
   * Returns the extensions handled by this association.
   * @return Returns the extensions handled by this association.
   */
  public String[] getExtensions();
  
  /**
   * Read the specified file content and apply it to the specified parent file element.
   * @param parent The parent file element (as defined by FileSystemCachingPolicy).
   * @param file The file whose content will be read.
   */
  public void apply( IModelObject parent, File file) throws CachingException;
}
