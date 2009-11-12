/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * PathListenerTraceEntry.java
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
package org.xmodel.path;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.xmodel.IModelObject;
import org.xmodel.IPath;
import org.xmodel.ModelAlgorithms;


public class PathListenerTraceEntry
{
  public enum Event { install, remove};
  
  public PathListenerTraceEntry( Event event, IPath path, int pathIndex)
  {
    this.event = event;
    this.path = path;
    this.pathIndex = pathIndex;
    this.targets = new ArrayList<IModelObject>();
    this.stack = Thread.currentThread().getStackTrace();
  }
  
  /**
   * Add a target to the list of targets.
   * @param target The target to be added.
   */
  public void addTarget( IModelObject target)
  {
    if ( !targets.contains( target)) targets.add( target);
  }
  
  /**
   * Add the specified targets to the list of targets.
   * @param targets The targets to be added.
   */
  public void addTargets( List<IModelObject> targets)
  {
    for ( IModelObject target: targets) addTarget( target);
  }
  
  /**
   * Returns the list of targets.
   * @return Returns the list of targets.
   */
  public List<IModelObject> getTargets()
  {
    return targets;
  }

  /**
   * Returns the event type of this entry.
   * @return Returns the event type of this entry.
   */
  public Event getEvent()
  {
    return event;
  }
  
  /**
   * Returns the path index of this entry.
   * @return Returns the path index of this entry.
   */
  public int getPathIndex()
  {
    return pathIndex;
  }
  
  /**
   * Returns true if this entry is associated with a leaf of the path.
   * @return Returns true if this entry is associated with a leaf of the path.
   */
  public boolean isLeaf()
  {
    return pathIndex == path.length();
  }
  
  /**
   * Return a string representation of the TraceEntry and use the specified regular expression to include
   * fully qualified class names in the stack trace which match the expression.
   * @param regex The filter regular expression.
   * @return Returns a string representation of the TraceEntry.
   */
  public String toString( Pattern regex)
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printer = new PrintStream( stream);
    
    // print path information
    String element = "leaf";
    if ( pathIndex < path.length()) element = path.getPathElement( pathIndex).toString();
    printer.printf( "%s(%d) '%s' of '%s' at:\n", event.toString(), pathIndex, element, path);
    
    // print targets
    for ( int i=0; i<targets.size(); i++)
      printer.printf( "    %d. '%s'\n", (i+1), ModelAlgorithms.createIdentityPath( targets.get( i)));
        
    // print stack trace
    boolean skipped = false;
    printer.println( "");
    for ( int i=0; i<stack.length; i++)
    {
      String className = stack[ i].getClassName();
      if ( regex.matcher( className).matches())
      {
        printer.printf( "    %s\n", stack[ i]);
        skipped = false;
      }
      else if ( !skipped)
      {
        printer.printf( "    ...\n");
        skipped = true;
      }
    }
    
    // return result
    return stream.toString();
  }

  /**
   * Returns a string representation of the TraceEntry including a complete stack trace.
   */
  public String toString()
  {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printer = new PrintStream( stream);
    
    // print path information
    String element = "leaf";
    if ( pathIndex < path.length()) element = path.getPathElement( pathIndex).toString();
    printer.printf( "%s(%d) '%s' of '%s' on:\n", event.toString(), pathIndex, element, path);
    
    // print targets
    for ( int i=0; i<targets.size(); i++)
      printer.printf( "    %d. '%s'\n", (i+1), ModelAlgorithms.createTypePath( targets.get( i)));
        
    // print stack trace
    printer.printf( "\nStack:\n");
    for ( int i=0; i<stack.length; i++)
      printer.printf( "    %s\n", stack[ i]);
    
    // return result
    return stream.toString();
  }
  
  Event event;
  IPath path;
  int pathIndex;
  List<IModelObject> targets;
  StackTraceElement[] stack;
}
