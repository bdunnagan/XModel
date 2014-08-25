package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;

public interface ISQLProvider
{
  public void configure( IModelObject annotation);

  public ISQLCursor query( String sql);
  
  public void update( String sql);
  
  public void delete( String sql);
}
