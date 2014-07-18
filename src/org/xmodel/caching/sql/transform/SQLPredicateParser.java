package org.xmodel.caching.sql.transform;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.log.Log;

public class SQLPredicateParser
{
  private enum Token { compareOp, keyword, negateOp, comma, literal, parenOpen, parenClose};
  private enum State { predicate, between1, between2, in, list};
  
  public String getParameterizedPredicate()
  {
    return parameterized.toString();
  }
  
  public List<String> getParameters()
  {
    return parameters;
  }
  
  public void parse( String text) throws SQLException
  {
    index = 0;
    rhs = false;
    parameterized = new StringBuilder();
    parameters = new ArrayList<String>();
    stack = new ArrayDeque<State>();
    stack.push( State.predicate);
    
    while( index < text.length())
    {
      char c = text.charAt( index);
      switch( c)
      {
        case ' ':
        case ';':
          index++;
          break;
          
        case '<':
        case '>':
          if ( text.charAt( index+1) == '=')
          {
            emitToken( Token.compareOp, text, index, index+2);
            index += 2;
          }
          else 
          {
            emitToken( Token.compareOp, text, index, index+1);
            index++;
          }
          break;
          
        case '=':
          emitToken( Token.compareOp, text, index, index+1);
          index++;
          break;
          
        case '!':
          if ( text.charAt( index+1) == '=')
          {
            emitToken( Token.compareOp, text, index, index+2);
            index += 2;
          }
          else
          {
            emitToken( Token.negateOp, text, index, index+1);
            index++;
          }
          break;
          
        case ',':
          emitToken( Token.comma, text, index, index+1);
          index++;
          break;
          
        case '\'':
        case '"':
          int k = text.indexOf( c, index+1);
          while ( k > 0 && text.charAt( k-1) == '\\')
            k = text.indexOf( c, k+1);
            
          if ( k > 0 && text.charAt( k-1) != '\\')
          {
            emitToken( Token.literal, text, index, k+1);
            index = k+1;
          }
          break;
          
        case '(':
          emitToken( Token.parenOpen, text, index, index+1);
          index++;
          break;
          
        case ')':
          emitToken( Token.parenClose, text, index, index+1);
          index++;
          break;

        default:
          int end = match( keywordToken, text, index, Token.keyword);
          if ( end < 0)
          {
            end = match( literalToken, text, index, Token.literal);
            if ( end < 0) return;
          }
          
          index = end;
          break;
      }
    }
  }
  
  private int match( Pattern pattern, String text, int index, Token token) throws SQLException
  {
    Matcher matcher = pattern.matcher( text.substring( index));
    if ( matcher.find())
    {
      emitToken( token, text, index + matcher.start(), index + matcher.end());
      return index + matcher.end();
    }
    return -1;
  }
  
  private void emitToken( Token token, String text, int begin, int end) throws SQLException
  {
    log.debugf( "[%s] %s\n", token, text.substring( begin, end));
    
    if ( !rhs || !token.equals( Token.literal))
    {
      parameterized.append( text.substring( begin, end));
      parameterized.append( ' ');
    }
    
    //
    // Here we just need to keep track of which literal tokens are on the left-hand 
    // vs. the right-hand side of operands.  Right-hand side literal tokens must be
    // replaced.
    //
    switch( stack.peek())
    {
      case predicate:  predicateState( token, text, begin, end); break;
      case between1:   
      case between2:   betweenState( token, text, begin, end); break;
      case in:         inState( token, text, begin, end); break;
      case list:       listState( token, text, begin, end); break;
    }
  }
  
  private void predicateState( Token token, String text, int begin, int end) throws SQLException
  {
    switch( token)
    {
      case compareOp:   
        rhs = true;
        break;
        
      case keyword:     
        if ( text.regionMatches( begin, "in", 0, 2)) 
        {
          rhs = true;
          stack.push( State.in);
        }
        else if ( text.regionMatches( begin, "between", 0, 7)) 
        {
          rhs = true;
          stack.push( State.between1);
        }
        else
        {
          rhs = false;
        }
        break;
        
      case negateOp:
      case comma:
        break;
        
      case parenOpen:
        stack.push( State.predicate);
        break;
        
      case parenClose:
        stack.pop();
        break;
        
      case literal:
        if ( rhs) 
        {
          parameterized.append( "? ");
          parameters.add( text.substring( begin, end));
        }
        break;
    }
  }
  
  private void betweenState( Token token, String text, int begin, int end) throws SQLException
  {
    switch( token)
    {
      case compareOp:
      case negateOp:
      case comma:
        throw new SQLException( String.format( "Parse error at %d in %s", begin, text));
        
      case keyword:     
        if ( text.regionMatches( begin, "and", 0, 3))
        {
          stack.pop();
          stack.push( State.between2);
        }
        else
        {
          throw new SQLException( String.format( "Parse error at %d in %s", begin, text));
        }
        break;
        
      case parenOpen:
        stack.push( State.predicate);
        break;
        
      case parenClose:
        stack.pop();
        break;
        
      case literal:
        if ( rhs) 
        {
          parameterized.append( "? ");
          parameters.add( text.substring( begin, end));
          if ( stack.peek().equals( State.between2)) stack.pop();
        }
        break;
    }
  }

  private void inState( Token token, String text, int begin, int end) throws SQLException
  {
    switch( token)
    {
      case compareOp:
      case negateOp:
      case literal:
      case comma:
      case keyword:     
      case parenClose:
        throw new SQLException( String.format( "Parse error at %d in %s", begin, text));
        
      case parenOpen:
        stack.pop();
        stack.push( State.list);
        break;
    }
  }

  private void listState( Token token, String text, int begin, int end) throws SQLException
  {
    switch( token)
    {
      case compareOp:
      case negateOp:
        throw new SQLException( String.format( "Parse error at %d in %s", begin, text));
        
      case comma:
      case keyword:     
        break;
        
      case parenOpen:
        stack.push( State.list);
        break;
        
      case parenClose:
        stack.pop();
        break;
        
      case literal:
        if ( rhs) 
        {
          parameterized.append( "? ");
          parameters.add( text.substring( begin, end));
        }
        break;
    }
  }
  
  private int index;
  private boolean rhs;
  private Deque<State> stack;
  private StringBuilder parameterized;
  private List<String> parameters;
  
  private static Pattern literalToken = Pattern.compile( "\\A\\w++");
  private static Pattern keywordToken = Pattern.compile( "(?i)\\A(:?and|or|between|in)");
  
  public final static Log log = Log.getLog( SQLPredicateParser.class);

  public static void main( String[] args) throws Exception
  {
    SQLPredicateParser p = new SQLPredicateParser();
    p.parse( "user = 'bob'");
    
    System.out.println( p.getParameterizedPredicate());
    
    for( String param: p.getParameters())
    {
      System.out.println( param);
    }
  }
}
