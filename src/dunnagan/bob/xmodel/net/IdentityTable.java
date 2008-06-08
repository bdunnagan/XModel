/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dunnagan.bob.xmodel.IModelObject;

/**
 * A bidirectional map between objects and identifiers.
 */
public class IdentityTable
{
  public IdentityTable()
  {
    counter = 0;
    forward = new HashMap<String, IModelObject>();
    reverse = new HashMap<IModelObject, String>();
  }
  
  /**
   * Insert an object into the map and return its identifier.
   * @param object The object.
   * @return Returns the new identifier.
   */
  public String insert( IModelObject object)
  {
    String id = Long.toString( counter++, 36).toUpperCase();
    //System.err.printf( "insert: %s %s\n", id, object);
    forward.put( id, object);
    reverse.put( object, id);
    return id;
  }
  
  /**
   * Insert an object into the map with the specified identifier.
   * @param id The identifier.
   * @param object The object.
   */
  public void insert( String id, IModelObject object)
  {
    //System.err.printf( "insert: %s %s\n", id, object);
    forward.put( id, object);
    reverse.put( object, id);
  }
  
  /**
   * Remove an identifier from the map and return its object.
   * @param id The identifier.
   * @return Returns the associated object.
   */
  public IModelObject remove( String id)
  {
    //System.err.printf( "remove: %s\n", id);
    IModelObject object = forward.remove( id);
    reverse.remove( object);
    return object;
  }
  
  /**
   * Remove an object from the map and returns it identifier.
   * @param object The object.
   */
  public String remove( IModelObject object)
  {
    String id = reverse.remove( object);
    //System.err.printf( "remove: %s %s\n", id, object);
    forward.remove( id);
    return id;
  }
  
  /**
   * Clear the table.
   */
  public void clear()
  {
    forward.clear();
    reverse.clear();
  }
  
  /**
   * Get an object using its identifier.
   * @param id The identifier.
   * @return Returns the object.
   */
  public IModelObject get( String id)
  {
    return forward.get( id);
  }
  
  /**
   * Get the identifier for the specified object.
   * @param object The object.
   * @return Returns the identifier.
   */
  public String get( IModelObject object)
  {
    return reverse.get( object);
  }
  
  /**
   * Returns all the objects in the table.
   * @return Returns all the objects in the table.
   */
  public Set<IModelObject> getObjects()
  {
    return reverse.keySet();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "COUNT: "); sb.append( forward.size());
    sb.append( '\n');
    for( Map.Entry<String, IModelObject> entry: forward.entrySet())
    {
      sb.append( entry.getKey());
      sb.append( " <-> ");
      sb.append( entry.getValue());
      sb.append( '\n');
    }
    sb.append( '\n');
    return sb.toString();
  }
  
  private long counter;
  private HashMap<String, IModelObject> forward;
  private HashMap<IModelObject, String> reverse;
}
