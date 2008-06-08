/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xml;

/**
 * An unchecked exception for errors encountered while processing an ITransform.
 */
public class TransformException extends RuntimeException
{
  public TransformException()
  {
    super();
  }

  public TransformException( String message, Throwable cause)
  {
    super( message, cause);
  }

  public TransformException( String message)
  {
    super( message);
  }

  public TransformException( Throwable cause)
  {
    super( cause);
  }
}
