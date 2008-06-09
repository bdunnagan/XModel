/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.compress.CompressorException;
import dunnagan.bob.xmodel.compress.ICompressor;
import dunnagan.bob.xmodel.compress.TabularCompressor;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xml.IXmlIO.Style;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XAction which saves an element to a file in compressed or uncompressed form.
 */
public class FileSaveAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    mode = Xlate.get( document.getRoot(), "mode", "printable");
    overwrite = Xlate.get( document.getRoot(), "overwrite", false);
    sourceExpr = document.getExpression( "source", false);
    fileExpr = document.getExpression( "file", false);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    IModelObject element = sourceExpr.queryFirst( context);
    if ( element == null) return;
    
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
        FileOutputStream stream = new FileOutputStream( file);
        stream.write( xml.getBytes( "UTF-8"));
        stream.close();
      }
      catch( IOException e)
      {
        throw new IllegalArgumentException( "Unable write file: "+this, e);
      }
    }
  }

  private XmlIO xmlIO;
  private ICompressor compressor;
  private IExpression sourceExpr;
  private IExpression fileExpr;
  private String mode;
  private boolean overwrite;
}
