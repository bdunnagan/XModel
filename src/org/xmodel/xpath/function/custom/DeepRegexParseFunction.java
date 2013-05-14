package org.xmodel.xpath.function.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.function.Function;

/**
 * Regex-based string parser capable of parsing a string into a model of arbitrary depth.
 * The function takes at least three arguments.  The first argument is the string to be parsed.
 * The second argument is the name of the element to be created for each match.  The third
 * argument is the regex.  If the regex has capturing groups then an element is created for
 * each group.  Otherwise, an element is created for the entire regex match.
 * Each group of two arguments following the first three arguments is another name/regex pair.
 * Each match from a preceding name/regex match is further parsed by the following name/regex
 * pair.
 */
public class DeepRegexParseFunction extends Function
{
  public final static String name = "deep-regex-parse";

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getName()
   */
  @Override
  public String getName()
  {
    return name;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.IExpression#getType()
   */
  @Override
  public ResultType getType()
  {
    return ResultType.NODES;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xpath.expression.Expression#evaluateNodes(org.xmodel.xpath.expression.IContext)
   */
  @Override
  public List<IModelObject> evaluateNodes( IContext context) throws ExpressionException
  {
    assertArgs( 3, Integer.MAX_VALUE);
    
    List<IExpression> args = getArguments();
    String string = args.get( 0).evaluateString( context);
    
    List<String> names = new ArrayList<String>();
    List<Pattern> patterns = new ArrayList<Pattern>();
    for( int i=1; i<args.size(); i+=2)
    {
      String name = getArgument( i).evaluateString( context);
      names.add( name);
      
      String regex = getArgument( i+1).evaluateString( context);
      patterns.add( Pattern.compile( regex));
    }
    
    return parse( string, names, patterns, 0);
  }
  
  private List<IModelObject> parse( String string, List<String> names, List<Pattern> patterns, int index)
  {
    String name = names.get( index);
    Pattern pattern = patterns.get( index);
    int last = names.size() - 1;
    
    List<IModelObject> list = new ArrayList<IModelObject>();
    Matcher matcher = pattern.matcher( string);
    while( matcher.find())
    {
      if ( matcher.groupCount() > 0)
      {
        for( int i=1; i<=matcher.groupCount(); i++)
        {
          String match = matcher.group( i);
          if ( match != null)
          {
            IModelObject node = new ModelObject( name);
            if ( index < last)
            {
              for( IModelObject child: parse( match, names, patterns, index+1))
                node.addChild( child);
            }
            else
            {
              node.setValue( match);
            }
            list.add( node);
          }
        }
      }
      else
      {
        IModelObject node = new ModelObject( name);
        if ( index < last)
        {
          for( IModelObject child: parse( matcher.group(), names, patterns, index+1))
            node.addChild( child);
        }
        else
        {
          node.setValue( matcher.group());
        }
        list.add( node);
      }
    }
    
    return list;
  }
}
