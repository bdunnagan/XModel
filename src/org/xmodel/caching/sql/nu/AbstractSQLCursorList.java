package org.xmodel.caching.sql.nu;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractSequentialList;
import java.util.ListIterator;

import org.xmodel.IModelObject;

/**
 * An implementation of java.util.List that uses a JDBC ResultSet in "cursor mode" to efficiently 
 * visit each row in a large result set.  This class discards the previous element when a new element
 * is visited.  Note that it is the responsibility of the client to insure that the ResultSet is
 * configured for streaming/cursor use.
 */
public abstract class AbstractSQLCursorList extends AbstractSequentialList<IModelObject>
{
  protected AbstractSQLCursorList( ResultSet cursor) throws SQLException
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
  protected abstract IModelObject transform( ResultSet cursor) throws SQLException;

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
    public SQLCursorIterator( AbstractSQLCursorList list)
    {
      this.list = list;
      this.index = 0;
      
      try
      {
        if ( !list.cursor.isBeforeFirst())
        {
          list.cursor = ((PreparedStatement)list.cursor.getStatement()).executeQuery();
        }
      }
      catch( SQLException e)
      {
        throw new RuntimeException( "Iteration interrupted", e);
      }
      
      try
      {
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
        index++;
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
      return index;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#previous()
     */
    @Override
    public IModelObject previous()
    {
      return null;
    }

    /* (non-Javadoc)
     * @see java.util.ListIterator#previousIndex()
     */
    @Override
    public int previousIndex()
    {
      return -1;
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

    private AbstractSQLCursorList list;
    private boolean hasNext;
    private int index;
  }
}
