package dunnagan.bob.xmodel.xaction;

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
}
