/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * TransformException.java
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
