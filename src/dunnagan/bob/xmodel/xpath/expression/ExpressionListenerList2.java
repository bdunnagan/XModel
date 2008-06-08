/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xpath.expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;

/**
 * An implementation of IExpressionListener which contains IExpressionListener children and forwards
 * listener notifications to its children. Children can be added and removed during notification. Because
 * IExpression instances can be bound to multiple contexts, listeners must be associated with a particular
 * context when registered.
 * <p>
 * This alternate implementation is broken, but I'm keeping it around for posterity.
 */
public class ExpressionListenerList2 implements IExpressionListener
{
  public ExpressionListenerList2()
  {
    entries = new ArrayList<Entry>( 1);
  }

  /**
   * Add a listener associated with the specified context object.  The listener will be notified
   * whenever an event occurs for the specified context.
   * @param context The context.
   * @param listener The listener to be added.
   */
  public void addListener( IContext context, IExpressionListener listener)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null)
    {
      Entry entry = new Entry();
      entry.context = context;
      entry.listeners.add( listener);
      if ( listener.requiresValueNotification()) requiresValueNotification = true;
    }
    else if ( !listeners.contains( listener))
    {
      listeners.add( listener);
      if ( listener.requiresValueNotification()) requiresValueNotification = true;
    }
  }

  /**
   * Remove a listener associated with the specified context object.
   * @param context The context.
   * @param listener The listener to be removed.
   * @return Returns true if the listener was removed.
   */
  public boolean removeListener( IContext context, IExpressionListener listener)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners != null) return listeners.remove( listener);
    return false;
  }
  
  /**
   * Returns the set of contexts associated with all the listeners in this list.
   * @return Returns the set of contexts associated with all the listeners in this list.
   */
  public Collection<IContext> getContexts()
  {
    return findContexts();
  }
  
  /**
   * Returns the listeners installed for the specified context.
   * @param context The context.
   * @return Returns the listeners installed for the specified context.
   */
  public List<IExpressionListener> getListeners( IContext context)
  {
    return findListeners( context);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyAdd(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyAdd( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).notifyAdd( expression, context, nodes);
      }
      catch( Exception e)
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyRemove(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.util.List)
   */
  public void notifyRemove( IExpression expression, IContext context, List<IModelObject> nodes)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).notifyRemove( expression, context, nodes);
      }
      catch( Exception e)
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * boolean)
   */
  public void notifyChange( IExpression expression, IContext context, boolean newValue)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).notifyChange( expression, context, newValue);
      }
      catch( Exception e)
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * double, double)
   */
  public void notifyChange( IExpression expression, IContext context, double newValue, double oldValue)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).notifyChange( expression, context, newValue, oldValue);
      }
      catch( Exception e)
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.lang.String, java.lang.String)
   */
  public void notifyChange( IExpression expression, IContext context, String newValue, String oldValue)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).notifyChange( expression, context, newValue, oldValue);
      }
      catch( Exception e)
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e);
      }
    }       
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyChange(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void notifyChange( IExpression expression, IContext context)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).notifyChange( expression, context);
      }
      catch( Exception e)
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e);
      }
    }       
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#requiresValueNotification()
   */
  public boolean requiresValueNotification()
  {
    return requiresValueNotification;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#notifyValue(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext[], 
   * dunnagan.bob.xmodel.IModelObject, java.lang.Object, java.lang.Object)
   */
  public void notifyValue( IExpression expression, IContext[] contexts, IModelObject object, Object newValue, Object oldValue)
  {
    for( IContext context: contexts)
    {
      List<IExpressionListener> listeners = findListeners( context);
      if ( listeners == null) continue;

      IContext[] selection = new IContext[ 1];
      Object[] array = listeners.toArray();
      for ( int i=0; i<array.length; i++)
      {
        try
        {
          selection[ 0] = context;
          ((IExpressionListener)array[ i]).notifyValue( expression, selection, object, newValue, oldValue);
        }
        catch( Exception e)
        {
          ((IExpressionListener)array[ i]).handleException( expression, context, e);
        }
      }       
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xpath.expression.IExpressionListener#handleException(
   * dunnagan.bob.xmodel.xpath.expression.IExpression, dunnagan.bob.xmodel.xpath.expression.IContext, 
   * java.lang.Exception)
   */
  public void handleException( IExpression expression, IContext context, Exception e1)
  {
    List<IExpressionListener> listeners = findListeners( context);
    if ( listeners == null) return;
    
    Object[] array = listeners.toArray();
    for ( int i=0; i<array.length; i++)
    {
      try
      {
        ((IExpressionListener)array[ i]).handleException( expression, context, e1);
      }
      catch( Exception e2)
      {
        e2.printStackTrace( System.err);
      }
    }       
  }

  /**
   * Returns the set of contexts.
   * @return Returns the set of contexts.
   */
  protected Collection<IContext> findContexts()
  {
    List<IContext> result = new ArrayList<IContext>();
    for( Entry entry: entries) result.add( entry.context);
    return result;
  }
  
  /**
   * Returns the listeners for the specified context.
   * @param context The context.
   * @return Returns the listeners for the specified context.
   */
  protected List<IExpressionListener> findListeners( IContext context)
  {
    for( Entry entry: entries)
      if ( entry.context.equals( context))
        return entry.listeners;
    return null;
  }
  
  class Entry
  {
    IContext context;
    List<IExpressionListener> listeners = new ArrayList<IExpressionListener>( 1);
  }

  List<Entry> entries;
  boolean requiresValueNotification;
}
