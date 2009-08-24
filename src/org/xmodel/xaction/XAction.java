/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.xaction;

import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.ModelObjectFactory;
import org.xmodel.Xlate;
import org.xmodel.diff.ConfiguredXmlMatcher;
import org.xmodel.diff.DefaultXmlMatcher;
import org.xmodel.diff.IXmlMatcher;
import org.xmodel.xaction.debug.IDebugger;
import org.xmodel.xml.XmlIO;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;
import org.xmodel.xpath.variable.IVariableScope;

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
   * @see org.xmodel.xaction.IXAction#configure(org.xmodel.xaction.XActionDocument)
   */
  public void configure( XActionDocument document)
  {
    this.document = document;
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#run()
   */
  public IVariableScope run()
  {
    StatefulContext context = new StatefulContext( new ModelObject( "root"));
    run( context);
    return context.getScope();
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public final Object[] run( IContext context)
  {
    if ( debugger != null) debugger.push( context, this);
    try
    {
      return doRun( context);
    }
    finally
    {
      if ( debugger != null) debugger.pop();
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#run(org.xmodel.xpath.expression.IContext)
   */
  public abstract Object[] doRun( IContext context);
  
  /* (non-Javadoc)
   * @see org.xmodel.xaction.IXAction#setDocument(org.xmodel.xaction.XActionDocument)
   */
  public void setDocument( XActionDocument document)
  {
    this.document = document;
  }

  /* (non-Javadoc)
   * @see org.xmodel.ui.swt.form.IXAction#getViewModel()
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
    if ( matcherElement == null) return new ConfiguredXmlMatcher();
    
    String className = Xlate.get( matcherElement, (String)null);
    if ( className == null) 
      getDocument().error(
        "Class name is undefined in matcher element: "+
          ModelAlgorithms.createIdentityPath( matcherElement));
    
    ClassLoader loader = null;
    IModelObject loaderElement = loaderExpr.queryFirst( locus);
    if ( loaderElement != null) loader = (ClassLoader)loaderElement.getValue();
    if ( loader == null) loader = getDocument().getClassLoader();
    
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

  private final IExpression factoryExpr = XPath.createExpression(
    "ancestor-or-self::*/factory");
  
  private final IExpression matcherExpr = XPath.createExpression(
    "ancestor-or-self::*/matcher");
  
  private final IExpression loaderExpr = XPath.createExpression(
    "ancestor-or-self::*/classLoader");

  public static IDebugger debugger;
  
  protected XActionDocument document;
}
