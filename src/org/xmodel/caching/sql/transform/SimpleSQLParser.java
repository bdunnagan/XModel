package org.xmodel.caching.sql.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.log.SLog;

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
    sections = section( sql);
    
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
  private static Map<String, String> section( String sql)
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

  /**
   * Returns the SQL statement with all predicate conditions parameterized.  The parameter values
   * that were extracted are returned in array elements starting with 1.
   * @return Returns null or the SQL statement with all predicate conditions parameterized.
   */
  public static Object[] parameterizePredicate( String predicate)
  {
    List<Object> result = new ArrayList<Object>( 5);
    StringBuilder newPredicate = new StringBuilder();
    int start = 0;
    
    Matcher conditionMatcher = conditionRegex.matcher( predicate);
    while( start >= 0)
    {
      boolean found = conditionMatcher.find();      
      int end = found? conditionMatcher.start(): predicate.length();
      
      if ( found && conditionMatcher.group().equalsIgnoreCase( "between"))
      {
        return null;
      }
      else
      {
        Matcher opMatcher = opRegex.matcher( predicate);
        if ( !opMatcher.region( start, end).find())
        {
          SLog.warnf( SimpleSQLParser.class, "Unable to parse predicate, '%s'", predicate);
          return null;
        }
        
        if ( opMatcher.group().toLowerCase().contains( "is"))
        {
          newPredicate.append( predicate.substring( start, (found)? end: predicate.length()));
        }
        else if ( predicate.charAt( opMatcher.end()) == '(')
        {
          int index = predicate.indexOf( ')', opMatcher.end() + 1);
          if ( index == -1) 
          {
            SLog.warnf( SimpleSQLParser.class, "Unable to parse predicate, '%s'", predicate);
            return null;
          }
          
          newPredicate.append( predicate.substring( start, opMatcher.end()));
          newPredicate.append( '(');
          
          String[] values = predicate.substring( consumeWhitespace( predicate, opMatcher.end() + 1), index).split( "\\s*,\\s*");
          for( int i=0; i<values.length; i++)
          {
            String paramValue = parseQuoted( values[ i], 0);
            if ( paramValue == null) paramValue = values[ i];
            result.add( paramValue);
            newPredicate.append( (i == 0)? "?": ",?");
          }
          
          newPredicate.append( ") ");
        }
        else
        {
          String paramValue = parseQuoted( predicate, opMatcher.end());
          if ( paramValue == null) paramValue = predicate.substring( opMatcher.end(), end);
          result.add( paramValue);
          
          newPredicate.append( predicate.substring( start, opMatcher.end()));
          newPredicate.append( "? ");
        }
        
        if ( found)
        {
          newPredicate.append( conditionMatcher.group());
          newPredicate.append( ' ');
        }
        
        start = found? conditionMatcher.end(): -1;
      }
    }
    
    result.add( 0, newPredicate);
    return result.toArray();
  }

  private static Object[] parseList( String text, int offset)
  {
    char paren = text.charAt( offset++);
    if ( paren == '(')
    {
      int index = text.indexOf( ')', offset);
      if ( index == -1) return null;
      
      return text.substring( consumeWhitespace( text, offset), index).split( "\\s*,\\s*");
    }
    else
    {
      return null;
    }
  }
  
  private static String parseQuoted( String text, int offset)
  {
    char quote = text.charAt( offset++);
    if ( quote == '\"' || quote == '\'')
    {
      int index = text.indexOf( quote, offset);
      return (index >= 0)? text.substring( offset, index): null;
    }
    else
    {
      return null;
    }
  }
  
  private static int consumeWhitespace( String text, int offset)
  {
    while( offset < text.length())
    {
      if ( text.charAt( offset) != ' ') break;
      offset++;
    }
    return offset;
  }
    
  private String querySansPredicate;
  private List<String> columns;
  private Map<String, String> sections;
  
  private static Pattern keywordRegex = Pattern.compile( "(?i)select|from|where|order by|group by");
  private static Pattern conditionRegex = Pattern.compile( "(?i)\\s++(and|or|between)\\s++");
  private static Pattern opRegex = Pattern.compile( "(?i)\\s*([=<>]|[!][=]|(?<=[ ])in|(?<=[ ])is not null|(?<=[ ])is null)\\s*+");
  
  public static void main( String[] args) throws Exception
  {
    SimpleSQLParser parser = new SimpleSQLParser(
    		"SELECT count(*) count, first_name f, last_name l , format( from_ltime( created_on)) c " +
    		"FROM user u JOIN monitor m on m.user_id = u.id " +
    		"WHERE id = 1093984938576 and user_id in ( '1', '1');");
    
    Object[] objects = SimpleSQLParser.parameterizePredicate( "WHERE login_on IS NOT NULL AND x in ('1','2','3','4','5')");
    for( Object object: objects)
      System.out.println( object);
    
//    for( String name: parser.getColumnNames())
//    {
//      System.out.printf( "%s\n", name);
//    }
  }
}
