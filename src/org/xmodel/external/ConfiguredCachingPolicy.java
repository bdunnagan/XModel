/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * ConfiguredCachingPolicy.java
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
package org.xmodel.external;

import org.xmodel.INode;
import org.xmodel.INodeFactory;
import org.xmodel.Xlate;
import org.xmodel.log.SLog;
import org.xmodel.xaction.ScriptAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

/** 
 * A further refinement of AbstractCachingPolicy which provides built-in support for secondary caching
 * stages and configuration from metadata annotations culled during preprocessing of a model loaded 
 * from an xml file. This class also supports the definition of a skeleton tree which is automatically
 * created when an external reference is synchronized so that secondary stages are immediately available.
 * This architecture supports resolution of an external reference in three ways.
 * 
 * <ul>
 * <li>The reference consists solely of information from the external storage location.
 * <li>The reference consists of a skeleton with zero or more secondary stages.
 * <li>The reference is a combination of the previous two methods.
 * </ul>
 * 
 * The subclass implementation of the <code>sync</code> method is responsible for applying the skeleton
 * structure. The relative position of the skeleton with respect to elements constructed from information
 * in the external storage location is implementation dependent.
 * <p>
 * <b>Most implementations of ICachingPolicy should use this base class instead of AbstractCachingPolicy.</b>
 * <p>
 * The schema and examples of the metadata annotation are provided in the metadata.xsd and metadata.xml
 * files in this package. Within the metadata annotation is an arbitrary fragment which is passed directly
 * to the caching policy and contains caching policy specific configuration.
 */
public abstract class ConfiguredCachingPolicy extends AbstractCachingPolicy
{
  protected ConfiguredCachingPolicy()
  {
    super( new UnboundedCache());
  }
  
  protected ConfiguredCachingPolicy( ICache cache)
  {
    super( cache);
  }

  /**
   * Configure this caching policy from the specified annotation.
   * @param context The context in which to evaluate expressions in the annotation.
   * @param annotation The annotation element.
   */
  public void configure( IContext context, INode annotation) throws CachingException
  {
    this.context = context;
    
    // static attributes
    String list = Xlate.childGet( annotation, "static", (String)null);
    if ( list != null)
    {
      String[] staticAttributes = list.split( ",");
      for( int i=0; i<staticAttributes.length; i++)
        staticAttributes[ i] = staticAttributes[ i].trim();
      setStaticAttributes( staticAttributes);
    }
    
    // scripts
    XActionDocument doc = new XActionDocument( annotation);
    onSync = doc.createChildScript( "onSync");
    onStore = doc.createChildScript( "onStore");
    onError = doc.createChildScript( "onError");
    
    // factory
    String factoryClassName = Xlate.get( annotation, "factory", Xlate.childGet( annotation, "factory", (String)null));
    if ( factoryClassName != null)
    {
      try
      {
        Class<?> factoryClass = getClass().getClassLoader().loadClass( factoryClassName);
        Object factory = factoryClass.newInstance();
        setFactory( (INodeFactory)factory);
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
  }
    
  /* (non-Javadoc)
   * @see org.xmodel.external.ICachingPolicy#sync(org.xmodel.external.IExternalReference)
   */
  public void sync( IExternalReference reference) throws CachingException
  {
    if ( onError == null)
    {
      syncImpl( reference);
      if ( onSync != null) onSync( reference);
    }
    else
    {
      try
      {
        syncImpl( reference);
        if ( onSync != null) onSync( reference);
      }
      catch( CachingException e)
      {
        onError( reference, e.getMessage());
      }
    }
  }

  /**
   * Called to synchronize the reference.
   * @param reference The reference.
   */
  protected abstract void syncImpl( IExternalReference reference) throws CachingException;
  
  /**
   * @return Returns the configuration context.
   */
  protected IContext getContext()
  {
    return context;
  }

  /**
   * Called just after the implementation of sync.
   * @param reference The reference for which the error occurred.
   */
  protected void onSync( IExternalReference reference)
  {
    StatefulContext context = new StatefulContext( this.context, reference);
    onSync.run( context);
  }
  
  /**
   * Called just before the implementation of flush.
   * @param reference The reference for which the error occurred.
   */
  protected void onStore( IExternalReference reference)
  {
    StatefulContext context = new StatefulContext( this.context, reference);
    onStore.run( context);
  }
  
  /**
   * Called when an exception is caught during synchronization or flushing.
   * @param reference The reference for which the error occurred.
   * @param error The error message to be set in $error.
   */
  protected void onError( IExternalReference reference, String error)
  {
    StatefulContext context = new StatefulContext( this.context, reference);
    context.set( "error", error);
    onError.run( context);
  }
  
  private IContext context;
  private ScriptAction onSync;
  private ScriptAction onStore;
  private ScriptAction onError;
}
