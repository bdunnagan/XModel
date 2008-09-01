/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.Collection;
import java.util.List;
import org.xmodel.*;
import org.xmodel.diff.IXmlMatcher;
import org.xmodel.diff.XmlDiffer;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.ExpressionException;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;


public class AddAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#configure(org.xmodel.ui.model.ViewModel)
   */
  public void configure( XActionDocument document)
  {
    super.configure( document);

    if ( document.getRoot().getNumberOfChildren() == 0)
    {
      sourceExpr = document.getExpression( document.getRoot());
      targetExpr = defaultTargetExpr;
    }
    else
    {
      // load IModelObjectFactory and IXmlMatcher classes
      IModelObject viewRoot = document.getRoot();
      factory = getFactory( viewRoot);
      matcher = getMatcher( viewRoot);
      
      // get source and target expressions
      sourceExpr = document.getExpression( "source", false);
      targetExpr = document.getExpression( "target", false);
      indexExpr = document.getExpression( "index", false);
      
      // get flags (with backwards compatible support)
      Collection<String> attributes = viewRoot.getAttributeNames();
      if ( attributes == null || attributes.size() == 0)
      {
        create = Xlate.childGet( viewRoot, "create", false);
        unique = Xlate.childGet( viewRoot, "unique", false);
        mode = Xlate.childGet( viewRoot, "mode", "copy");
      }
      else
      {
        create = Xlate.get( viewRoot, "create", false);
        unique = Xlate.get( viewRoot, "unique", false);
        mode = Xlate.get( viewRoot, "mode", "copy");
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  protected void doAction( IContext context)
  {
    // create target if requested
    if ( create) ModelAlgorithms.createPathSubtree( context, targetExpr, factory, null);
    
    // source
    List<IModelObject> sources = sourceExpr.query( context, null);
    if ( sources.size() == 0) return;
    
    // fail if target is null
    List<IModelObject> targets = targetExpr.query( context, null);
    if ( targets.size() == 0) return; 
    
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
      int start = (index < 0)? target.getNumberOfChildren(): index;
      for( IModelObject source: sources)
      {
        if ( mode.startsWith( "ref"))
        {
          if ( !unique || target.getChild( source.getType(), source.getID()) == null)
            target.addChild( new Reference( source), start);
        }
        else if ( mode.equals( "fk1"))
        {
          if ( !unique || target.getChild( source.getType(), source.getID()) == null)
          {
            IModelObject object = new ModelObject( source.getType());
            object.setValue( source.getID());
            target.addChild( object, start);
          }
        }
        else if ( mode.equals( "fk2"))
        {
          if ( !unique || target.getChild( source.getType(), source.getID()) == null)
          {
            IModelObject object = new ModelObject( source.getType());
            object.setID( source.getID());
            target.addChild( object, start);
          }
        }
        else if ( mode.equals( "copy"))
        {
          if ( unique)
          {
            IModelObject matching = target.getChild( source.getType(), source.getID());
            if (matching != null)
            {
            	XmlDiffer differ = new XmlDiffer();
            	if (matcher != null) differ.setMatcher( matcher);
            	differ.diffAndApply( matching, source);
            }
            else
            {
              IModelObject object = ModelAlgorithms.cloneTree( source, factory);
              target.addChild( object, start);        	  
            }
          }
          else
          {
            IModelObject object = ModelAlgorithms.cloneTree( source, factory);
            target.addChild( object, start);
          }
        }
        else if ( mode.equals( "move"))
        {
          if ( !unique || target.getChild( source.getType(), source.getID()) == null)
            target.addChild( source, start);
        }
        start++;
      }
    }
  }

  private final static IExpression defaultTargetExpr = XPath.createExpression( ".");
  
  private IModelObjectFactory factory;
  private IXmlMatcher matcher;
  private IExpression sourceExpr;
  private IExpression targetExpr;
  private IExpression indexExpr;
  private String mode;
  private boolean unique;
  private boolean create;
}
