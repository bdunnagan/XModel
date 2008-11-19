/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xpath;

import java.io.StringReader;
import java.util.Hashtable;
import java.util.List;
import org.xmodel.*;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.PathExpression;
import org.xmodel.xpath.expression.RootExpression;
import org.xmodel.xpath.parser.generated.ParseException;
import org.xmodel.xpath.parser.generated.XPathParser;


/**
 * An implementation of IPath which supports the X-Path 1.0 specification with the
 * following exceptions:
 * <ul>
 * <li>Qualified names are never expanded with the namespace URI.
 * <li>The namespace-uri() function is not supported.
 * <li>The lang() function is not supported.
 * <li>Java number/string translation is used.
 * </ul>
 * <p>
 * This class is used to create both IPath instances and IExpression instances.
 */
public class XPath extends AbstractPath implements IAxis
{
  /**
   * Create and compile an XPath from the specified xpath expression.
   * @param expression The xpath expression.
   * @throws PathSyntaxException If the xpath expression is invalid.
   */
  public XPath( String expression) throws PathSyntaxException
  {
    compile( expression);
  }

  /**
   * Compile the given expression specification and evaluate against the null context.
   * @param spec The expression specification.
   * @return Returns the result node-set.
   */
  public static List<IModelObject> query( String spec)
  {
    IExpression expression = XPath.createExpression( spec);
    return expression.query( null);
  }
  
  /**
   * Compile the given expression specification and evaluate against the given context.
   * @param context The context.
   * @param spec The expression specification.
   * @return Returns the result node-set.
   */
  public static List<IModelObject> query( IContext context, String spec)
  {
    IExpression expression = XPath.createExpression( spec);
    return expression.query( context, null);
  }
  
  /**
   * Compile the given expression specification and evaluate against the null context.
   * @param spec The expression specification.
   * @return Returns the first node of the node-set.
   */
  public static IModelObject queryFirst( String spec)
  {
    IExpression expression = XPath.createExpression( spec);
    return expression.queryFirst();
  }
  
  /**
   * Compile the given expression specification and evaluate against the given context.
   * @param context The context.
   * @param spec The expression specification.
   * @return Returns the first node of the node-set.
   */
  public static IModelObject queryFirst( IContext context, String spec)
  {
    IExpression expression = XPath.createExpression( spec);
    return expression.queryFirst( context);
  }

  /**
   * Convenience method for creating XPath objects without having to handle the PathSyntaxException.
   * If the path has a syntax problem, this method merely returns null. This method caches xpath
   * expression for faster access.
   * @param pathString The xpath expression to compile.
   * @return Returns null or the XPath object.
   */
  public static IPath createPath( String pathString)
  {
    try
    {
      if ( pathCache == null) pathCache = new Hashtable<String, XPath>( 100);
      XPath path = pathCache.get( pathString);
      if ( path != null) return path;
        
      path = new XPath( pathString);
      pathCache.put( pathString, path);
      return path;
    }
    catch( PathSyntaxException e)
    {
      e.printStackTrace( System.err);
      return null;
    }
  }
  
  /**
   * Create an arbitrary expression using the X-Path 1.0 parser. This method caches xpath
   * expressions for faster access.
   * @param expressionString The x-path expression to compile.
   * @return Returns the root of the expression tree.
   */
  public static IExpression createExpression( String expressionString)
  {
    try
    {
      return compileExpression( expressionString);
    }
    catch( PathSyntaxException e)
    {
      e.printStackTrace( System.err);
      return null;
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.xmodel.IPath#compile(java.lang.String)
   */
  public void compile( String pathString) throws PathSyntaxException
  {
    XPathParser parser = new XPathParser( new StringReader( pathString));
    parser.setSpec( pathString);
    try
    {
      parser.ParsePath( this);
    }
    catch( ParseException e)
    {
      throw new PathSyntaxException( "Syntax error in path: "+pathString, e);
    }
  }
  
  /**
   * Compile the specified X-Path 1.0 expression.
   * @param expression The expression string to compile.
   */
  public static IExpression compileExpression( String expression) throws PathSyntaxException
  {
    try
    {
      XPathParser parser = new XPathParser( new StringReader( expression));
      parser.setSpec( expression);
      IExpression result = parser.ParseExpression();
      return result;
    }
    catch( ParseException e)
    {
      throw new PathSyntaxException( "Syntax error in expression: "+expression, e);
    }
  }
  
  /**
   * Convert the specified expression into an IPath instance. The expression must begin with
   * a location step and cannot have any clauses.  If the expression cannot be converted then
   * null is returned.
   * @param expression A simple path expression.
   * @return Returns null or the path.
   */
  public static IPath convertToPath( IExpression expression)
  {
    if ( expression instanceof RootExpression) expression = expression.getArgument( 0);
    if ( expression != null && expression instanceof PathExpression)
      return ((PathExpression)expression).getPath();
    return null;
  }
  
  /**
   * Convert the specified path into an expression.
   * @param path The path.
   * @return Returns the new expression.
   */
  public static IExpression convertToExpression( IPath path)
  {
    PathExpression expression = new PathExpression( path);
    return new RootExpression( expression);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#getRootExpression()
   */
  public RootExpression getRootExpression()
  {
    return null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object object)
  {
    return object.toString().equals( toString());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IPath#clone(org.xmodel.xpath.expression.RootExpression)
   */
  public IPath clone( RootExpression root)
  {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    int length = length();
    StringBuilder builder = new StringBuilder();
    for( int i=0; i<length; i++)
    {
      IPathElement element = getPathElement( i);
      builder.append( element.toString());
      if ( i < (length - 1)) builder.append( '/');
    }
    return builder.toString();
  }

  /**
   * Set the size of the path and expression caches.
   * @param size The size of the hashtable.
   */
  public static void setCacheSize( int size)
  {
    pathCache = new Hashtable<String, XPath>( size);
  }

  static Hashtable<String, XPath> pathCache;
  
  public static void main( String[] args) throws Exception
  {
    String xml =
      "<a id='5'>" +
      "  <b x='9'>1</b>" +
      "  <b>2" +
      "    <c>i</c>" +
      "    <c>ii</c>" +
      "    <c>iii</c>" +
      "  </b>" +
      "  <?pi?>" +
      "  <b>3</b>" +
      "  <b>4" +
      "    <c>i</c>" +
      "    <c>ii</c>" +
      "    <c>iii</c>" +
      "  </b>" +
      "  <b>5</b>" +
      "</a>";

    XmlIO xmlIO = new XmlIO();
    IModelObject a = xmlIO.read( xml);
    
    System.out.println( xmlIO.write( a));
    
    IPath path = XPath.createPath( "/a/b[ 2]/c[ 2]/following::*");
    for( IModelObject node: path.query( a, null))
      System.out.println( node);
    
    IExpression expr = XPath.createExpression( "/a/b/@x");
    IModelObject attribute = expr.queryFirst( a);
    path = ModelAlgorithms.createIdentityPath( attribute);
    System.out.println( "path="+path);
  }
}
