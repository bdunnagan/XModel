/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xsd;

import dunnagan.bob.xmodel.xml.XmlException;

/**
 * An exception generated when parsing XSD documents.
 */
public class SchemaException extends XmlException
{
  /**
   * Create an exception with the specified message.
   * @param message The message.
   */
  public SchemaException( String message)
  {
    super( message);
  }

  /**
   * Create an exception with the specified message and cause.
   * @param message The message.
   * @param cause The cause.
   */
  public SchemaException( String message, Throwable cause)
  {
    super( message, cause);
  }
}
