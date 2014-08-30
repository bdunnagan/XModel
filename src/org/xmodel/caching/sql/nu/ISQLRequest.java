package org.xmodel.caching.sql.nu;

import org.xmodel.IModelObject;

public interface ISQLRequest
{
  public IModelObject getSchema();
  
  public String getSQL();
  
  public int getParamType( int paramIndex);
  
  public Object getParamValue( int paramIndex);
  
  public int getParamCount();
  
  public int getLimit();
  
  public boolean isStreaming();
}
