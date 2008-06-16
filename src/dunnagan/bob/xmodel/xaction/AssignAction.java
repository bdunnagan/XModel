/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.IModelObjectFactory;
import dunnagan.bob.xmodel.ModelAlgorithms;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;
import dunnagan.bob.xmodel.xpath.variable.IVariableSource;

/**
 * An IXAction which sets a variable in the context scope. If the context does not have a scope
 * then the variable is not set and the action exists with false return code.
 */
public class AssignAction extends XAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.actions.XAction#configure(dunnagan.bob.xmodel.ui.model.XActionDocument)
   */
  @Override
  public void configure( XActionDocument document)
  {
    super.configure( document);
    
    name = Xlate.get( document.getRoot(), "name", (String)null);
    if ( name != null)
    {
      String text = Xlate.get( document.getRoot(), "");
      if ( text.length() > 0) sourceExpr = document.getExpression( document.getRoot());
    }
    else
    {
      name = document.getString( "name");
      sourceExpr = document.getExpression( "source", false);
    }
    
    // load IModelObjectFactory class
    IModelObject viewRoot = document.getRoot();
    factory = getFactory( viewRoot);
    
    clone = Xlate.get( document.getRoot(), "clone", false);
    replace = Xlate.get( document.getRoot(), "replace", false);
    define = Xlate.get( document.getRoot(), "define", false);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#run(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public void doRun( IContext context)
  {
    if ( name.length() == 0)
      throw new IllegalArgumentException(
        "Variable name has zero length in AssignAction: "+this);
    
    IVariableScope scope = findScope( name, context, replace);
    if ( scope == null) return;

    if ( sourceExpr == null)
    {
      scope.set( name, context.getObject());
    }
    else if ( define)
    {
      scope.define( name, sourceExpr);
    }
    else
    {
      switch( sourceExpr.getType( context))
      {
        case NODES:   
        {
          List<IModelObject> sources = sourceExpr.evaluateNodes( context);
          if ( clone)
          {
            List<IModelObject> clones = new ArrayList<IModelObject>( sources.size());
            for( IModelObject source: sources)
              clones.add( ModelAlgorithms.cloneTree( source, factory));
            scope.set( name, clones);
          }
          else
            scope.set( name, sources);
        }
        break;
        
        case STRING:  scope.set( name, sourceExpr.evaluateString( context)); break;
        case NUMBER:  scope.set( name, sourceExpr.evaluateNumber( context)); break;
        case BOOLEAN: scope.set( name, sourceExpr.evaluateBoolean( context)); break;
      }
    }
  }
  
  /**
   * Find the scope in which to define the variable. If the replace flag is false then the
   * scope of the context argument is returned. Otherwise, this method will find the nearest
   * enclosing scope in which the specified variable is already defined. If the variable is
   * not defined in an enclosing scope, then the scope of the specified context is returned.
   * @param variable The name of the variable to be set.
   * @param context The context.
   * @param replace The replace flag.
   * @return Returns the appropriate scope or null.
   */
  private IVariableScope findScope( String variable, IContext context, boolean replace)
  {
    if ( replace)
    {
      IVariableScope scope = context.getScope();
      if ( scope == null) return null;
      
      IVariableSource source = scope.getSource();
      return source.getVariableScope( variable);
    }
    
    return context.getScope();
  }

  private String name;
  private boolean clone;
  private boolean replace;
  private boolean define;
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
}
