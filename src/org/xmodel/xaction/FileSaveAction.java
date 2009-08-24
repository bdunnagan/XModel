/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.xml.XmlIO;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

/**
 * An XAction which saves an element to a file in compressed or uncompressed form.
 */
public class FileSaveAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    mode = Xlate.get( document.getRoot(), "mode", "printable");
    overwrite = Xlate.get( document.getRoot(), "overwrite", false);
    
    sourceExpr = document.getExpression( "source", true);
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    
    fileExpr = document.getExpression( "file", true);
    if ( fileExpr == null) fileExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    IModelObject element = sourceExpr.queryFirst( context);
    if ( element == null) return null;
    
    File file = new File( fileExpr.evaluateString( context));
    if ( !overwrite && file.exists())
      throw new IllegalArgumentException(
        "File already exists: "+this);
    
    if ( mode.equals( "compressed"))
    {
      if ( compressor == null) compressor = new TabularCompressor();
      try
      {
        byte[] compressed = compressor.compress( element);
        FileOutputStream stream = new FileOutputStream( file);
        stream.write( compressed);
        stream.close();
      }
      catch( CompressorException e)
      {
      }
      catch( IOException e)
      {
        e.printStackTrace( System.err);
      }
    }
    else
    {
      if ( xmlIO == null) xmlIO = new XmlIO();
      xmlIO.setOutputStyle( mode.equals( "compact")? Style.compact: Style.printable);
      String xml = xmlIO.write( element);
      try
      {
        file.createNewFile();
        FileOutputStream stream = new FileOutputStream( file);
        stream.write( xml.getBytes( "UTF-8"));
        stream.close();
      }
      catch( IOException e)
      {
        throw new XActionException( "Unable write file: "+this, e);
      }
    }
    
    return null;
  }

  private XmlIO xmlIO;
  private ICompressor compressor;
  private IExpression sourceExpr;
  private IExpression fileExpr;
  private String mode;
  private boolean overwrite;
}
