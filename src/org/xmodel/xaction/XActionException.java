/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * XActionException.java
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
package org.xmodel.xaction;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.log.Log;


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

  public XActionException( XActionDocument document, String message)
  {
    super( message);
    setLocation( document);
  }

  public XActionException( XActionDocument document, String message, Throwable cause)
  {
    super( message, cause);
    setLocation( document);
  }

  public XActionException( String message)
  {
    super( message);
  }

  public XActionException( String message, Throwable cause)
  {
    super( message, cause);
  }

  public XActionException( Throwable cause)
  {
    super( cause);
  }
  
  public String getMessage()
  {
    if ( document == null) return super.getMessage();
    IPath path = ModelAlgorithms.createIdentityPath( document.getRoot());
    return String.format( "%s: %s", super.getMessage(), path.toString());
  }
  
  /**
   * Set the location of an XActionException.
   * @param document The configuration document.
   */
  public void setLocation( XActionDocument document)
  {
    this.document = document;
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
      String message = throwable.getMessage();
      if ( message == null || message.length() == 0) message = throwable.getClass().getCanonicalName();
      Xlate.childSet( root, "message", message);
      Xlate.childSet( root, "stack", stream.toString());

      IModelObject causeElement = root.getCreateChild( "cause");
      Throwable cause = throwable.getCause();
      if ( cause != null && cause != throwable) causeElement.addChild( createExceptionFragment( cause, factory));

      stream.close();
      
      return root;
    }
    catch( Exception e)
    {
      log.exception( e);
    }
    
    return null;
  }
  
  private static Log log = Log.getLog( "org.xmodel.xaction");
  
  private XActionDocument document;
}
