package dunnagan.bob.xmodel.xaction;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelObjectFactory;
import dunnagan.bob.xmodel.Xlate;

/**
 * A runtime exception which occurred during the processing of an XAction. This exception class exists
 * so that receivers can distinguish between exceptions caught during XAction processing and subsequently
 * rethrown, and exceptions which were not caught.  A caught, but unhandled exception, is always rethrown
 * as an XActionException.
 */
@SuppressWarnings("serial")
public class XActionException extends RuntimeException
{
  public XActionException()
  {
    super();
  }

  public XActionException( String message, Throwable cause)
  {
    super( message, cause);
  }

  public XActionException( String message)
  {
    super( message);
  }

  public XActionException( Throwable message)
  {
    super( message);
  }
 
  /**
   * Returns the exception fragment containing the cause.
   * @param throwable The exception.
   * @param factory Null or a factory.
   * @return Returns the exception fragment containing the cause.
   */
  public IModelObject createExceptionFragment( IModelObjectFactory factory)
  {
    return createExceptionFragment( this, factory);
  }
  
  /**
   * Returns the exception fragment containing the cause.
   * @param throwable The exception.
   * @param factory Null or a factory.
   * @return Returns the exception fragment containing the cause.
   */
  public static IModelObject createExceptionFragment( Throwable throwable, IModelObjectFactory factory)
  {
    if ( factory == null) factory = new ModelObjectFactory();
    
    try
    {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      throwable.printStackTrace( new PrintStream( stream));
      stream.flush();

      IModelObject root = factory.createObject( null, "exception");
      Xlate.childSet( root, "message", throwable.getMessage());
      Xlate.childSet( root, "stack", stream.toString());

      IModelObject causeElement = root.getCreateChild( "cause");
      Throwable cause = throwable.getCause();
      if ( cause != null && cause != throwable) causeElement.addChild( createExceptionFragment( cause, factory));

      stream.close();
      
      return root;
    }
    catch( Exception e)
    {
      e.printStackTrace( System.err);
    }
    
    return null;
  }
}
