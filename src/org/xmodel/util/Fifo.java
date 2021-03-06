/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Fifo.java
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
//
// File: Fifo.java
//    (JDK 1.3)
//
// Copyright (c) 2000,2001 Cyberwerx, Inc.
// The copyright to the code herein is the property of
// Cyberwerx, Inc. Cary, North Carolina, USA.  Content may
// be used or copied only with wrtten permission of Cyberwerx, Inc.
// or in accordance with the terms and conditions stipulated in the
// agreement/contract under which this file was been supplied.
//
// Contact Information:
//    Bob Dunnagan <bdunnagan@cyberwerx.com>
//
// Revision History:
//    Date       Author             Description
//    ---------- ------------------ --------------------------------
//    01-28-02   Bob Dunnagan       Created
//

package org.xmodel.util;

import java.util.*;

/**
  A first-in, first-out container implemented using a LinkedList.
*/
@SuppressWarnings("serial")
public class Fifo<T> extends ArrayList<T>
{
  /**
    Construct an empty fifo.
  */
  public Fifo()
  {
  }

  /**
    Push an object onto the end of the fifo.
    @param object An object to put on the end of the fifo.
  */
  public void push( T object)
  {
    add( object);
  }
  
  /**
   * Push a collection of objects onto the end of the fifo.
   * @param objects The objects to put on the end of the fifo.
   */
  @SuppressWarnings("unchecked")
  public void push( Collection<T> objects)
  {
    for( Object object: objects) push( (T)object);
  }

  /**
    Pop an object off the beginning of the fifo.
    @param object An object to pop off the beginning of the fifo.
    @throws EmptyStackException If no more elements exist.
  */
  public T pop()
  {
    if ( size() == 0) throw new EmptyStackException();
    return remove( 0);
  }

  /**
    Peek at the object at the beginning of the fifo.
    @return The object at the beginning of the fifo.
  */
  public T peek()
  {
    return get( 0);
  }
  
  /**
   * Returns true if the fifo is empty.
   * @return Returns true if the fifo is empty.
   */
  public boolean empty()
  {
    return size() == 0;
  }
}

