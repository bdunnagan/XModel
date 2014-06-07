package org.xmodel.net.nu.xaction;

import java.util.Iterator;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Reference;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.RoutedTransport;
import org.xmodel.util.MultiIterator;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;

final class MessageSchema
{
  public static IModelObject getMessage( IModelObject document)
  {
    List<IModelObject> children = document.getChildren();
    if ( children.size() > 1)
    {
      IModelObject message = new ModelObject( "message");
      for( IModelObject child: children)
        message.addChild( new Reference( child));
      return message;
    }
    else
    {
      return new Reference( children.get( 0));
    }
  }
  
  public static Iterator<ITransport> resolveTransport( IContext context, IExpression viaExpr, IExpression atExpr)
  {
    MultiIterator<ITransport> transports = new MultiIterator<ITransport>();
    
    if ( atExpr != null)
    {
      if ( atExpr.getType() == ResultType.NODES)
      {
        List<IModelObject> elements = atExpr.evaluateNodes( context);
        for( IModelObject element: elements)
        {
          Object via = element.getAttribute( "via");
          String at = Xlate.get( element, "at", (String)null);
          getTransports( via, at, transports);
        }
      }
      else
      {
        String at = atExpr.evaluateString( context);
        List<IModelObject> viaElements = viaExpr.evaluateNodes( context);
        for( IModelObject viaElement: viaElements)
        {
          Object via = viaElement.getValue();
          getTransports( via, at, transports);
        }
      }
    }
    else
    {
      List<IModelObject> viaElements = viaExpr.evaluateNodes( context);
      for( IModelObject viaElement: viaElements)
      {
        Object via = viaElement.getValue();
        getTransports( via, null, transports);
      }
    }
    
    return transports;
  }
  
  private static void getTransports( Object via, String at, MultiIterator<ITransport> iterator)
  {
    if ( via != null)
    {
      if ( via instanceof IRouter)
      {
        if ( at != null)
        {
          iterator.add( ((IRouter)via).resolve( at));
        }
        else
        {
          log.warnf( "Route is null.");
        }
      }
      else if ( via instanceof ITransport)
      {
        iterator.add( (at != null)? new RoutedTransport( (ITransport)via, at): (ITransport)via);
      }
      else
      {
        log.warnf( "Via object is not an instance of IRouter or ITransport.");
      }
    }
    else
    {
      log.warnf( "Via object is null.");
    }
  }
  
  public final static Log log = Log.getLog( MessageSchema.class);
}
