package org.xmodel.script;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.xpath.expression.IExpression.ResultType;

/**
 * Program to generate the skeleton code for an IScript implementation.
 */
public class GenerateSkeleton
{
  public final static int value = 0x01;
  public final static int attribute = 0x02;
  public final static int element = 0x04;
  
  public GenerateSkeleton()
  {
    declarations = new ArrayList<Declaration>();
  }
  
  /**
   * Declare an argument in the specified node, with the specified name and default value.
   * @param argnum The index of the argument.
   * @param nodes The node where the argument is defined.
   * @param required True if required.
   * @param type The return type of the expression.
   * @param name The name of the argument.
   * @param dfault Null or the default value if the argument is not specified.
   */
  public void declare( int argnum, int nodes, boolean required, ResultType type, String name, String dfault)
  {
    Declaration declaration = new Declaration( nodes, required, type, name, dfault);
    if ( argnum < declarations.size())
      declarations.set( argnum, declaration);
    else
      declarations.add( argnum, declaration);
  }
  
  /**
   * Generate the code for the IScript implementation.
   * @param name The name of the script.
   * @return Returns the generated code.
   */
  public String generateCode( String name)
  {
    StringBuilder skeleton = new StringBuilder( GenerateSkeleton.skeleton);
    
    String pkg = getPackage( name);
    if ( pkg != null) 
    {
      skeleton.insert( 0, "package "+pkg+";\n\n");
      name = name.substring( pkg.length() + 1);
    }
        
    substitute( skeleton, "#SCRIPTNAME", name);
    substitute( skeleton, "#DECLARATIONS", generateDeclarations());
    substitute( skeleton, "#EVALUATIONS", generateEvaluations());
    substitute( skeleton, "#MEMBERS", generateMembers());
    return skeleton.toString();
  }
  
  /**
   * Returns the package from the fully-qualified class name.
   * @param name The fully-qualified class name.
   * @return Returns null or the package.
   */
  private String getPackage( String name)
  {
    int index = name.lastIndexOf( '.');
    if ( index == -1) return null;
    return name.substring( 0, index);
  }
  
  /**
   * Generate the declarations.
   * @return Returns the code for the declarations.
   */
  public String generateDeclarations()
  {
    String indent = "    ";
    StringBuilder sb = new StringBuilder();
    
    for( Declaration declaration: declarations)
    {
      sb.append( indent);
      sb.append( declaration.name);
      if ( declaration.type != ResultType.UNDEFINED)
        sb.append( "Expr = ");
      
      int node = declaration.node;
      while( node != 0)
      {
        if ( (node & attribute) != 0)
        {
          sb.append( "Xlate.get( element, \"");
          sb.append( declaration.name);
          sb.append( "\", ");
          node &= ~attribute;
        }
        else if ( (node & element) != 0)
        {
          sb.append( "Xlate.childGet( element, \"");
          sb.append( declaration.name);
          sb.append( "\", ");
          node &= ~element;
        }
        else if ( (node & value) != 0)
        {
          if ( declaration.type != ResultType.UNDEFINED)
          {
            sb.append( "Xlate.get( element, ");
          }
          else
          {
            sb.append( " = ");
            sb.append( "factory.compile( context, element.getChildren());\n");
          }
          node &= ~value;
        }

        if ( node != 0)
        {
          sb.append( "\n  ");
          sb.append( indent);
        }
        else if ( declaration.type != ResultType.UNDEFINED)
        {
          sb.append( "(IExpression)null);\n");
        }
      }
    }
    
    return sb.toString();
  }
  
  /**
   * Generate the evaluations
   * @return Returns the code for the evaluations.
   */
  public String generateEvaluations()
  {
    String indent = "    ";
    StringBuilder sb = new StringBuilder();
    
    for( Declaration declaration: declarations)
    {
      if ( declaration.required)
      {
        sb.append( indent);
        sb.append( "if ( "); sb.append( declaration.name); sb.append( "Expr == null) ");
        sb.append( "throw new CompileException( \""); sb.append( declaration.name); 
        sb.append( " is a required argument.\");\n");
      }
      
      sb.append( indent);
      switch( declaration.type)
      {
        case NODES:
          sb.append( "List<IModelObject> ");
          sb.append( declaration.name); sb.append( " = ");
          if ( !declaration.required) { sb.append( "("); sb.append( declaration.name); sb.append(  "Expr != null)? ");}
          sb.append( declaration.name);
          sb.append( "Expr.evaluateNodes( context)");
          if ( declaration.required) sb.append( ";\n"); else { sb.append(  ": ("); sb.append( declaration.dfault); sb.append( ");\n");}
          break;
          
        case STRING:
          sb.append( "String ");
          sb.append( declaration.name); sb.append( " = ");
          if ( !declaration.required) { sb.append( "("); sb.append( declaration.name); sb.append(  "Expr != null)? ");}
          sb.append( declaration.name);
          sb.append( "Expr.evaluateString( context)");
          if ( declaration.required) sb.append( ";\n"); else { sb.append(  ": ("); sb.append( declaration.dfault); sb.append( ");\n");}
          break;
          
        case NUMBER:
          sb.append( "double ");
          sb.append( declaration.name); sb.append( " = ");
          if ( !declaration.required) { sb.append( "("); sb.append( declaration.name); sb.append(  "Expr != null)? ");}
          sb.append( declaration.name);
          sb.append( "Expr.evaluateNumber( context)");
          if ( declaration.required) sb.append( ";\n"); else { sb.append(  ": ("); sb.append( declaration.dfault); sb.append( ");\n");}
          break;
          
        case BOOLEAN:
          sb.append( "boolean ");
          sb.append( declaration.name); sb.append( " = ");
          if ( !declaration.required) { sb.append( "("); sb.append( declaration.name); sb.append(  "Expr != null)? ");}
          sb.append( declaration.name);
          sb.append( "Expr.evaluateBoolean( context)");
          if ( declaration.required) sb.append( ";\n"); else { sb.append(  ": ("); sb.append( declaration.dfault); sb.append( ");\n");}
          break;
          
        case UNDEFINED:
          sb.append( declaration.name); sb.append( ".execute( context);\n");
          break;
      }
    }
    
    return sb.toString();
  }
  
