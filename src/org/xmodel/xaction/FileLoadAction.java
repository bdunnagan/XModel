/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.compress.CompressorException;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;


/**
 * An XAction which loads an element from a file in compressed or uncompressed form.
 */
public class FileLoadAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#configure(org.xmodel.xaction.XActionDocument)
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
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
  {
    IModelObject parent = (targetExpr != null)? targetExpr.queryFirst( context): null;
    IModelObject element = null;
    
    // initialize variable
    IVariableScope scope = context.getScope();
    if ( scope != null) scope.set( variable, new ArrayList<IModelObject>( 0));
    
    // get file
    File file = new File( fileExpr.evaluateString( context));
    long length = file.length();
    if ( length == 0) return;
    
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
    if ( scope != null) scope.set( variable, element);
    
    // add to parent
    if ( parent != null) parent.addChild( element);
  }

  private XmlIO xmlIO;
  private ICompressor compressor;
  private String variable;
  private IExpression targetExpr;
  private IExpression fileExpr;
}
