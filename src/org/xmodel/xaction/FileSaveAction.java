/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FileSaveAction.java
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
package org.xmodel.xaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import org.jboss.netty.buffer.ChannelBuffer;
import org.xmodel.INode;
import org.xmodel.Xlate;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.ZipCompressor;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlIO;
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
    File file = new File( fileExpr.evaluateString( context));
    if ( !overwrite && file.exists()) throw new IllegalArgumentException( "File already exists: "+this);
    
    INode element = sourceExpr.queryFirst( context);
    if ( element == null)
    {
      try
      {
        // go ahead and create empty file
        file.createNewFile();
      }
      catch( IOException e)
      {
        throw new XActionException( "Unable to write file: "+file, e);
      }
      
      return null;
    }
    
    if ( mode.equals( "compressed"))
    {
      if ( compressor == null) compressor = new ZipCompressor( new TabularCompressor(), Deflater.BEST_COMPRESSION);
      try
      {
        FileOutputStream stream = new FileOutputStream( file);
        ChannelBuffer buffer = compressor.compress( element);
        buffer.readBytes( stream, buffer.readableBytes());
        stream.close();
      }
      catch( IOException e)
      {
        throw new XActionException( "Unable to write file: "+file, e);
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
        throw new XActionException( "Unable to write file: "+file, e);
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
