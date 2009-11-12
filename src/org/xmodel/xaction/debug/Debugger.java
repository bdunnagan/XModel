/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * Debugger.java
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
package org.xmodel.xaction.debug;

import java.io.File;
import java.net.URL;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import org.xmodel.IModelObject;
import org.xmodel.net.ModelServer;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.debug.GlobalDebugger.Breakpoint;
import org.xmodel.xpath.expression.Context;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

/**
 * A class which manages the debugging execution context of an XAction script.
 */
public class Debugger implements IDebugger
{
  public Debugger( GlobalDebugger global, String threadID, String threadName, ModelServer server)
  {
    this.global = global;
    this.threadID = threadID;
    this.threadName = threadName;
    this.server = server;
    this.lock = new Semaphore( 0);
    this.stack = new Stack<Frame>();
    this.step = Step.RESUME;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#push(org.xmodel.xpath.expression.IContext, org.xmodel.xaction.IXAction)
   */
  public void push( IContext context, IXAction action)
  {
    Frame parent = stack.isEmpty()? null: stack.peek();
    
    // update stack
    Frame frame = new Frame();
    frame.parent = parent;
    frame.context = context;
    frame.action = action;
    stack.push( frame);
    
    switch( step)
    {
      case SUSPEND:     block( "request"); break;
      case STEP_INTO:   block( "stepEnd"); break;
      
      case STEP_OVER:   
        if ( poppedFrame == stepFromFrame || poppedFrame == stepFromFrame.parent) block( "stepEnd"); 
        break;
      
      default:
    }
    
    // check breakpoints
    for( Breakpoint breakpoint: global.getBreakpoints())
      if ( isBreakpoint( breakpoint, action))
        block( "breakpoint");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#pop()
   */
  public Frame pop()
  {
    // script already executing when debugger was connected
    if ( stack.isEmpty())
    {
      step = Step.RESUME;
      return null;
    }
    
    // pop
    poppedFrame = stack.pop();
    
    // step return
    if ( step == Step.STEP_RETURN && stepFromFrame != null && poppedFrame == stepFromFrame.parent) 
      block( "stepEnd");
      
    // return popped frame
    return poppedFrame;
  }
    
  /**
   * Block and send debug status to server.
   * @param action The action causing the block.
   */
  protected void block( String action)
  {
    stepFromFrame = stack.peek();
    server.sendDebugMessage( threadID, threadName, "suspended", action, stack);
    
    // drain permits after sending status to insure synchronization
    try { lock.acquire();} catch( InterruptedException e) {}
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#report()
   */
  public void report()
  {
    server.sendDebugMessage( threadID, threadName, "report", null, null);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#suspend()
   */
  public void suspend()
  {
    synchronized( this) { step = Step.SUSPEND;}
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#resume()
   */
  public void resume()
  {
    if ( stepFromFrame != null)
    {
      synchronized( this) { step = Step.RESUME;}
    
      // ignore exceptions during resume in case the socket has already been closed
      try { server.sendDebugMessage( threadID, threadName, "resumed", "request", null);} catch( Exception e) {}
    
      lock.release();
      stepFromFrame = null;
    }
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepInto()
   */
  public void stepInto()
  {
    if ( stack.isEmpty()) return;
    synchronized( this) { step = Step.STEP_INTO;}
    server.sendDebugMessage( threadID, threadName, "resumed", "stepInto", null);
    lock.release();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepOver()
   */
  public void stepOver()
  {
    if ( stack.isEmpty()) return;
    synchronized( this) { step = Step.STEP_OVER;}
    server.sendDebugMessage( threadID, threadName, "resumed", "stepOver", null);
    lock.release();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#stepReturn()
   */
  public void stepReturn()
  {
    if ( stack.isEmpty()) return;
    synchronized( this) { step = Step.STEP_RETURN;}
    server.sendDebugMessage( threadID, threadName, "resumed", "stepReturn", null);
    lock.release();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#createBreakpoint(java.lang.String, java.lang.String)
   */
  public void createBreakpoint( String file, String path)
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#removeBreakpoint(java.lang.String, java.lang.String)
   */
  public void removeBreakpoint( String file, String path)
  {
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#getVariableValue(int, java.lang.String)
   */
  public Object getVariableValue( int stackFrameIndex, String variable)
  {
    if ( stack.size() <= stackFrameIndex) return "";
    Frame frame = stack.get( stackFrameIndex);
    IVariableScope scope = frame.context.getScope();
    return scope.get( variable);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.debug.IDebugger#setFilters(org.xmodel.xpath.expression.IExpression, org.xmodel.xpath.expression.IExpression)
   */
  public void setFilters( IExpression fileFilter, IExpression scriptFilter)
  {
  }

  /**
   * Returns true if the specified action matches the breakpoint location.
   * @param breakpoint The breakpoint.
   * @param action The action.
   * @return Returns true if the specified action matches the breakpoint location.
   */
  protected boolean isBreakpoint( Breakpoint breakpoint, IXAction action)
  {
    IExpression fileFilter = global.getFileFilter();
    if ( fileFilter == null) return false;
    
    IExpression scriptFilter = global.getScriptFilter();
    if ( scriptFilter == null) return false;
    
    try
    {
      IModelObject element = action.getDocument().getRoot();
      if ( element == null) return false;
      
      String spec = fileFilter.evaluateString( new Context( element));
      if ( spec.length() == 0) return false;
      
      IModelObject root = scriptFilter.queryFirst( element);
      if ( root == null) return false;
    
      // verify file name
      URL url = new URL( spec);
      File filePath = new File( url.getPath());
      String fileName = filePath.getName();
      if ( !breakpoint.file.equals( fileName)) return false;
    
      // verify locus
      IModelObject leaf = breakpoint.path.queryFirst( root);
      return leaf == element;
    }
    catch( Exception e)
    {
      return false;
    }
  }
  
  private GlobalDebugger global;
  private String threadID;
  private String threadName;
  private ModelServer server;
  private Semaphore lock;
  private Stack<Frame> stack;
  private Step step;
  private Frame stepFromFrame;
  private Frame poppedFrame;
}
