/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * FileLoadAction.java
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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.jboss.netty.buffer.ChannelBuffers;
import org.xmodel.INode;
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
    var = Conventions.getVarName( document.getRoot(), false, "assign");
    targetExpr = document.getExpression( "target", true);
    fileExpr = document.getExpression( "file", true);
    if ( fileExpr == null) fileExpr = document.getExpression();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    INode parent = (targetExpr != null)? targetExpr.queryFirst( context): null;
    INode element = null;
    
    // initialize variable
    IVariableScope scope = context.getScope();
    if ( scope != null) scope.set( var, new ArrayList<INode>( 0));
    
    // get file
    File file = new File( fileExpr.evaluateString( context));
    long length = file.length();
    if ( length == 0) return null;
    
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
        element = compressor.decompress( ChannelBuffers.wrappedBuffer( content));
      }
      catch( IOException e)
      {
        throw new IllegalArgumentException( "Unable to decompress xml: "+this, e); 
      }
    }
    
    // set variable if defined
    if ( var != null && scope != null) scope.set( var, element);
    
    // add to parent
    if ( parent != null) parent.addChild( element);
    
    return null;
  }

  private XmlIO xmlIO;
  private ICompressor compressor;
  private String var;
  private IExpression targetExpr;
  private IExpression fileExpr;
}
