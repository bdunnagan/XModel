/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.external.caching;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.external.CachingException;
import org.xmodel.external.ConfiguredCachingPolicy;
import org.xmodel.external.ExternalReference;
import org.xmodel.external.ICache;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.UnboundedCache;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * A caching policy for jar files. This caching policy loads the contents of the jar file in stages.
 */
public class JarCachingPolicy extends ConfiguredCachingPolicy
{
  public JarCachingPolicy()
  {
    this( new UnboundedCache());
  }
  
  public JarCachingPolicy( ICache cache)
  {
    super( cache);
    
    setStaticAttributes( new String[] { "path", "jar"});
    defineNextStage( XPath.createExpression( ".//*[ @entry]"), new JarEntryCachingPolicy(), true);
    filterExpr = XPath.createExpression( "matches( $path, '.*[.]xml')");
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
   */
  @Override
  public void configure( IContext context, IModelObject annotation) throws CachingException
  {
    super.configure( context, annotation);
    filterExpr = Xlate.get( annotation, "filter", (IExpression)null);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
   */
  @Override
  protected void syncImpl( IExternalReference reference) throws CachingException
  {
    File file = new File( Xlate.get( reference, "path", "."));
    if ( file.canRead())
    {
      try
      {
        JarFile jarFile = new JarFile( file);
        reference.setAttribute( "jar", jarFile);
        
        IModelObject clone = reference.cloneObject();
        
        // convert manifest to xml
        Manifest manifest = jarFile.getManifest();
        IModelObject manifestElement = new ModelObject( "manifest");
        IModelObject mainRoot = new ModelObject( "attributes");
        mainRoot.setAttribute( "name", "main");
        Attributes mainAttributes = manifest.getMainAttributes();
        for( Object attribute: mainAttributes.keySet())
        {
          IModelObject mainChild = new ModelObject( attribute.toString());
          Object value = mainAttributes.get( attribute);
          mainChild.setValue( value);
          mainRoot.addChild( mainChild);
        }
        manifestElement.addChild( mainRoot);
        
        for( String name: manifest.getEntries().keySet())
        {
          IModelObject root = new ModelObject( "attributes");
          root.setAttribute( "name", name);
          Attributes attributes = manifest.getAttributes( name);
          for( Object attribute: attributes.keySet())
          {
            IModelObject child = new ModelObject( attribute.toString());
            Object value = attributes.get( attribute);
            child.setValue( value);
            root.addChild( child);
          }
          manifestElement.addChild( root);
        }
        
        clone.addChild( manifestElement);
        
        // create elements for root entries
        String separator = null;
        Enumeration<JarEntry> iter = jarFile.entries();
        while( iter.hasMoreElements())
        {
          JarEntry entry = iter.nextElement();
          if ( entry.isDirectory()) continue;
          
          String path = entry.getName();
          
          if ( separator == null) 
          {
            separator = determinePathSeparator( path);
          }
          
          // populate entire directory tree
          StatefulContext context = new StatefulContext( reference);
          context.set( "path", path);
          if ( filterExpr == null || filterExpr.evaluateBoolean( context))
          {
            if ( separator != null)
            {
              IModelObject element = clone;
              StringTokenizer tokenizer = new StringTokenizer( path, separator);
              while( tokenizer.hasMoreTokens())
              {
                String token = tokenizer.nextToken();
                element = element.getCreateChild( token);
              }
  
              if ( !entry.isDirectory())
                element.setAttribute( "entry", entry);
            }
            else
            {
              IModelObject element = new ModelObject( entry.getName());
              element.setAttribute( "entry", entry);
              clone.addChild( element);
            }
          }
        }
        
        clone.setAttribute( "separator", separator);
        update( reference, clone);
      }
      catch( IOException e)
      {
        throw new CachingException( "Unable to load jar file: "+file, e);
      }
    }
  }
  
  
  /**
   * Determines the path separator character by scanning the path.
   * @param path The path.
   * @return Returns null or the path separator.
   */
  private static String determinePathSeparator( String path)
  {
    int index = path.indexOf( "\\ ");
    if ( index >= 0) return "/";
    
    index = path.indexOf( "/");
    if ( index >= 0) return "/";
    
    return null;
  }
  
  private IExpression filterExpr;
  
  public static void main( String[] args) throws Exception
  {
    StringTokenizer tokener = new StringTokenizer( "main.xml", "/");
    if ( tokener.hasMoreTokens()) System.out.println( tokener.nextToken());
    System.exit( 1);
    
    IExternalReference reference = new ExternalReference( "jar");
    reference.setCachingPolicy( new JarCachingPolicy());
    reference.setAttribute( "path", "/Users/bdunnagan/xmodel.jar");
    reference.setDirty( true);
    
    System.out.println( XmlIO.toString( reference));
  }
}
