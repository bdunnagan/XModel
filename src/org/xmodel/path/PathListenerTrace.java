/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.xmodel.IModelObject;
import org.xmodel.path.PathListenerTraceEntry.Event;


/**
 * A class for initiating and storing the results of an IPathListener trace.
 */
public class PathListenerTrace
{
  /**
   * Create an IPathListener trace.
   */
  public PathListenerTrace()
  {
    entries = new ArrayList<PathListenerTraceEntry>();
    leavesOnly = true;
  }

  /**
   * Set whether entries should be printed to stdout as they're added.
   * @param print True if entries should be printed.
   */
  public void setPrint( boolean print)
  {
    this.print = print;
  }
  
  /**
   * Set the regular expression used to filter the stack frames when printing to stdout.
   * @param regex The regular expression.
   */
  public void setStackFilter( String regex)
  {
    pattern = Pattern.compile( regex);
  }
  
  /**
   * If the leavesOnly flag is true then trace entries will only be generated for the leaves of the path. 
   * Otherwise, trace entries are generated for every location step of the path. (default: true)
   * @param leavesOnly True if only leaves should be traced.
   */
  public void setLeavesOnly( boolean leavesOnly)
  {
    this.leavesOnly = leavesOnly;
  }
  
  /**
   * Returns true if this trace is only tracking the leaves of the path.
   * @return Returns true if this trace is only tracking the leaves of the path.
   */
  public boolean isLeavesOnly()
  {
    return leavesOnly;
  }

  /**
   * Calculate the list of objects on which the associated IPathListener is installed according to the
   * the set of PathListenerTraceEntry instances which have been recorded. <b>Note that this set will not
   * necessarily include all objects where the listener is installed.</b>
   * @return Returns the list of objects on which the associated IPathListener is installed.
   */
  public List<IModelObject> calculateExtent()
  {
    Map<IModelObject, IModelObject> extent = new HashMap<IModelObject, IModelObject>();
    for ( PathListenerTraceEntry entry: entries)
    {
      if ( !entry.isLeaf()) continue;
      if ( entry.getEvent() == Event.install)
      {
        for ( IModelObject target: entry.getTargets())
          extent.put( target, target);
      }
      else
      {
        for ( IModelObject target: entry.getTargets())
          extent.remove( target);
      }
    }
    
    return new ArrayList<IModelObject>( extent.keySet());
  }
  
  /**
   * Add an entry to the trace.
   * @param entry The entry to be added.
   */
  void addEntry( PathListenerTraceEntry entry)
  {
    entries.add( entry);
    if ( print) System.out.println( (pattern != null)? entry.toString( pattern): entry.toString());
  }
  
  List<PathListenerTraceEntry> entries;
  Pattern pattern;
  boolean print;
  boolean leavesOnly;
}
