package org.xmodel.caching.sql.transform;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelObjectFactory;

/**
 * Generalized transform between SQL and IModelObject instances.
 */
public class SQLTransform
{
  public SQLTransform()
  {
    columnTransforms = new ArrayList<ISQLColumnTransform>();
  }

  /**
   * Set the row element factory.
   * @param factory The new factory.
   */
  public void setFactory( IModelObjectFactory factory)
  {
    this.factory = factory;
  }
  
  /**
   * Transform the next row in the specified ResultSet into an IModelObject instance.
   * Only the columns for which there exists a column transform will be considered.
   * @param rowCursor The cursor pointing before the row to be transformed.
   * @param elementName The name of the row element.
   * @return Returns the new row element or null if the cursor is at the end.
   */
  public IModelObject transform( ResultSet rowCursor, String elementName) throws SQLException
  {
    if ( !rowCursor.next()) return null;

    if ( factory == null) factory = new ModelObjectFactory();
    IModelObject rowElement = factory.createObject( null, elementName);
    
    for( int i=0; i<columnTransforms.size(); i++)
    {
      ISQLColumnTransform transform = columnTransforms.get( i);
      transform.importColumn( rowCursor, rowElement, i);
    }
    
    return rowElement;
  }

  private IModelObjectFactory factory;
  private List<ISQLColumnTransform> columnTransforms;
}
