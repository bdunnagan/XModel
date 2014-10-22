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
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.RoutedTransport;
import org.xmodel.util.MultiIterator;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;

final public class ActionUtil
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
  
  public static Iterator<ITransport> resolveTransport( IContext context, IExpression viaExpr, IExpression toExpr)
  {
    MultiIterator<ITransport> transports = new MultiIterator<ITransport>();
    
    if ( toExpr != null)
    {
      if ( toExpr.getType() == ResultType.NODES)
      {
        List<IModelObject> viaElements = viaExpr.evaluateNodes( context);
        for( IModelObject viaElement: viaElements)
        {
          Object via = viaElement.getValue();
          List<IModelObject> elements = toExpr.evaluateNodes( context);
          for( IModelObject element: elements)
          {
            String to = Xlate.get( element, (String)null);
            getTransports( via, to, transports);
          }
        }
      }
      else
      {
        String to = toExpr.evaluateString( context);
        List<IModelObject> viaElements = viaExpr.evaluateNodes( context);
        for( IModelObject viaElement: viaElements)
        {
          Object via = viaElement.getValue();
          getTransports( via, to, transports);
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
  
  private static void getTransports( Object via, String to, MultiIterator<ITransport> iterator)
  {
    if ( via != null)
    {
      if ( via instanceof IRouter && to != null)
      {
        iterator.add( ((IRouter)via).resolve( to));
      }
      else if ( via instanceof ITransportImpl)
      {
        iterator.add( (to != null)? new RoutedTransport( (ITransportImpl)via, to): (ITransportImpl)via);
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
  
  public final static Log log = Log.getLog( ActionUtil.class);
}
