package org.xmodel.caching.sql.transform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractSequentialList;
import java.util.ListIterator;

/**
 * An implementation of java.util.List that uses a JDBC ResultSet in "cursor mode" to efficiently 
 * visit each row in a large result set.  This class discards the previous element when a new element
 * is visited.
 */
public class SQLCursorList<E> extends AbstractSequentialList<E>
{
  public SQLCursorList( ResultSet cursor) throws SQLException
  {
    this.cursor = cursor;
    
    Statement statement = cursor.getStatement();
    if ( statement == null) throw new IllegalArgumentException( "ResultSet does not have an associated Statement.");
    
    statement.setFetchSize( 1);
  }
  
  /* (non-Javadoc)
   * @see java.util.AbstractSequentialList#listIterator(int)
   */
  @Override
  public ListIterator<E> listIterator( int index)
  {
    return null;
  }

  /* (non-Javadoc)
   * @see java.util.AbstractCollection#size()
   */
  @Override
  public int size()
  {
    return 0;
  }

  private ResultSet cursor;
}
