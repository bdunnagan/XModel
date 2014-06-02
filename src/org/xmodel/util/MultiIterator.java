package org.xmodel.util;

import java.util.Iterator;
import java.util.List;

public class MultiIterator<E> implements Iterator<E>
{
  public void add( Iterator<E> iterator)
  {
    list.add( iterator);
  }
  
  public void add( E item)
  {
    list.add( item);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean hasNext()
  {
    if ( iterator != null)
    {
      boolean hasNext = iterator.hasNext();
      if ( hasNext) return true;
    }
    else if ( item != null)
    {
      return true;
    }
    
    if ( index < list.size())
    {
      Object next = list.get( index++);
      if ( next instanceof Iterator)
      {
        iterator = (Iterator<E>)next;
        return hasNext();
      }
      else
      {
        item = (E)next;
        return true;
      }
    }
    
    return false;
  }

  @Override
  public E next()
  {
    if ( hasNext())
    {
      if ( iterator != null) return iterator.next();
      E next = item; item = null;
      return next;
    }
    
    return null;
  }

  @Override
  public void remove()
  {
    throw new UnsupportedOperationException();
  }
  
  private List<Object> list;
  private Iterator<E> iterator;
  private E item;
  private int index;
}
