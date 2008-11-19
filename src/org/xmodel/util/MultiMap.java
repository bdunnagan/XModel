/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
//
// File: MultiMap.java
// (JDK 1.3)
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
//    03-14-02   Bob Dunnagan       Created
//

package org.xmodel.util;

import java.util.*;

/**
  This interface is similar to a Map except that it allows multiple entries
  for a single key.
*/
public interface MultiMap<K, T>
{
  /**
   * Removes all mappings from this map.
   */
  public void clear();

  /**
   * Returns true if the map contains this key.
   * @param key The key to test.
   * @return Returns true if the map contains this key.
   */
  public boolean containsKey( Object key);

  /**
   * Returns true if the map contains this value.
   * @param value The value to test.
   * @return Returns true if the map contains this value.
   */
  public boolean containsValue( Object value);

  /**
   * Implementation of equals.
   * @param object An object test compare to this map.
   * @return Returns true if this map equals object.
   */
  public boolean equals( Object object);

  /**
   * Returns the first value matching key.
   * @param key The key to lookup.
   * @return Returns the first value matching key.
   */
  public T getFirst( K key);

  /**
   * Returns an iterator over the values mapped under key.
   * @param key The key whose values will be iterated.
   * @return Returns an iterator over the values mapped under key.
   */
  public Iterator<T> iterator( K key);

  /**
   * Returns true if the map is empty.
   * @return Returns true if the map is empty.
   */
  public boolean isEmpty();

  /**
   * Returns the set of keys contained in this map.
   * @return Returns the set of key contained in this map.
   */
  public Set<K> keySet();

  /**
   * Returns the list of values defined for the specified key.
   * @param key The key.
   * @return Returns the list of values defined for the specified key.
   */
  public List<T> get( K key);
  
  /**
   * Adds a key/value mapping.
   * @param key The key for this mapping.
   * @param value The value for this mapping.
   */
  public void put( K key, T value);

  /**
   * Add the specified values to the specified key.
   * @param key The key.
   * @param values The values to be added.
   */
  public void putAll( K key, Collection<T> values);
  
  /**
   * Adds all the key/value mappings from another map.
   * @param map The source of the new key/value mappings.
   */
  public void putAll( MultiMap<K, T> map);

  /**
    Removes a key/value mapping.
    @param key The key for the mapping.
    @param value The value for the mapping.
  */
  public boolean remove( K key, T value);

  /**
   * Removes all the mappings for key.
   * @param key The key whose mappings will be removed.
   * @return
   */
  public List<T> removeAll( K key);

  /**
   * Returns the count of the mappings under key.
   * @param key A key in the map.
   * @return Returns the count of the mappings under key.
   */
  public int size( K key);

  /**
   * Returns the count of all mappings.
   * @return Returns the count of all mappings.
   */
  public int size();

  /**
   * Returns a collection view of the values in this map.
   * @return Returns a list of the values in this map.
   */
  public List<T> values();
};
