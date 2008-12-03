/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.Reference;
import org.xmodel.Xlate;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.variable.IVariableScope;
import org.xmodel.xpath.variable.IVariableSource;


/**
 * An IXAction which sets a variable in the context scope. If the context does not have a scope
 * then the variable is not set and the action exists with false return code.
 */
public class AssignAction extends GuardedAction
{
  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.actions.XAction#configure(org.xmodel.ui.model.XActionDocument)
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

    // flags
    mode = Xlate.get( document.getRoot(), "mode", "direct");
    append = Xlate.get( document.getRoot(), "append", false);
    replace = Xlate.get( document.getRoot(), "replace", false);
    define = Xlate.get( document.getRoot(), "define", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected void doAction( IContext context)
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
          if ( mode.equals( "direct"))
          {
            setVariable( scope, sources);
          }
          else if ( mode.startsWith( "ref"))
          {
            List<IModelObject> refs = new ArrayList<IModelObject>( sources.size());
            for( IModelObject source: sources)
              refs.add( new Reference( source));
            setVariable( scope, refs);
          }
          else if ( mode.equals( "copy"))
          {
            List<IModelObject> clones = new ArrayList<IModelObject>( sources.size());
            for( IModelObject source: sources)
              clones.add( ModelAlgorithms.cloneTree( source, factory));
            setVariable( scope, clones);
          }
          else if ( mode.equals( "fk1"))
          {
            List<IModelObject> fks = new ArrayList<IModelObject>( sources.size());
            for( IModelObject source: sources)
            {
              IModelObject fk = factory.createObject( null, source.getType());
              fk.setValue( source.getID());
              fks.add( fk);
            }
            setVariable( scope, fks);
          }
          else if ( mode.equals( "fk2"))
          {
            List<IModelObject> fks = new ArrayList<IModelObject>( sources.size());
            for( IModelObject source: sources)
            {
              IModelObject fk = factory.createObject( null, source.getType());
              fk.setID( source.getID());
              fks.add( fk);
            }
            setVariable( scope, fks);
          }
        }
        break;
        
        case STRING:  scope.set( name, sourceExpr.evaluateString( context)); break;
        case NUMBER:  scope.set( name, sourceExpr.evaluateNumber( context)); break;
        case BOOLEAN: scope.set( name, sourceExpr.evaluateBoolean( context)); break;
        case UNDEFINED: throw new XActionException( "Expression type is undefined: "+sourceExpr);
      }
    }
  }
  
  /**
   * Replace or append variable node-set depending on the <i>append</i> flag.
   * @param scope The variable scope.
   * @param list The new elements.
   */
  @SuppressWarnings("unchecked")
  private void setVariable( IVariableScope scope, List<IModelObject> list)
  {
    if ( append)
    {
      List<IModelObject> newList = new ArrayList<IModelObject>();
      newList.addAll( (List<IModelObject>)scope.get( name));
      newList.addAll( list);
    }
    else
    {
      scope.set( name, list);
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
  private String mode;
  private boolean append;
  private boolean replace;
  private boolean define;
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
}
