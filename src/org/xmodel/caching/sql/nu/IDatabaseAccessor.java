package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;

public interface IDatabaseAccessor
{
  public void configure( IModelObject annotation);

  public IDatabaseCursor query( StringBuilder sql);
}
