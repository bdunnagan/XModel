package org.xmodel.caching.sql.transform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;

/**
 * Generalized transform between SQL and IModelObject instances.  This class is designed to be reused
 * for multiple queries, and adapts the size of its internal row storage.
 */
public class SQLTransform
{
  public SQLTransform()
  {
    rowMinCount = Integer.MAX_VALUE;
  }

  /**
   * Transform all the rows from the specified ResultSet.
   * @param rowCursor The ResultSet positioned before the first row.
   * @return Returns the transform row elements.
   */
  public List<IModelObject> importRows( ISQLRowTransform transform, ResultSet rowCursor) throws SQLException
  {
    // estimate the number of rows as the median of the minimum and maximum row counts, and make sure it's not 0
    List<IModelObject> rows = new ArrayList<IModelObject>( (rowMaxCount - rowMinCount) / 2 + 1);
    
    int count = 0;
    while( rowCursor.next())
    {
      rows.add( transform.importRow( rowCursor));
      count++;
    }

    // gradually forget minimum and maximum counts
    rowMinCount += (count - rowMinCount) * 0.1f;
    rowMaxCount += (count - rowMaxCount) * 0.1f;
    
    // track minimum and maximum row counts
    if ( count < rowMinCount) rowMinCount = count;
    if ( count > rowMaxCount) rowMaxCount = count;

    return rows;
  }
  
  private int rowMinCount;
  private int rowMaxCount;
}
