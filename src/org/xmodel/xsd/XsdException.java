/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xsd;

import org.xmodel.xml.XmlException;

/**
 * An exception generated when parsing XSD documents.
 */
@SuppressWarnings("serial")
public class XsdException extends XmlException
{
  /**
   * Create an exception with the specified message.
   * @param message The message.
   */
  public XsdException( String message)
  {
    super( message);
  }

  /**
   * Create an exception with the specified message and cause.
   * @param message The message.
   * @param cause The cause.
   */
  public XsdException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
