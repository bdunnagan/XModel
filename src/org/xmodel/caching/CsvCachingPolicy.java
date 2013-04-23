package org.xmodel.caching;

import java.io.File;
import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.IExternalReference;
import org.xmodel.util.FileUtil;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;

/**
 * A caching policy that parses CSV text files into an xml table.  This class can detect three types
 * of common delimiters: tab, comma and space.  The auto-detection algorithm uses the following rules:
 * <ul>
 * <li>If the first line contains a non-quoted comma, then comma is the separator.</li>
 * <li>If the first line contains a non-quoted tab, then tab is the separator.</li>
 * <li>If the first line contains a non-quoted space, then space is the seprator.</li>
 * <li>Otherwise, the above steps are repeated for the next line.</li>
 * </ul>
 */
public class CsvCachingPolicy extends ConfiguredCachingPolicy
{
  public final static int maxDelimiterSearch = 10;
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    File path = new File( Xlate.get( reference, "path", ""));
    
    try
    {
      String content = FileUtil.readAll( path);
      
      char delimiter = findDelimiter( content);
      if ( delimiter == 0) 
        throw new CachingException( 
          "Auto-detection of CSV delimiter failed.");
      
      IModelObject element = parse( content, delimiter);
      update( reference, element);
    }
    catch( IOException e)
    {
      throw new CachingException( String.format( 
        "Unable read csv file, '%s'", path.toString()), e);
    }
  }
  
  /**
   * Parse the specified CSV content using the specified delimiter.
   * @param text The text of the CSV file.
   * @param delimiter The delimiter.
   * @return Returns the xml table.
   */
  private IModelObject parse( String text, char delimiter)
  {
    IModelObject table = new ModelObject( "table");
    IModelObject row = null;
    StringBuilder field = new StringBuilder();
    
    boolean squote = false;
    boolean dquote = false;
    for( int i=0; i<text.length(); i++)
    {
      char c = text.charAt( i);
      
      if ( !squote && c == '\"')
      {
        dquote = !dquote;
      }
      else if ( !dquote && c == '\'')
      {
        squote = !squote;
      }
      else if ( !squote && !dquote)
      {
        if ( c == delimiter || c == '\n')
        {
          if ( (c != '\t' && c != ' ') || field.length() != 0)
          {
            if ( row == null) 
            {
              row = new ModelObject( "row");
              table.addChild( row);
            }
            
            IModelObject column = new ModelObject( "column");
            column.setValue( trimFields? field.toString().trim(): field.toString());
            field.setLength( 0);
            
            row.addChild( column);
          }
        }
        else
        {
          field.append( c);
        }
      }
      
      if ( c == '\n') row = null;
    }
    
    return table;
  }
  
  /**
   * Find the delimiter in the specified text.
   * @param text The complete text.
   * @return Returns 0 or the delimiter character.
   */
  private char findDelimiter( String text) throws CachingException
  {
    int index = 0;
    int eol = text.indexOf( '\n');
    for( int i=0; i<maxDelimiterSearch && eol >= 0; i++)
    {
      char delimiter = detectDelimiter( text.substring( index, eol));
      if ( delimiter != 0) return delimiter;
      eol = text.indexOf( index, '\n');
    }
    return 0;
  }
  
  /**
   * Detect the delimiter character, if possible, from the specified line.
   * @param line A line from the text file.
   * @return Returns 0 or the delimiter character.
   */
  private static char detectDelimiter( String line)
  {
    boolean squote = false;
    boolean dquote = false;
    boolean comma = false;
    boolean tab = false;
    boolean space = false;
    for( int i=0; i<line.length(); i++)
    {
      char c = line.charAt( i);
      
      if ( !squote && c == '\"')
      {
        dquote = !dquote;
      }
      else if ( !dquote && c == '\'')
      {
        squote = !squote;
      }
      else if ( !squote && !dquote)
      {
        if ( c == ',')
        {
          comma = true;
        }
        else if ( c == '\t')
        {
          tab = true;
        }
        else if ( c == ' ')
        {
          space = true;
        }
      }
    }
    
    if ( comma) return ',';
    if ( tab) return '\t';
    if ( space) return ' ';
    return 0;
  }
  
  private boolean trimFields;
  
  public static void main( String[] args) throws Exception
  {
    CsvCachingPolicy cachingPolicy = new CsvCachingPolicy();
    
    IExternalReference reference = new ExternalReference( "countries.txt");
    reference.setAttribute( "path", "countries.txt");
    reference.setCachingPolicy( cachingPolicy);
    reference.setDirty( true);
    
    System.out.println( XmlIO.write( Style.printable, reference));
  }
}
