/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XsdException.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
