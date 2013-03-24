package org.xmodel.caching.sql.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A partial SQL statement parser that can extract column names.
 */
public class SimpleSQLParser
{
  public SimpleSQLParser( String sql)
  {
    parse( sql);
  }
  
  /**
   * @return Returns the query without its predicate.
   */
  public String getQueryWithoutPredicate()
  {
    return querySansPredicate;
  }
  
  /**
   * @return Returns the names of the columns.
   */
  public List<String> getColumnNames()
  {
    return columns;
  }
  
  /**
   * Parse the column names from the specified SQL statement.
   * @param sql The statement.
   * @return Returns true if the parse was successful.
   */
  private boolean parse( String sql)
  {
    // section query
    Map<String, String> sections = section( sql);
    
    // validate query
    if ( !sections.containsKey( "SELECT")) return false;
    if ( !sections.containsKey( "FROM")) return false;
    
    querySansPredicate = String.format( "SELECT %s FROM %s",
        sections.get( "SELECT"),
        sections.get( "FROM"));
    
    List<String> names = new ArrayList<String>();
    int parens = 0;
    int start = 0;
    boolean word = false;
    String name = null;
    
    String columns = sections.get( "SELECT");
    for( int i=0; i<columns.length(); i++)
    {
      char c = columns.charAt( i);
      switch( c)
      {
        case '(': parens++; break;
        case ')': parens--; break;
        case ' ': 
          if ( parens == 0)
          {
            if ( word)
            {
              String substring = columns.substring( start, i);
              if ( substring.length() > 0) name = substring;
              word = false;
            }
            start = i+1;
          }
          break;
          
        case ',':
          if ( parens == 0)
          {
            if ( word)
            {
              String substring = columns.substring( start, i);
              if ( substring.length() > 0) name = substring;
              word = false;
            }
            names.add( name);
            start = i+1;
          }
          break;
          
        default:
          if ( parens == 0)
          {
            if ( !word) start = i;
            word = true;
          }
          break;
      }
    }
    
    if ( word)
    {
      String substring = columns.substring( start);
      if ( substring.length() > 0) name = substring;
      names.add( name);
    }
    
    this.columns = names;
    return true;
  }
  
  /**
   * Section the query and return the sections.
   * @param sql The query.
   * @return Returns the section map.
   */
  private Map<String, String> section( String sql)
  {
    Map<String, String> sections = new HashMap<String, String>();
    
    String keyword = null;
    int start = 0;
    
    Matcher matcher = keywordRegex.matcher( sql);
    while( matcher.find())
    {
      if ( keyword != null) sections.put( keyword, sql.substring( start, matcher.start()).trim());
      start = matcher.end();
      keyword = matcher.group().toUpperCase();
    }
    
    if ( keyword != null) sections.put( keyword, sql.substring( start).trim());
    
    return sections;
  }
  
  private String querySansPredicate;
  private List<String> columns;
  
  private static Pattern keywordRegex = Pattern.compile( "(?i)select|from|where|order by|group by");
  
  public static void main( String[] args) throws Exception
  {
    SimpleSQLParser parser = new SimpleSQLParser( "SELECT " +
    		"count(*) count, first_name f, last_name l , format( from_ltime( created_on)) c   " +
    		"FROM user u JOIN monitor m on m.user_id = u.id " +
    		" WHERE id in (SELECT id FROM user);");
    
    System.out.println( parser.getQueryWithoutPredicate());
    
    for( String name: parser.getColumnNames())
    {
      System.out.printf( "%s\n", name);
    }
  }
}
