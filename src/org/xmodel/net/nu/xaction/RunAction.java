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
import org.xmodel.net.nu.run.AsyncExecutionGroup;
import org.xmodel.util.MultiIterator;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.GuardedAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.expression.StatefulContext;

public class RunAction extends GuardedAction
{
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);

    var = Conventions.getVarName( document.getRoot(), false);
    viaExpr = document.getExpression( "via", true);
    atExpr = document.getExpression( "at", true);
    timeoutExpr = document.getExpression( "timeout", true);
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    int timeout = (int)timeoutExpr.evaluateNumber( context);
    
    IContext messageContext = new StatefulContext( context);
    Iterator<ITransport> transports = resolveTransport( context);
    
    AsyncExecutionGroup execution = new AsyncExecutionGroup( var, context);
    
    execution.setSuccessScript( Conventions.getScript( document, context, onSuccessExpr));
    execution.setErrorScript( Conventions.getScript( document, context, onErrorExpr));
    execution.setCompleteScript( Conventions.getScript( document, context, onCompleteExpr));
    
    execution.send( transports, getMessage(), messageContext, timeout);

    return null;
  }
  
  private Iterator<ITransport> resolveTransport( IContext context)
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
  
  private void getTransports( Object via, String at, MultiIterator<ITransport> iterator)
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
  
  private IModelObject getMessage()
  {
    IModelObject message = new ModelObject( "script");
    
    for( IModelObject child: document.getRoot().getChildren())
      message.addChild( new Reference( child));

    return message;
  }
  
  public final static Log log = Log.getLog( RunAction.class);
  
  private String var;
  private IExpression viaExpr;
  private IExpression atExpr;
  private IExpression timeoutExpr;
  private IExpression onSuccessExpr;  // each
  private IExpression onErrorExpr;    // each
  private IExpression onCompleteExpr; // all
}
