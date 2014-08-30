package org.xmodel.caching.sql.nu;

import java.sql.SQLException;
import org.xmodel.IModelObject;

public interface ISQLProvider
{
  public void configure( IModelObject annotation);

  public ISQLCursor query( ISQLRequest request) throws SQLException;
  
  public void update( ISQLRequest request) throws SQLException;
}