  /**
   * Generate the members.
   * @return Returns the code for the members.
   */
  public String generateMembers()
  {
    String indent = "  ";
    StringBuilder sb = new StringBuilder();
    
    for( Declaration declaration: declarations)
    {
      sb.append( indent);
      if ( declaration.type != ResultType.UNDEFINED)
      {
        sb.append( "private IExpression "); sb.append( declaration.name); sb.append( "Expr;\n");
      }
      else
      {
        sb.append( "private IScript "); sb.append( declaration.name); sb.append( ";\n");
      }
    }    
    
    return sb.toString();
  }  
  
  /**
   * Substitute the specified code into the specified region.
   * @param skeleton The skeleton code.
   * @param region The region.
   * @param code The code.
   */
  private void substitute( StringBuilder skeleton, String region, String code)
  {
    int index = skeleton.indexOf( region);
    skeleton.replace( index, index + region.length(), code);
  }
  
  private final static class Declaration
  {
    public Declaration( int node, boolean required, ResultType type, String name, String dfault)
    {
      this.node = node;
      this.required = required;
      this.type = type;
      this.name = name;
      this.dfault = dfault;
    }
    
    public int node;
    public boolean required;
    public ResultType type;
    public String name;
    public String dfault;
  }

  private List<Declaration> declarations;
  
  private static String skeleton =
      "import org.xmodel.IModelObject;\n" + 
      "import org.xmodel.xpath.expression.IContext;\n" +
      "import org.xmodel.script.IScript;\n" +
      "import org.xmodel.script.CompileException;\n" +
      "import org.xmodel.script.ExecuteException;\n" +
      "\n" +
      "/**\n" +
      " * An implementation of IScript that ...\n" +
      " */\n" + 
      "public class #SCRIPTNAME implements IScript\n" + 
      "{\n" +
      "  /* (non-Javadoc)\n" + 
      "   * @see org.xmodel.script.IScript#compile(org.xmodel.xpath.expression.IContext, org.xmodel.script.IScriptFactory, org.xmodel.IModelObject)\n" + 
      "   */\n" + 
      "  @Override\n" + 
      "  public void compile( IContext context, IScriptFactory factory, IModelObject element) throws CompileException\n" + 
      "  {\n" +
      "#DECLARATIONS" + 
      "  }\n" + 
      "\n" +
      "  /* (non-Javadoc)\n" + 
      "   * @see org.xmodel.script.IScript#execute(org.xmodel.xpath.expression.IContext)\n" + 
      "   */\n" + 
      "  @Override\n" + 
      "  public Object execute( IContext context) throws ExecuteException\n" + 
      "  {\n" +
      "#EVALUATIONS" + 
      "    return null;\n" + 
      "  }\n" + 
      "\n" + 
      "#MEMBERS" +
      "}\n"; 

  public final static Pattern parser = Pattern.compile( "^(\\d++)\\s*([aev]++)\\s*([ro])\\s*([nsdbx])\\s++(\\w++)\\s*(.*)");
  
  public static void main( String[] args) throws Exception
  {
    GenerateSkeleton gen = new GenerateSkeleton();
    
    BufferedReader reader = new BufferedReader( new InputStreamReader( System.in));
    
    System.out.print( "Enter the name of the script: ");
    String sname = reader.readLine();
    
    System.out.println( "Enter declarations in order followed by an empty line: ");
    while( true)
    {
      System.out.print( "arg# [aev]++ [ro] [nsdbx] <name> <default>: ");
      String line = reader.readLine().trim();
      if ( line.length() == 0) break;
      
      
      Matcher matcher = parser.matcher( line);
      if ( !matcher.find())
      {
        System.out.println( "Parse error.");
      }
      else
      {
        int argnum = Integer.parseInt( matcher.group( 1));
        String aev = matcher.group( 2);
        boolean required = matcher.group( 3).startsWith( "r");
        String esnb = matcher.group( 4);
        String name = matcher.group( 5);
        String dfault = matcher.group( 6).trim();
        
        int nodes = 0;
        if ( aev.contains( "a")) nodes |= attribute; 
        if ( aev.contains( "e")) nodes |= element; 
        if ( aev.contains( "v")) nodes |= value; 
        
        ResultType type = ResultType.UNDEFINED;
        if ( esnb.equals( "n")) type = ResultType.NODES;
        else if ( esnb.equals( "s")) type = ResultType.STRING;
        else if ( esnb.equals( "d")) type = ResultType.NUMBER;
        else if ( esnb.equals( "b")) type = ResultType.BOOLEAN;
        else if ( nodes != value || required) 
        {
          System.out.println( "Only v may be specified with r and x.");
        }
        
        gen.declare( argnum, nodes, required, type, name, dfault);
      }
    }
    
    System.out.println( gen.generateCode( sname));
  }
}
