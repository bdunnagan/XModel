/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * AssignAction.java
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
    
    IModelObject config = document.getRoot();
    var = Conventions.getVarName( config, true, "name");
    
    sourceExpr = document.getExpression();
    if ( sourceExpr == null) sourceExpr = document.getExpression( "source", true);
    
    // load IModelObjectFactory class
    factory = getFactory( config);

    // flags
    mode = Xlate.get( config, "mode", "direct");
    append = Xlate.get( config, "append", false);
    replace = Xlate.get( config, "replace", false);
    define = Xlate.get( config, "define", false);
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.GuardedAction#doAction(org.xmodel.xpath.expression.IContext)
   */
  @Override
  protected Object[] doAction( IContext context)
  {
    if ( var.length() == 0)
      throw new IllegalArgumentException(
        "Variable name has zero length in AssignAction: "+this);
    
    IVariableScope scope = findScope( var, context, replace);
    if ( scope == null) return null;

    if ( sourceExpr == null)
    {
      scope.set( var, context.getObject());
    }
    else if ( define)
    {
      scope.define( var, sourceExpr);
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
            for( IModelObject source: sources) refs.add( new Reference( source));
            setVariable( scope, refs);
          }
          else if ( mode.equals( "copy"))
          {
            List<IModelObject> clones = new ArrayList<IModelObject>( sources.size());
            for( IModelObject source: sources) clones.add( ModelAlgorithms.cloneTree( source, factory));
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
        
        case STRING:  scope.set( var, sourceExpr.evaluateString( context)); break;
        case NUMBER:  scope.set( var, sourceExpr.evaluateNumber( context)); break;
        case BOOLEAN: scope.set( var, sourceExpr.evaluateBoolean( context)); break;
        case UNDEFINED: throw new XActionException( "Expression type is undefined: "+sourceExpr);
      }
    }
    
    return null;
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
      newList.addAll( (List<IModelObject>)scope.get( var));
      newList.addAll( list);
    }
    else
    {
      scope.set( var, list);
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

  private String var;
  private String mode;
  private boolean append;
  private boolean replace;
  private boolean define;
  private IModelObjectFactory factory;
  private IExpression sourceExpr;
}
