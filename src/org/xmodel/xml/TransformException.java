/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xml;

/**
 * An unchecked exception for errors encountered while processing an ITransform.
 */
@SuppressWarnings("serial")
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
