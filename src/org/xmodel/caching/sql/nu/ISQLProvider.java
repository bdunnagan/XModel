package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;

public interface ISQLProvider
{
  public void configure( IModelObject annotation);

  public ISQLCursor query( String sql);
  
  public void update( String sql, Object... params);
  
  public void delete( String sql, Object... params);
}
