package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;

public interface ISQLCursor
{
  public void reset();
  
  public IModelObject next();
  
  public void dispose();
}
