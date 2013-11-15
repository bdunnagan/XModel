package org.xmodel.caching.sql.transform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractSequentialList;
import java.util.ListIterator;

import org.xmodel.IModelObject;
import org.xmodel.log.SLog;

/**
 * An implementation of java.util.List that uses a JDBC ResultSet in "cursor mode" to efficiently 
 * visit each row in a large result set.  This class discards the previous element when a new element
 * is visited.  Note that it is the responsibility of the client to insure that the ResultSet is
 * configured for streaming/cursor use.
 */
public abstract class SQLCursorList extends AbstractSequentialList<IModelObject>
{
  protected SQLCursorList( ResultSet cursor) throws SQLException
  {
    this.cursor = cursor;
  }
  
  /* (non-Javadoc)
   * @see java.util.AbstractSequentialList#listIterator(int)
   */
  @Override
  public ListIterator<IModelObject> listIterator( int index)
  {
    return new SQLCursorIterator( this);
  }
  
  /**
   * Transform the current row from the specified cursor.
   * @param cursor The cursor.
   * @return Returns null or the transformed row.
   */
  protected abstract IModelObject transform( ResultSet cursor);

  /* (non-Javadoc)
   * @see java.util.AbstractCollection#size()
   */
  @Override
  public int size()
  {
    throw new UnsupportedOperationException();
  }

  private ResultSet cursor;
  
  private class SQLCursorIterator implements ListIterator<IModelObject>
  {
    public SQLCursorIterator( SQLCursorList list)
    {
      try
      {
        this.list = list;
        hasNext = list.cursor.next();
      }
      catch( SQLException e)
      {
        throw new RuntimeException( "Iteration interrupted", e);
      }
    }
    
    /* (non-Javadoc)
     * @see java.util.ListIterator#hasNext()
     */
    @Override
    public boolean hasNext()
    {
      return hasNext;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#hasPrevious()
     */
    @Override
    public boolean hasPrevious()
    {
      return false;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#next()
     */
    @Override
    public IModelObject next()
    {
      try
      {
        IModelObject row = list.transform( cursor);
        hasNext = list.cursor.next();
        return row;
      }
      catch( SQLException e)
      {
        throw new RuntimeException( "Iteration interrupted", e);
      }
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#nextIndex()
     */
    @Override
    public int nextIndex()
    {
      // TODO Auto-generated method stub
      return 0;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#previous()
     */
    @Override
    public IModelObject previous()
    {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#previousIndex()
     */
    @Override
    public int previousIndex()
    {
      // TODO Auto-generated method stub
      return 0;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#add(java.lang.Object)
     */
    @Override
    public void add( IModelObject arg0)
    {
      throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#remove()
     */
    @Override
    public void remove()
    {
      throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#set(java.lang.Object)
     */
    @Override
    public void set( IModelObject arg0)
    {
      throw new UnsupportedOperationException();
    }

    private SQLCursorList list;
    private boolean hasNext;
  }
}
