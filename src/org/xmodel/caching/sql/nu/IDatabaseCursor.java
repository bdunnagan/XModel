package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;

public interface IDatabaseCursor
{
  public void reset();
  
  public IModelObject next();
  
  
}
