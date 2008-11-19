/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.external.DefaultSpace;
import org.xmodel.external.IExternalSpace;
import org.xmodel.net.ExternalSpace;



/**
 * An implementation of IModelRegistry which allows IModel instances to be associated with threads
 * an accessed via thread-local data.
 */
public class ModelRegistry implements IModelRegistry
{
  public ModelRegistry()
  {
    register( new DefaultSpace());
    register( new ExternalSpace());
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#getModel()
   */
  public IModel getModel()
  {
    if ( threadModel == null) threadModel = new ThreadLocal<IModel>();
    IModel model = threadModel.get();
    if ( model == null)
    {
      model = new Model();
      threadModel.set( model);
    }
    return model;
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#createCollection(java.lang.String)
   */
  public IModelObject createCollection( String name)
  {
    ModelObject root = new ModelObject( name);
    getModel().addRoot( name, root);
    return root;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#register(org.xmodel.external.IExternalSpace)
   */
  public void register( IExternalSpace externalSpace)
  {
    if ( externalSpaces == null) externalSpaces = new ArrayList<IExternalSpace>();
    externalSpaces.add( externalSpace);
  }

  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#unregister(org.xmodel.external.IExternalSpace)
   */
  public void unregister( IExternalSpace externalSpace)
  {
    if ( externalSpaces == null) return;
    externalSpaces.remove( externalSpace);
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.IModelRegistry#query(java.net.URI)
   */
  public List<IModelObject> query( URI uri)
  {
    if ( externalSpaces != null)
    {
      for( int i = externalSpaces.size() - 1; i >= 0; i--)
      {
        IExternalSpace externalSpace = externalSpaces.get( i);
        if ( externalSpace.contains( uri))
          return externalSpace.query( uri);
      }
    }
    
    return null;
  }

  /**
   * Returns the singleton.
   * @return Returns the singleton.
   */
  public static ModelRegistry getInstance()
  {
    if ( instance == null) instance = new ModelRegistry();
    return instance;
  }
  
  private static ModelRegistry instance;
  private static ThreadLocal<IModel> threadModel;
  
  private List<IExternalSpace> externalSpaces;
}