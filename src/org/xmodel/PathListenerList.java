/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.path.IListenerChain;
import org.xmodel.xpath.expression.IContext;


/**
 * This class stores the IListenerChain instances installed on a particular context object.
 * Each IModelObject implementation must support an association to one of these instances.
 */
public class PathListenerList
{
  public PathListenerList()
  {
    chains = new ArrayList<IListenerChain>( 1);
  }
  
  /**
   * Add an IListenerChain.
   * @param chain The chain to be added.
   */
  public void addListenerChain( IListenerChain chain)
  {
    chains.add( chain);
  }
  
  /**
   * Lookup an IListenerChain instance by its IContext and IPathListener.
   * @param context The context.
   * @param listener The listener.
   * @return Returns the IListenerChain instance.
   */
  public IListenerChain findListenerChain( IContext context, IPathListener listener)
  {
    for( int i=0; i<chains.size(); i++)
    {
      IListenerChain chain = chains.get( i);
      if ( chain.getContext() != context && !chain.getContext().equals( context)) continue; 
      if ( chain.getPathListener() != listener) continue;
      return chain;
    }
    return null;
  }

  /**
   * Remove the IListenerChain instance associated with the specified IContext and IPathListener.
   * @param context The context.
   * @param listener The listener.
   * @return Returns the IListenerChain that was removed.
   */
  public IListenerChain removeListenerChain( IContext context, IPathListener listener)
  {
    for( int i=0; i<chains.size(); i++)
    {
      IListenerChain chain = chains.get( i);
      if ( chain.getContext() != context && !chain.getContext().equals( context)) continue; 
      if ( chain.getPathListener() != listener) continue;
      return chains.remove( i);
    }
    return null;
  }

  List<IListenerChain> chains;
}
