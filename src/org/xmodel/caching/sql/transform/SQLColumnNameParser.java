package org.xmodel.caching.sql.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A partial SQL statement parser that can extract column names.
 */
public class SQLColumnNameParser
{
  /**
   * Parse the column names from the specified SQL statement.
   * @param sql The statement.
   * @return Returns null or the column names.
   */
  public List<String> parse( String sql)
  {
    Matcher matcher = columnsRegex.matcher( sql);
    if ( !matcher.find()) return null;
    
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
    
    return names;
  }
  
  private static Pattern columnsRegex = Pattern.compile( "^\\s*+select\\s++(.*)\\s++from");
  
  public static void main( String[] args) throws Exception
  {
    SQLColumnNameParser parser = new SQLColumnNameParser();
    List<String> names = parser.parse( "select count(*) count, first_name f, last_name l , format( from_ltime( created_on)) c   from user;");
    for( String name: names)
    {
      System.out.println( "|"+name+"|");
    }
  }
}
