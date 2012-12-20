package org.xmodel.caching.sql.transform;

import java.util.ArrayList;
import java.util.List;
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
    Matcher matcher = columnsRegex.matcher( sql);
    if ( !matcher.find()) return false;
    
    querySansPredicate = matcher.group( 1);
    
    List<String> names = new ArrayList<String>();
    int parens = 0;
    int start = 0;
    boolean word = false;
    String name = null;
    
    String columns = matcher.group( 1).trim();
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
  
  private String querySansPredicate;
  private List<String> columns;
  
  private static Pattern columnsRegex = Pattern.compile( "(?i)^\\s*+(select\\s++(.*)\\s++from\\s++.*)\\s+(WHERE.*);");
  
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
