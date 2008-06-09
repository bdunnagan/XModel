/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.external.DefaultSpace;
import dunnagan.bob.xmodel.external.IExternalSpace;
import dunnagan.bob.xmodel.net.ExternalSpace;


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
   * @see dunnagan.bob.xmodel.IModelRegistry#getModel()
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
   * @see dunnagan.bob.xmodel.IModelRegistry#createCollection(java.lang.String)
   */
  public IModelObject createCollection( String name)
  {
    ModelObject root = new ModelObject( name);
    getModel().addRoot( name, root);
    return root;
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelRegistry#register(dunnagan.bob.xmodel.external.IExternalSpace)
   */
  public void register( IExternalSpace externalSpace)
  {
    if ( externalSpaces == null) externalSpaces = new ArrayList<IExternalSpace>();
    externalSpaces.add( externalSpace);
  }

  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelRegistry#unregister(dunnagan.bob.xmodel.external.IExternalSpace)
   */
  public void unregister( IExternalSpace externalSpace)
  {
    if ( externalSpaces == null) return;
    externalSpaces.remove( externalSpace);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.IModelRegistry#query(java.net.URI)
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