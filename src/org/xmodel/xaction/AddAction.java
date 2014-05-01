/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AddAction.java
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Reference;
import org.xmodel.Xlate;
import org.xmodel.diff.IXmlMatcher;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


public class AddAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    // load IModelObjectFactory and IXmlMatcher classes
    IModelObject viewRoot = document.getRoot();
    factory = Conventions.getFactory( viewRoot);
    matcher = Conventions.getMatcher( document, viewRoot);
    
    // get source and target expressions
    sourceExpr = document.getExpression( "source", true);
    targetExpr = document.getExpression( "target", true);
    indexExpr = document.getExpression( "index", true);
    
    // get filter
    filterExpr = document.getExpression( "filter", true);
    
    // alternate form with either source or target expression defined in value
    if ( sourceExpr == null) sourceExpr = document.getExpression();
    if ( targetExpr == null) targetExpr = document.getExpression();
    
    // get flags
    create = Xlate.get( viewRoot, "create", Xlate.childGet( viewRoot, "create", false));
    unique = Xlate.get( viewRoot, "unique", Xlate.childGet( viewRoot, "unique", false));
    mode = Xlate.get( viewRoot, "mode", Xlate.childGet( viewRoot, "mode", "copy"));
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected Object[] doAction( IContext context)
  {
    // create target if requested
    if ( create) ModelAlgorithms.createPathSubtree( context, targetExpr, factory, null, null, true);
    
    // source
    List<IModelObject> sources = sourceExpr.query( context, null);
    if ( sources.size() == 0) return null;
    
    // fail if target is null
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) return null; 
    
    // index
    int index = -1;
    try
    {
      if ( indexExpr != null) index = (int)indexExpr.evaluateNumber( context);
    }
    catch( ExpressionException e)
    {
      getDocument().error( "Unable to evaluate index expression in AddAction: "+toString(), e);
    }

    // perform add
    for( IModelObject target: targets)
    {
      for( IModelObject source: sources)
      {
        if ( mode.startsWith( "ref"))
        {
          if ( !unique || target.getChild( source.getType(), source.getAttribute( "id")) == null)
            target.addChild( new Reference( source), index);
        }
        else if ( mode.equals( "fk1"))
        {
          if ( !unique || target.getChild( source.getType(), source.getAttribute( "id")) == null)
          {
            IModelObject object = factory.createObject( target, source.getType());
            object.setValue( source.getAttribute( "id"));
            target.addChild( object, index);
          }
        }
        else if ( mode.equals( "fk2"))
        {
          if ( !unique || target.getChild( source.getType(), source.getAttribute( "id")) == null)
          {
            IModelObject object = factory.createObject( target, source.getType());
            object.setAttribute( "id", source.getAttribute( "id"));
            target.addChild( object, index);
          }
        }
        else if ( mode.equals( "copy"))
        {
          Set<IModelObject> exclude = null;
          if ( filterExpr != null)
          {
            StatefulContext filterContext = new StatefulContext( context, source);
            exclude = new HashSet<IModelObject>( filterExpr.evaluateNodes( filterContext));
          }
          
          if ( unique)
          {
            IModelObject matching = target.getChild( source.getType(), source.getAttribute( "id"));
            if ( matching != null)
            {
            	XmlDiffer differ = new XmlDiffer();
            	if (matcher != null) differ.setMatcher( matcher);
            	differ.diffAndApply( matching, source);
            }
            else
            {
              IModelObject object = ModelAlgorithms.cloneTree( source, factory, exclude);
              target.addChild( object, index);        	  
            }
          }
          else
          {
            IModelObject object = ModelAlgorithms.cloneTree( source, factory, exclude);
            target.addChild( object, index);
          }
        }
        else if ( mode.equals( "move"))
        {
          if ( !unique || target.getChild( source.getType(), source.getAttribute( "id")) == null)
            target.addChild( source, index);
        }
        
        if ( index >= 0) index++;
      }
    }
    
    return null;
  }

  private IModelObjectFactory factory;
  private IXmlMatcher matcher;
  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression indexExpr;
  private IExpression filterExpr;
  private String mode;
  private boolean unique;
  private boolean create;
}
