package org.xmodel.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import org.xmodel.IChangeRecord;
import org.xmodel.IModelObject;
import org.xmodel.IModelObjectFactory;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObjectFactory;

/**
 * An implementation of IMirrorSet which uses a bounded queue to transfer
 * mirror updates between threads. There is a single queue that all threads
 * use to post changes to their respective copies.
 */
public class QueueMirrorSet implements IMirrorSet
{
  /**
   * Create with the specified master element. Note that changes are replicated
   * between all instances, not just changes made to the master.
   * @param master The master element.
   */
  public QueueMirrorSet( IModelObject master)
  {
    this( master, 100);
  }
  
  /**
   * Create with the specified master element. Note that changes are replicated
   * between all instances, not just changes made to the master.
   * @param master The master element.
   * @param queueSize The size of the bounded queue.
   */
  public QueueMirrorSet( IModelObject master, int queueSize)
  {
    this.master = master;
    this.mirrors = new ThreadLocal<Mirror>();
    this.mirrorList = new ArrayList<Mirror>();
    this.queueSize = queueSize;
    this.factory = new ModelObjectFactory();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.concurrent.IMirrorSet#attach()
   */
  @Override
  public void attach()
  {
    attached = true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.concurrent.IMirrorSet#detach()
   */
  @Override
  public void detach()
  {
  }

  /* (non-Javadoc)
   * @see org.xmodel.concurrent.IMirrorSet#get(boolean)
   */
  @Override
  public IModelObject get( boolean create)
  {
    Mirror mirrored = mirrors.get();
    if ( mirrored == null)
    {
      if ( create)
      {
        mirrored = new Mirror();
        mirrored.element = copy();
        mirrored.queue = new ArrayBlockingQueue<IChangeRecord>( queueSize);
        mirrored.listener = new ChangeSetListener( mirrored.element, this);
        mirrored.element.addModelListener( mirrored.listener);
        synchronized( mirrorListLock)
        {
          mirrorList.add( mirrored);
        }
        return mirrored.element;
      }
      return null;
    }
    else
    {
      return mirrored.element;
    }
  }
  
  /**
   * Create a copy of the master element.
   * @return Returns the copy.
   */
  private IModelObject copy()
  {
    if ( attached)
    {
      return factory.createObject( null, master.getType());
    }
    else
    {
      return ModelAlgorithms.cloneTree( master, factory);
    }
  }
  
  /**
   * Post the specified change record.
   * @param record The record.
   */
  void post( IChangeRecord record)
  {
    synchronized( mirrorListLock)
    {
      Mirror current = mirrors.get();
      for( Mirror mirror: mirrorList)
      {
        if ( mirror == current) continue;
        try
        {
          mirror.queue.put( record);
        }
        catch( InterruptedException e)
        {
          detach();
          break;
        }
      }
    }
  }
  
  private static class Mirror
  {
    public IModelObject element;
    public BlockingQueue<IChangeRecord> queue;
    public ChangeSetListener listener;
  }

  private IModelObject master;
  private IModelObjectFactory factory;
  private ThreadLocal<Mirror> mirrors;
  private List<Mirror> mirrorList;
  private Object mirrorListLock;
  private int queueSize;
  private boolean attached;
}
