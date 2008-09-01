/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.listeners;

import java.io.PrintStream;
import java.util.*;
import org.xmodel.*;
import org.xmodel.external.NonSyncingIterator;
import org.xmodel.path.IListenerChain;
import org.xmodel.path.ListenerChainLink;
import org.xmodel.path.PredicateGuard;
import org.xmodel.util.HashMultiMap;
import org.xmodel.util.MultiMap;
import org.xmodel.xpath.expression.ExpressionListenerList;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpressionListener;
import org.xmodel.xpath.expression.PathExpression;


/**
 * This class analyzes all the nodes in a model (without synchronizing external references) and
 * produces a report which lists all of the end-user listeners installed in the model and possible
 * listener leaks. A listener leak is defined as a listener structure which is registered for the
 * same end-user listener and context.
 */
public class LeakAnalyzer
{
  /**
   * Create an analyzer that will print its output to System.out.
   */
  public LeakAnalyzer()
  {
    out = System.out;
    entryMap = new HashMultiMap<ClientListener, IModelObject>();
    leakMap = new HashMultiMap<ClientListener, IModelObject>();
  }

  /**
   * Create an analyzer that will print its output to the specified stream.
   * @param stream The stream.
   */
  public LeakAnalyzer( PrintStream stream)
  {
    out = stream;
    entryMap = new HashMultiMap<ClientListener, IModelObject>();
    leakMap = new HashMultiMap<ClientListener, IModelObject>();
  }
  
  /**
   * Analyze the specified model and print the results to the output stream.
   * @param root The root of the model to be analyzed.
   */
  public void analyze( IModelObject root)
  {
    NonSyncingIterator iter = new NonSyncingIterator( root);
    while( iter.hasNext())
    {
      IModelObject object = (IModelObject)iter.next();
      populateClientListeners( object);

      // analyze list for duplicate context+listener
//      List<ClientListener> leaks = findLeaks( listeners);
//      for( ClientListener listener: leaks) leakMap.put( listener, object);
    }
    
    // print analysis
    out.println( "Listeners:");
    List<ClientListener> entries = new ArrayList<ClientListener>( entryMap.keySet());
    Collections.sort( entries, new ClientListenerComparator());
    for( ClientListener entry: entries)
    {
      out.println( entry);
      List<IModelObject> objects = entryMap.get( entry);
      for( IModelObject object: objects)
        out.println( "  "+ModelAlgorithms.createIdentityPath( object));
      out.println( "  Total="+objects.size());
    }
    
    out.println( "\nLeaks:");
    for( ClientListener leak: leakMap.keySet())
    {
      out.println( leak);
      List<IModelObject> objects = entryMap.get( leak);
      for( IModelObject object: objects)
        out.println( "  "+ModelAlgorithms.createIdentityPath( object));
      out.println( "  Total="+objects.size());
    }
  }
  
  /**
   * Populate the maps of the end-user listeners for the specified object.
   * @param object The object.
   */
  protected void populateClientListeners( IModelObject object)
  {
    ModelListenerList list = object.getModelListeners();
    if ( list == null) return;
    
    Set<IModelListener> listeners = list.getListeners();
    for( IModelListener listener: listeners)
      populateFrom( object, listener);
  }

  /**
   * Populate the end-user listeners starting from the specified listener.
   * @param object The object.
   * @param listener The starting point.
   */
  protected void populateFrom( IModelObject object, IModelListener listener)
  {
    if ( listener instanceof ListenerChainLink)
    {
      IListenerChain chain = ((ListenerChainLink)listener).getListenerChain();
      IPathListener pathListener = chain.getPathListener();
      populateFrom( object, chain, pathListener);
    }
    else
    {
      ClientListener clientListener = new ClientListener();
      clientListener.modelListener = listener;
      entryMap.put( clientListener, object);
    }
  }
  
  /**
   * Populate the end-user listeners starting from the specified listener.
   * @param object The object.
   * @param context The context.
   * @param listener The starting point.
   */
  protected void populateFrom( IModelObject object, IContext context, IExpressionListener listener)
  {
    if ( listener instanceof PredicateGuard)
    {
      IListenerChain chain = ((PredicateGuard)listener).getListenerChain();
      IPathListener pathListener = chain.getPathListener();
      populateFrom( object, chain, pathListener);
    }
    else
    {
      ClientListener clientEntry = new ClientListener();
      clientEntry.context = context;
      clientEntry.exprListener = listener;
      entryMap.put( clientEntry, object);
    }
  }
  
