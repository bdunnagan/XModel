package org.xmodel.net.nu.xaction;

import java.io.IOException;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.log.SLog;
import org.xmodel.net.nu.DefaultEventHandler;
import org.xmodel.net.nu.ITransport;
import org.xmodel.net.nu.ITransport.Error;
import org.xmodel.net.nu.ITransportImpl;
import org.xmodel.net.nu.protocol.IEnvelopeProtocol.Type;
import org.xmodel.xaction.Conventions;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class EventHandlerAdapter extends DefaultEventHandler
{
  public EventHandlerAdapter( XActionDocument document, ITransport transport, IContext context)
  {
    this.transport = transport;
    
    onConnect = Conventions.getScript( document, context, document.getExpression( "onConnect", true));
    onDisconnect = Conventions.getScript( document, context, document.getExpression( "onDisconnect", true));
    onRegister = Conventions.getScript( document, context, document.getExpression( "onRegister", true));
    onDeregister = Conventions.getScript( document, context, document.getExpression( "onDeregister", true));
    onReceive = Conventions.getScript( document, context, document.getExpression( "onReceive", true));
    onError = Conventions.getScript( document, context, document.getExpression( "onError", true));
  }
  
  @Override
  public boolean notifyConnect( final ITransportImpl transport, final IContext transportContext) throws IOException
  {
    if ( onConnect != null) 
    {
      transportContext.getExecutor().execute( new Runnable() {
        public void run()
        {
          StatefulContext connectContext = new StatefulContext( transportContext);
          
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
          
          ScriptAction.passVariables( new Object[] { transportNode}, connectContext, onConnect);
          try
          {
            onConnect.run( connectContext);
          }
          catch( Exception e)
          {
            handleException( e);
          }
        }
      });
    }
    return false;
  }

  @Override
  public boolean notifyDisconnect( final ITransportImpl transport, final IContext transportContext) throws IOException
  {
    if ( onDisconnect != null) 
    {
      transportContext.getExecutor().execute( new Runnable() {
        public void run()
        {
          StatefulContext disconnectContext = new StatefulContext( transportContext);
          
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
          
          ScriptAction.passVariables( new Object[] { transportNode}, disconnectContext, onDisconnect);
          try
          {
            onDisconnect.run( disconnectContext);
          }
          catch( Exception e)
          {
            handleException( e);
          }
        }
      });
    }
    return false;
  }

  @Override
  public boolean notifyReceive( final ITransportImpl transport, final IModelObject envelope, final IContext messageContext, final IModelObject requestEnvelope)
  {
    if ( transport.getProtocol().envelope().getType( envelope) == Type.ack)
      return false;
    
    if ( onReceive != null) 
    {
      messageContext.getExecutor().execute( new Runnable() {
        public void run()
        {
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
          
          synchronized( messageContext)
          {
            ScriptAction.passVariables( new Object[] { transportNode, unwrap( envelope), unwrap( requestEnvelope)}, messageContext, onReceive);
          }
          
          try
          {
            onReceive.run( messageContext);
          }
          catch( Exception e)
          {
            handleException( e);
          }
        }
      });
    }
    
    return false;
  }

  @Override
  public boolean notifyRegister( final ITransportImpl transport, final IContext transportContext, final String name)
  {
    if ( onRegister != null) 
    {
      transportContext.getExecutor().execute( new Runnable() {
        public void run()
        {
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
          
          synchronized( transportContext)
          {
            ScriptAction.passVariables( new Object[] { transportNode, name}, transportContext, onRegister);
          }
    
          try
          {
            onRegister.run( transportContext);
          }
          catch( Exception e)
          {
            handleException( e);
          }
        }
      });
    }
    
    return false;
  }

  @Override
  public boolean notifyDeregister( final ITransportImpl transport, final IContext transportContext, final String name)
  {
    if ( onDeregister != null) 
    {
      transportContext.getExecutor().execute( new Runnable() {
        public void run()
        {
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
          
          synchronized( transportContext)
          {
            ScriptAction.passVariables( new Object[] { transportNode, name}, transportContext, onDeregister);
          }
    
          try
          {
            onDeregister.run( transportContext);
          }
          catch( Exception e)
          {
            handleException( e);
          }
        }
      });
    }
    
    return false;
  }

  @Override
  public boolean notifyError( final ITransportImpl transport, final IContext context, final Error error, final IModelObject requestEnvelope)
  {
    if ( onError != null) 
    {
      context.getExecutor().execute( new Runnable() {
        public void run()
        {
          StatefulContext messageContext = new StatefulContext( context);
          
          ModelObject transportNode = new ModelObject( "transport");
          transportNode.setValue( transport);
    
          synchronized( messageContext)
          {
            ScriptAction.passVariables( new Object[] { transport, error.toString(), unwrap( requestEnvelope)}, messageContext, onError);
          }
    
          try
          {
            onError.run( messageContext);
          }
          catch( Exception e)
          {
            handleException( e);
          }
        }
      });
    }
    
    return false;
  }
  
  private void handleException( Exception e)
  {
    SLog.exception( this, e);
  }

  @Override
  public boolean notifyException( ITransportImpl transport, IOException e)
  {
    return false;
  }
  
  private Object unwrap( IModelObject envelope)
  {
    return transport.getProtocol().envelope().getMessage( envelope);
  }
  
  private ITransport transport;
  private IXAction onConnect;
  private IXAction onDisconnect;
  private IXAction onRegister;
  private IXAction onDeregister;
  private IXAction onReceive;
  private IXAction onError;
}