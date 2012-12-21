package org.xmodel.caching.sql;

public class SQLRowCachingPolicy extends SQLCachingPolicy
{
  public SQLRowCachingPolicy( SQLCachingPolicy parentPolicy)
  {
    this.parentPolicy = parentPolicy;
    
    this.provider = parentPolicy.provider;
    this.query = String.format( "%s WHERE %s = $?", parser.getQueryWithoutPredicate(), primaryKey);
    this.primaryKey = parentPolicy.primaryKey;
    this.parser = parentPolicy.parser;
    this.update = parentPolicy.update;
    this.transform = parentPolicy.transform;
    this.updateListener = parentPolicy.updateListener;
  }
  
  private SQLCachingPolicy parentPolicy;
}
