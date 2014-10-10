package org.xmodel.xaction;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;

public class WriteStreamAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    streamExpr = document.getExpression( "stream", true);
    sourceExpr = document.getExpression( "source", true);
    if ( sourceExpr == null) sourceExpr = document.getExpression();
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    FileOutputStreamAction.Stream stream = (FileOutputStreamAction.Stream)Conventions.getCache( context, streamExpr);
    switch( stream.type)
    {
      case xml:    writeXml( stream.out, context, sourceExpr); break;
      case xip:    writeXip( stream.out, context, sourceExpr); break;
      case text:   writeText( stream.out, stream.charset, context, sourceExpr); break;
      case binary: writeBinary( stream.out, context, sourceExpr); break;
    }
    return null;
  }
  
  private void writeXml( OutputStream stream, IContext context, IExpression sourceExpr)
  {
    try
    {
      for( IModelObject element: sourceExpr.evaluateNodes( context))
      {
        new XmlIO().write( element, stream);
      }
    }
    catch( XmlException e)
    {
      throw new XActionException( e);
    }
  }

  private void writeXip( OutputStream stream, IContext context, IExpression sourceExpr)
  {
    try
    {
      for( IModelObject element: sourceExpr.evaluateNodes( context))
      {
        TabularCompressor compressor = new TabularCompressor( false, false);
        compressor.compress( element, stream);
      }
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }
  }

  private void writeText( OutputStream stream, Charset charset, IContext context, IExpression sourceExpr)
  {
    try
    {
      switch( sourceExpr.getType( context))
      {
        case NODES:
        {
          for( IModelObject element: sourceExpr.evaluateNodes( context))
          {
            stream.write( Xlate.get( element, "").getBytes( charset));
          }
        }
        break;
        
        default:
        {
          stream.write( sourceExpr.evaluateString( context).getBytes( charset));
        }
        break;
      }
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }
  }

  private void writeBinary( OutputStream stream, IContext context, IExpression sourceExpr)
  {
    try
    {
      for( IModelObject element: sourceExpr.evaluateNodes( context))
      {
        Object value = element.getValue();
        if ( value instanceof byte[])
        {
          stream.write( (byte[])value);
        }
      }
    }
    catch( IOException e)
    {
      throw new XActionException( e);
    }
  }

  private IExpression streamExpr;
  private IExpression sourceExpr;
}
