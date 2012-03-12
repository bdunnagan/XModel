/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * CreateAction.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.DepthFirstIterator;
import org.xmodel.IModel;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Xlate;
import org.xmodel.caching.AnnotationTransform;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;

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
    
    IModelObject config = document.getRoot();
    var = Conventions.getVarName( config, false, "assign");
    
    collectionExpr = Xlate.get( config, "collection", (IExpression)null);
    parentExpr = Xlate.get( config, "parent", (IExpression)null);
    nameExpr = Xlate.get( config, "name", (IExpression)null);
    
    // get the factory used to create elements
    factory = getFactory( config);

    // if annotated then preprocess template
    annotatedExpr = Xlate.get( config, "annotated", (IExpression)null);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  public Object[] doAction( IContext context)
  {
    XActionDocument document = getDocument();

    // get parent
    IModelObject parent = (parentExpr != null)? parentExpr.queryFirst( context): null;

    // create element
    List<IModelObject> elements = new ArrayList<IModelObject>( 1);
    
    // create element from name string
    if ( nameExpr != null)
    {
      String type = nameExpr.evaluateString( context);
      if ( type.length() == 0) throw new IllegalArgumentException( "Element type name is empty: "+this);
      elements.add( factory.createObject( parent, type));
    }
        
    // create children
    for( IModelObject child: document.getRoot().getChildren())
    {
      IModelObject element = ModelAlgorithms.cloneTree( child, factory);
      replaceTemplateExpressions( context, element);
      elements.add( element);
    }
    
    // process annotations
    if ( annotatedExpr == null || annotatedExpr.evaluateBoolean( context))
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
    if ( var != null) 
    {
      IVariableScope scope = context.getScope();
      if ( scope != null) scope.set( var, elements);
    }
    
    // add to parent if not null
    if ( parent != null) 
      for( IModelObject element: elements)
        parent.addChild( element);
    
    // add element to collection if not null
    String collection = (collectionExpr != null)? collectionExpr.evaluateString( context): null;
    if ( collection != null && elements.size() > 0)
    {
      IModel model = elements.get( 0).getModel();
      for( IModelObject element: elements)
        model.addRoot( collection, element);
    }
    
    return null;
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
      String[] attrNames = element.getAttributeNames().toArray( new String[ 0]);
      for( String attrName: attrNames)
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
    if ( input.length() == 0) 
    {
      return input;
    }
    else if ( input.charAt( 0) == '{')
    {
      if ( input.charAt( input.length() - 1) != '}') return input;
      return getExpressionResult( context, input.substring( 1, input.length() - 1));
    }
    else if ( input.charAt( 0) == '\\')
    {
      if ( input.length() > 1 && input.charAt( 1) == '\\') return input;
      return input.substring( 1);
    }
    
    return input;
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
    switch( expression.getType( context))
    {
      case NODES:
        IModelObject node = expression.queryFirst( context);
        return (node != null)? node.getValue(): null;
        
      case NUMBER:
        return expression.evaluateNumber( context);
        
      case STRING:
        return expression.evaluateString( context);
        
      case BOOLEAN:
        return expression.evaluateBoolean( context);
    }
    
    return null;
  }
  
  private IModelObjectFactory factory;
  private String var;
  private IExpression collectionExpr;
  private IExpression parentExpr;
  private IExpression nameExpr;
  private IExpression annotatedExpr;
}
