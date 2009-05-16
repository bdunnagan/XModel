/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmodel.DepthFirstIterator;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.external.caching.AnnotationTransform;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.expression.IExpression.ResultType;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xsd.Schema;

/**
 * An XAction which creates an element using a variety of mechanisms.  The element can be
 * created by a template, by element name or from a schema.  The created element can be
 * assigned to a collection or a variable, or added to a parent element.  Templates can
 * include annotations that describe the caching policy to be used according to the rules
 * defined in the AnnotationTransform class.
 */
public class CreateAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.ViewModel)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    // get optional variable to assign
    IModelObject root = document.getRoot();
    variable = Xlate.get( root, "assign", (String)null);
    if ( variable == null) variable = Xlate.childGet( root, "assign", (String)null);
    
    // get optional collection to which new elements will be added
    collection = Xlate.get( document.getRoot(), "collection", (String)null);
    
    // get optional parent
    parentExpr = document.getExpression( "parent", true);
    
    // treat the value of root as an expression prototype
    if ( root.getNumberOfChildren() == 0)
      createExpr = document.getExpression( root);
    
    // get the factory used to create elements
    factory = getFactory( root);

    // create the script
    script = document.createScript( "parent", "name", "template", "attribute", "schema");
    
    // if annotated then preprocess template
    annotated = Xlate.get( root.getFirstChild( "template"), "annotated", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  public void doAction( IContext context)
  {
    XActionDocument document = getDocument();
    
    // handle the zero child form
    if ( createExpr != null)
    {
      ModelAlgorithms.createPathSubtree( context, createExpr, factory, null);
      return;
    }
    
    // get parent
    IModelObject parent = (parentExpr != null)? parentExpr.queryFirst( context): null;

    // create element
    List<IModelObject> elements = new ArrayList<IModelObject>( 1);
    
    // create element from name string
    IExpression nameExpr = document.getExpression( "name", true);
    if ( nameExpr != null)
    {
      String type = nameExpr.evaluateString( context);
      if ( type.length() == 0)
        throw new IllegalArgumentException(
          "Element type name is empty: "+this);
      
      elements.add( factory.createObject( parent, type));
    }

    // create element from schema
    IExpression schemaExpr = document.getExpression( "schema", false);
    if ( schemaExpr != null)
    {
      IModelObject schema = schemaExpr.queryFirst( context);
      boolean optional = Xlate.get( document.getRoot().getFirstChild( "schema"), "optional", false);
      elements.add( Schema.createDocument( schema, factory, optional));
    }
    
    // create element from template
    IModelObject template = document.getRoot().getFirstChild( "template");
    if ( template != null) 
    {
      // process template expressions
      for( IModelObject child: template.getChildren())
      {
        IModelObject element = ModelAlgorithms.cloneTree( child, factory);
        replaceTemplateExpressions( context, element);
        elements.add( element);
      }
    }
    
    // create attributes
    List<IModelObject> attributes = document.getRoot().getChildren( "attribute");
    for( IModelObject attribute: attributes)
    {
      String name = Xlate.get( attribute, "name", (String)null);
      IExpression valueExpr = document.getExpression( attribute);
      String value = valueExpr.evaluateString( context);
      for( IModelObject element: elements)
        element.setAttribute( name, value);
    }
    
    // process actions
    int count = elements.size();
    for( int i=0; i<count; i++)
    {
      StatefulContext actionContext = new StatefulContext( context, elements.get( i), i+1, count);
      script.run( actionContext);
    }
    
    // process annotations
    if ( annotated)
    {
      for( int i=0; i<elements.size(); i++)
      {
        IModelObject element = elements.get( i);
        AnnotationTransform transform = new AnnotationTransform();
        transform.setFactory( factory);
        transform.setParentContext( context);
        transform.setClassLoader( document.getClassLoader());
        element = transform.transform( element);
        elements.set( i, element);
      }
    }
    
    // set variable if defined
    IVariableScope scope = context.getScope();
    if ( variable != null)
    {
      if ( scope == null)
      {
        throw new IllegalArgumentException( 
          "Unable to assign variable: "+variable+" in: "+this);
      }
      
      scope.set( variable, elements);
    }
    
    // add to parent if not null
    if ( parent != null) 
      for( IModelObject element: elements)
        parent.addChild( element);
    
    // add element to collection if not null
    if ( collection != null && elements.size() > 0)
    {
      IModel model = elements.get( 0).getModel();
      for( IModelObject element: elements)
        model.addRoot( collection, element);
    }
  }
  
  /**
   * Replace XPath expressions embeddeded in XML template.
   * @param context The context of the action.
   * @param template The raw template model.
   */
  private void replaceTemplateExpressions( IContext context, IModelObject template)
  {
    DepthFirstIterator iter = new DepthFirstIterator( template);
    while( iter.hasNext())
    {
      IModelObject element = (IModelObject)iter.next();
      for( String attrName: element.getAttributeNames())
      {
        String rawText = Xlate.get( element, attrName, "");
        Object newText = replaceTemplateExpressions( context, rawText);
        element.setAttribute( attrName, newText);
      }
    }
  }
  
  /**
   * Replace XPath expressions embedded within the specified attribute value or element text.
   * @param context The context of the action.
   * @param input The attribute value or element text.
   * @return Returns the replacement string.
   */
  private Object replaceTemplateExpressions( IContext context, String input)
  {
    // return single object replacements taken from the value of a node-set
    Object single = null; 

    // parse
    StringBuilder result = new StringBuilder();
    Matcher matcher = expressionPattern.matcher( input);
    int index = 0;
    while( matcher.find())
    {
      int start = matcher.start();
      int end = matcher.end();

      // clear single object replacement since it does not appear alone
      if ( single != null)
      {
        single = null;
        result.append( single);
      }
      
      // check for non-escaped expression token
      if ( start > 0 && input.charAt( start-1) == '\\')
      {
        // append non-matching material between end of previous match and this match (skip escape character)
        result.append( input, index, start-1);
        
        // append escaped expression token material
        result.append( input, start, end);
      }
      else
      {
        // append non-matching material between end of previous match and this match
        result.append( input, index, start);
        
        // append replacement for expression
        try
        {
          // get single object replacement candidate
          Object replacement = getExpressionResult( context, matcher.group( 1));
          if ( result.length() == 0) single = replacement; else result.append( replacement);
        }
        catch( ExpressionException e)
        {
          getDocument().error( "Syntax error in template expression: "+matcher.group(), e);
        }
      }
      
      // update index
      index = end;
    }
    
    // append remaining non-matching material
    result.append( input, index, input.length());
    
    // return result
    return (single != null)? single: result.toString();
  }
  
  /**
   * Returns the expression result. If the expression returns an node-set, then the Object
   * value of the first node is returned.  Otherwise, the string result of the expression
   * is returned.
   * @param context The context.
   * @param spec The expression specification.
   * @return Returns the expression result.
   */
  private Object getExpressionResult( IContext context, String spec)
  {
    IExpression expression = XPath.createExpression( spec);
    if ( expression.getType( context) == ResultType.NODES)
    {
      IModelObject node = expression.queryFirst( context);
      return (node != null)? node.getValue(): null;
    }
    else
    {
      return expression.evaluateString( context);
    }
  }
  
  private final Pattern expressionPattern = Pattern.compile(
    "[{]([^}]+)[}]");
  
  private IModelObjectFactory factory;
  private String variable;
  private String collection;
  private IExpression createExpr;
  private IExpression parentExpr;
  private ScriptAction script;
  private boolean annotated;
}
