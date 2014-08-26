package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;
import org.xmodel.external.ITransaction;

public interface ISQLProvider
{
  public void configure( IModelObject annotation);

  public ISQLCursor query( String sql);
  
  public void update( String sql, Object... params);
  
  public void delete( String sql, Object... params);
  
  public ITransaction transaction();
}
