package org.xmodel.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.xmodel.IModel;
import org.xmodel.Model;
import org.xmodel.ModelRegistry;

/**
 * An implementation of ThreadFactory that associates a shared instance of IModel with
 * each thread that is created. This behavior contrasts with the default behavior of 
 * the org.xmodel.ModelRegistry class, which creates instances of IModel on demand for
 * each thread.
 */
public class ModelThreadFactory implements ThreadFactory
{
  public ModelThreadFactory()
  {
    this( new Model());
  }
  
  public ModelThreadFactory( IModel model)
  {
    this( model, Executors.defaultThreadFactory());
  }
  
  public ModelThreadFactory( IModel model, ThreadFactory factory)
  {
    this.model = model;
    this.factory = factory;
  }
  
  /* (non-Javadoc)
   * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
   */
  @Override
  public final Thread newThread( Runnable runnable)
  {
    return factory.newThread( new RootRunnable( runnable));
  }
    
  private final class RootRunnable implements Runnable
  {
    public RootRunnable( Runnable runnable)
    {
      this.runnable = runnable;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      ModelRegistry.getInstance().setModel( model);
      runnable.run();
    }
    
    private Runnable runnable;
  }
  
  private IModel model;
  private ThreadFactory factory;
}