  /**
   * Populate the end-user listeners starting from the specified listener.
   * @param object The object.
   * @param chain The listener chain.
   * @param listener The starting point.
   */
  protected void populateFrom( IModelObject object, IListenerChain chain, IPathListener listener)
  {
    if ( listener instanceof PathExpression)
    {
      ExpressionListenerList clients = ((PathExpression)listener).getRoot().getListeners();
      if ( clients != null)
      {
        Collection<IContext> contexts = clients.getContexts();
        for( IContext context: contexts)
        {
          List<IExpressionListener> exprListeners = clients.getListeners( context);
          for( IExpressionListener exprListener: exprListeners)
            populateFrom( object, context, exprListener);
        }
      }
    }
    else
    {
      ClientListener clientEntry = new ClientListener();
      clientEntry.context = chain.getContext();
      clientEntry.pathListener = listener;
      entryMap.put( clientEntry, object);
    }
  }
  
  /**
   * Returns a list of leaked listeners.
   * @param listeners A list of listeners on a single object.
   * @return Returns a list of leaked listeners.
   */
  protected List<ClientListener> findLeaks( List<ClientListener> listeners)
  {
    return null;
  }
  
  PrintStream out;
  MultiMap<ClientListener, IModelObject> entryMap;
  MultiMap<ClientListener, IModelObject> leakMap;
}

class ClientListener
{
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    ClientListener listener = (ClientListener)object;
    if ( (listener.context == null || context == null) && listener.context != context) return false;
    if ( listener.context != null && context != null && !listener.context.equals( context)) return false;
    if ( !compareClass( listener.modelListener, modelListener)) return false;
    if ( !compareClass( listener.pathListener, pathListener)) return false;
    if ( !compareClass( listener.exprListener, exprListener)) return false;
    return true;
  }
  
  /**
   * Returns true if the two arguments are null or their classes are equal.
   * @param arg0 
   * @param arg1
   * @return Returns true if the two arguments are null or their classes are equal.
   */
  protected boolean compareClass( Object arg0, Object arg1)
  {
    if ( (arg0 == null || arg1 == null) && arg0 != arg1) return false;
    if ( arg0 != null && arg1 != null && !arg0.getClass().equals( arg1.getClass())) return false;
    return true;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    int hashCode = 0xf34801df;
    if ( context != null) hashCode = context.hashCode();
    if ( modelListener != null) hashCode ^= modelListener.getClass().hashCode();
    if ( pathListener != null) hashCode ^= pathListener.getClass().hashCode();
    if ( exprListener != null) hashCode ^= exprListener.getClass().hashCode();
    return hashCode;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    if ( modelListener != null) 
    {
      builder.append( "IModelListener=");
      builder.append( modelListener.getClass().getName());
    }
    if ( pathListener != null) 
    {
      builder.append( "IPathListener=");
      builder.append( pathListener.getClass().getName());
    }
    if ( exprListener != null) 
    {
      builder.append( "IExpressionListener=");
      builder.append( exprListener.getClass().getName());
    }
    if ( context != null)
    {
      builder.append( '\n');
      builder.append( "IContext=");
      builder.append( context);
    }
    return builder.toString();
  }
  
  IContext context;
  IModelListener modelListener;
  IPathListener pathListener;
  IExpressionListener exprListener;
}

class ClientListenerComparator implements Comparator<ClientListener>
{
  /* (non-Javadoc)
   * @see java.util.Comparator#compare(T, T)
   */
  public int compare( ClientListener o1, ClientListener o2)
  {
    String className1 = null;
    if ( o1.modelListener != null) className1 = o1.modelListener.getClass().getName();
    if ( o1.pathListener != null) className1 = o1.pathListener.getClass().getName();
    if ( o1.exprListener != null) className1 = o1.exprListener.getClass().getName();
    
    String className2 = null;
    if ( o2.modelListener != null) className2 = o2.modelListener.getClass().getName();
    if ( o2.pathListener != null) className2 = o2.pathListener.getClass().getName();
    if ( o2.exprListener != null) className2 = o2.exprListener.getClass().getName();
    
    return className1.compareTo( className2);
  }
}