/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.xaction;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.diff.DefaultXmlMatcher;
import dunnagan.bob.xmodel.diff.IXmlMatcher;
import dunnagan.bob.xmodel.xml.XmlIO;
import dunnagan.bob.xmodel.xml.IXmlIO.Style;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IContext;
import dunnagan.bob.xmodel.xpath.expression.IExpression;
import dunnagan.bob.xmodel.xpath.expression.StatefulContext;
import dunnagan.bob.xmodel.xpath.variable.IVariableScope;

/**
 * An abstract base implementation of IXAction which knows where to find the ClassLoader 
 * that is automatically placed in each form viewmodel. The <code>setClassLoader</code>
 * should be called on the IXForm instance to change the default ClassLoader which is
 * the eclipse class loader container the form framework.
 * <p>
 * This class works in conjunction with BreakAction to perform nested stepping.
 */
public abstract class XAction implements IXAction
{
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.IXAction#configure(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.IXAction#run()
   */
  public IVariableScope run()
  {
    StatefulContext context = new StatefulContext( new ModelObject( "root"));
    run( context);
    return context.getScope();
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.IXAction#run(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public final void run( IContext context)
  {
    BreakAction breakAction = BreakAction.getThreadBreakAction();
    if ( breakAction != null)
    {
      IModelObject root = getDocument().getRoot();
      if ( breakAction.startAction( this)) breakAction.prompt( context, root);
      doRun( context);
      breakAction.endAction( this);
    }
    else
    {
      doRun( context);
    }
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.IXAction#run(dunnagan.bob.xmodel.xpath.expression.IContext)
   */
  public abstract void doRun( IContext context);
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.IXAction#shouldReturn()
   */
  public boolean shouldReturn()
  {
    return false;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.xaction.IXAction#setDocument(dunnagan.bob.xmodel.xaction.XActionDocument)
   */
  public void setDocument( XActionDocument document)
  {
    this.document = document;
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.ui.swt.form.IXAction#getViewModel()
   */
  public XActionDocument getDocument()
  {
    if ( document == null) document = new XActionDocument();
    return document;
  }
  
  /**
   * Returns the factory defined for the specified locus. The first ancestor which defines a
   * factory determines which factory is created and returned.
   * @param locus The locus.
   * @return Returns the factory.
   */
  @SuppressWarnings("unchecked")
  protected IModelObjectFactory getFactory( IModelObject locus)
  {
    IModelObject factoryElement = factoryExpr.queryFirst( locus);
    if ( factoryElement == null) return new ModelObjectFactory();
    
    String className = Xlate.get( factoryElement, (String)null);
    if ( className == null) 
      getDocument().error(
        "Class name is undefined in factory element: "+
          ModelAlgorithms.createIdentityPath( factoryElement));
    
    ClassLoader loader = null;
    IModelObject loaderElement = loaderExpr.queryFirst( locus);
    if ( loaderElement != null) loader = (ClassLoader)loaderElement.getValue();
    if ( loader == null) loader = getClass().getClassLoader();
    
    try
    {
      Class<IModelObjectFactory> clss = (Class<IModelObjectFactory>)loader.loadClass( className);
      return clss.newInstance();
    }
    catch( Exception e)
    {
      getDocument().error( "Unable to resolve IModelObjectFactory class: "+className);      
      return new ModelObjectFactory();
    }
  }
  
  /**
   * Returns the matcher defined for the specified locus. The first ancestor which defines a
   * matcher determines which matcher is created and returned.
   * @param locus The locus.
   * @return Returns the matcher.
   */
  @SuppressWarnings("unchecked")
  protected IXmlMatcher getMatcher( IModelObject locus)
  {
    IModelObject matcherElement = matcherExpr.queryFirst( locus);
    if ( matcherElement == null) return new DefaultXmlMatcher();
    
    String className = Xlate.get( matcherElement, (String)null);
    if ( className == null) 
      getDocument().error(
        "Class name is undefined in matcher element: "+
          ModelAlgorithms.createIdentityPath( matcherElement));
    
    ClassLoader loader = null;
    IModelObject loaderElement = loaderExpr.queryFirst( locus);
    if ( loaderElement != null) loader = (ClassLoader)loaderElement.getValue();
    if ( loader == null) loader = getClass().getClassLoader();
    
    try
    {
      Class<IXmlMatcher> clss = (Class<IXmlMatcher>)loader.loadClass( className);
      return clss.newInstance();
    }
    catch( Exception e)
    {
      getDocument().error( "Unable to resolve IXmlMatcher class: "+className);      
      return new DefaultXmlMatcher();
    }
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    XActionDocument document = getDocument();
    if ( document != null)
    {
      IModelObject viewRoot = document.getRoot();
      if ( viewRoot != null)
      {
        XmlIO xmlIO = new XmlIO();
        xmlIO.skipOutputPrefix( "break");
        xmlIO.setOutputStyle( Style.printable);
        return xmlIO.write( viewRoot);
      }
    }
    
    return "(no document)";
  }

  private final static IExpression factoryExpr = XPath.createExpression(
    "ancestor-or-self::*/factory");
  
  private final static IExpression matcherExpr = XPath.createExpression(
    "ancestor-or-self::*/matcher");
  
  private final static IExpression loaderExpr = XPath.createExpression(
    "ancestor-or-self::*/classLoader");
  
  protected XActionDocument document;
}
