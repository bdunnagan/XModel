package org.xmodel.net.nu.xaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Reference;
import org.xmodel.log.Log;
import org.xmodel.net.nu.IRouter;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.run.AsyncExecutionGroup;
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
    atExpr = document.getExpression( "at", true);
    timeoutExpr = document.getExpression( "timeout", true);
  }

  @Override
  protected Object[] doAction( IContext context)
  {
    int timeout = (int)timeoutExpr.evaluateNumber( context);
    
    IContext messageContext = new StatefulContext( context);
    Iterator<ITransport> transports = resolveTransport( context);
    
    AsyncExecutionGroup execution = new AsyncExecutionGroup( var, context, transports);
    
    execution.setSuccessScript( Conventions.getScript( document, context, onSuccessExpr));
    execution.setErrorScript( Conventions.getScript( document, context, onErrorExpr));
    execution.setCompleteScript( Conventions.getScript( document, context, onCompleteExpr));
    
    execution.send( getMessage(), messageContext, timeout);

    return null;
  }
  
  private Iterator<ITransport> resolveTransport( IContext context)
  {
    if ( atExpr.getType() == ResultType.NODES)
    {
      List<IModelObject> elements = atExpr.evaluateNodes( context);
      List<ITransport> transports = new ArrayList<ITransport>( elements.size());
      for( IModelObject element: elements)
      {
        Object transport = element.getValue();
        if ( transport != null && transport instanceof ITransport) 
          transports.add( (ITransport)transport);
      }
      return transports.iterator();
    }
    else
    {
      String route = atExpr.evaluateString( context);
      String[] parts = parseRoute( route);
      
      IRouter router = Routers.getRouter( parts[ 0]);
      if ( router != null) return router.resolve( parts[ 1]);
      
      log.warnf( "Router '%s' is not defined.", parts[ 0]);
      return Collections.<ITransport>emptyList().iterator();
    }
  }
  
  private IModelObject getMessage()
  {
    IModelObject message = new ModelObject( "script");
    
    for( IModelObject child: document.getRoot().getChildren())
      message.addChild( new Reference( child));

    return message;
  }
  
  private String[] parseRoute( String route)
  {
    int index = route.indexOf( '.');
    if ( index < 0) return new String[] { route, ""};
    return new String[] { 
      route.substring( 0, index), 
      route.substring( index+1)
    };
  }
  
  public final static Log log = Log.getLog( RunAction.class);
  
  private String var;
  private IExpression atExpr;
  private IExpression timeoutExpr;
  private IExpression onSuccessExpr;  // each
  private IExpression onErrorExpr;    // each
  private IExpression onCompleteExpr; // all
}
