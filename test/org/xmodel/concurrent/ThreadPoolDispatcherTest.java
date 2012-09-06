package org.xmodel.concurrent;

import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.IModelObject;
import org.xmodel.ModelListener;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.log.Log;
import org.xmodel.xpath.expression.StatefulContext;

public class ThreadPoolDispatcherTest
{
  @Before
  public void before() throws Exception
  {
    context = new StatefulContext();
    start = System.nanoTime();
    semaphore = new Semaphore( 0);
    DutyCycle.reset();
  }

  @Test
  public void sequencingTest() throws Exception
  {
    dispatcher = new ThreadPoolDispatcher( 16, 100, false);
    
    final Runnable runnable = new UpdateRunnable();
    final Random random = new Random();
    
    Runnable producerRunnable = new Runnable() {
      public void run()
      {
        for( int i=0; i<10000; i++)
        {
          dispatcher.execute( runnable);

          int n = random.nextInt( 20);
          for( int j=0; j<n; j++) Thread.yield();
        }
      }
    };
    
    Thread[] producers = new Thread[ 10];
    for( int i=0; i<producers.length; i++)
    {
      producers[ i] = new Thread( producerRunnable, "producer-"+i);
      producers[ i].start();
    }

    semaphore.acquireUninterruptibly();
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void lockTest() throws Exception
  {
    Log.getLog( ThreadPoolDispatcher.class).setLevel( Log.all);
    
    dispatcher = new ThreadPoolDispatcher( 16, 100);
    dispatcher.lock();
    
    final Runnable runnable = new UpdateRunnable();
    final Random random = new Random();
    
    Runnable producerRunnable = new Runnable() {
      public void run()
      {
        for( int i=0; i<10000; i++)
        {
          dispatcher.execute( runnable);

          int n = random.nextInt( 20);
          for( int j=0; j<n; j++) Thread.yield();
        }
      }
    };
    
    Thread[] producers = new Thread[ 10];
    for( int i=0; i<producers.length; i++)
    {
      producers[ i] = new Thread( producerRunnable, "producer-"+i);
      producers[ i].start();
    }

    
    Thread.sleep( 1000);
    assertTrue( context.get( "x") == null);
    
    dispatcher.unlock();
    semaphore.acquireUninterruptibly();
    
    List<IModelObject> list = (List<IModelObject>)context.get( "x");
    assertTrue( Xlate.get( list.get( 0), 0) == 100000);
  }
  
  private class UpdateRunnable implements Runnable
  {
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
      DutyCycle.getInstance().start();
      
      List<?> list = (List<?>)context.get( "x");
      if ( list == null)
      {
        ModelObject element = new ModelObject( "x");
        Xlate.set( element, 1);
        context.set( "x", element);
        
        element.addModelListener( new ModelListener() {
          public void notifyChange( IModelObject object, String attrName, Object newValue, Object oldValue)
          {
            int newInt = (Integer)newValue;
            int oldInt = (Integer)oldValue;
            assertTrue( (newInt - oldInt) == 1);
            
            if ( newInt == 100000)
            {
              System.out.printf( "elapsed: %1.0fms\n", ((System.nanoTime() - start) / 1e6));
              
              dispatcher.shutdown( false);
              
              DutyCycle.dump();
              
              semaphore.release();
            }
          }
        });
      }
      else
      {
        IModelObject element = (IModelObject)list.get( 0);
        Xlate.set( element, Xlate.get( element, 0) + 1);
      }
      
      DutyCycle.getInstance().stop();
    }
  }
  
  private StatefulContext context;
  private ThreadPoolDispatcher dispatcher;
  private Semaphore semaphore;
  private long start;
}
