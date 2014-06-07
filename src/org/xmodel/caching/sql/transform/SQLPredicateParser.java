package org.xmodel.caching.sql.transform;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLPredicateParser
{
  private enum Token { compareOp, keyword, negateOp, comma, literal, parenOpen, parenClose};
  
  public void parse( String text) throws ParseException
  {
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
          int k = text.indexOf( c, index+1);
          if ( k >= 0)
          {
            emitToken( Token.literal, text, index+1, k);
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
  
  private int match( Pattern pattern, String text, int index, Token token)
  {
    Matcher matcher = pattern.matcher( text.substring( index));
    if ( matcher.find())
    {
      emitToken( token, text, index + matcher.start(), index + matcher.end());
      return index + matcher.end();
    }
    return -1;
  }
  
  private void emitToken( Token token, String text, int begin, int end)
  {
    System.out.printf( "[%s] %s\n", token, text.substring( begin, end));
    
    switch( token)
    {
      case compareOp:
      case keyword:
      case negateOp:
      case comma:
      case literal:
      case parenOpen:
      case parenClose:
    }
  }

  private int index;
  
  private static Pattern literalToken = Pattern.compile( "\\A\\w++");
  private static Pattern keywordToken = Pattern.compile( "\\A(:?and|or|between|in)");

  public static void main( String[] args) throws Exception
  {
    SQLPredicateParser p = new SQLPredicateParser();
    p.parse( " ( created_on between 1253320958 and 146094860948) or id in ('a', 'b', 'c')");
  }
}
