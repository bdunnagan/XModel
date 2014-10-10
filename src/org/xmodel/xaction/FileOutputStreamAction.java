package org.xmodel.xaction;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

public class FileOutputStreamAction extends GuardedAction
{
  public enum Type { xml, xip, text, binary};
  
  public static class Stream
  {
    public Stream( Type type, Charset charset, OutputStream out)
    {
      this.type = type;
      this.charset = charset;
      this.out = out;
    }
    
    public final Type type;
    public final Charset charset;
    public final OutputStream out;
  }
  
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    var = Conventions.getVarName( document.getRoot(), true);
    typeExpr = document.getExpression( "type", true);
    fileExpr = document.getExpression( "file", true);
    charsetExpr = document.getExpression( "charset", true);
    script = Conventions.getScript( document, document.getRoot());
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    String type = typeExpr.evaluateString( context);
    String file = fileExpr.evaluateString( context);
    Charset charset = (charsetExpr != null)? Charset.forName( charsetExpr.evaluateString( context)): null;
    Stream stream = new Stream( Type.valueOf( type), charset, openFile( file));
    try
    {
      StatefulContext nested = new StatefulContext( context);
      Conventions.putCache( context, var, stream);
      return script.run( nested);
    }
    finally
    {
      closeFile( stream.out);
    }
  }
  
  private FileOutputStream openFile( String file)
  {
    try
    {
      return new FileOutputStream( file);
    }
    catch( FileNotFoundException e)
    {
      throw new XActionException( e);
    }
  }
    
  private void closeFile( OutputStream out)
  {
    try
    {
      out.close();
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }
  }

  private String var;
  private IExpression typeExpr;
  private IExpression fileExpr;
  private IExpression charsetExpr;
  private IXAction script;
}
