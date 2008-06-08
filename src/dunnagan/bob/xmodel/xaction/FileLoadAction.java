/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.compress.CompressorException;
import dunnagan.bob.xmodel.compress.ICompressor;
import dunnagan.bob.xmodel.compress.TabularCompressor;
import dunnagan.bob.xmodel.xml.XmlException;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

/**
 * An XAction which loads an element from a file in compressed or uncompressed form.
 */
public class FileLoadAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    variable = Xlate.get( document.getRoot(), "assign", (String)null);
    targetExpr = document.getExpression( "target", false);
    fileExpr = document.getExpression( "file", false);
    if ( fileExpr == null) fileExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.GuardedAction#doAction(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    IModelObject parent = (targetExpr != null)? targetExpr.queryFirst( context): null;
    IModelObject element = null;
    
    File file = new File( fileExpr.evaluateString( context));
    long length = file.length();
    if ( length == 0 || length >= (1L << 32))
      throw new IllegalArgumentException( 
        "File size is too large or too small: "+this);
    
    // read file into memory
    byte[] content = new byte[ (int)length];
    try
    {
      DataInputStream stream = new DataInputStream( new FileInputStream( file));
      stream.readFully( content);
    }
    catch( IOException e)
    {
      throw new IllegalArgumentException( "Unable to open file: "+this, e); 
    }
    
    // parse
    int offset = 0; while( offset < length && Character.isWhitespace( content[ offset])) offset++;
    if ( content[ offset] == '<')
    {
      // xml
      try
      {
        if ( xmlIO == null) xmlIO = new XmlIO();
        element = xmlIO.read( new String( content));
      }
      catch( XmlException e)
      {
        throw new IllegalArgumentException( "Unable to parse file: "+this, e); 
      }
    }
    else
    {
      // compressed xml
      try
      {
        if ( compressor == null) compressor = new TabularCompressor();
        element = compressor.decompress( content, 0);
      }
      catch( CompressorException e)
      {
        throw new IllegalArgumentException( "Unable to decompress xml: "+this, e); 
      }
    }
    
    // set variable if defined
    IVariableScope scope = context.getScope();
    if ( variable != null)
    {
      if ( scope == null)
        throw new IllegalArgumentException( 
          "Unable to assign variable: "+variable+" in: "+this);
      
      scope.set( variable, element);
    }
    
    // add to parent
    if ( parent != null) parent.addChild( element);
  }

  private XmlIO xmlIO;
  private ICompressor compressor;
  private String variable;
  private IExpression targetExpr;
  private IExpression fileExpr;
}
