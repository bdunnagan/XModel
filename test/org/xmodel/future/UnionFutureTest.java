package org.xmodel.future;

import static org.junit.Assert.*;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.xmodel.future.AsyncFuture.IListener;

public class UnionFutureTest
{
  @Test
  public void test() throws Exception
  {
    final UnionFuture<Object, Integer> future = new UnionFuture<Object, Integer>( this);
    
    future.addListener( new IListener<Object>() {
      @SuppressWarnings("unchecked")
      public void notifyComplete( AsyncFuture<Object> future) throws Exception
      {
        for( AsyncFuture<Integer> task: ((UnionFuture<Object, Integer>)future).getTasks())
          if ( !task.isDone())
          {
            fail( "Not all tasks completed!");
          }
      }
    });

    @SuppressWarnings("unchecked")
    final AsyncFuture<Integer>[] tasks = new AsyncFuture[ 20000];
    for( int i=0; i<tasks.length; i++)
    {
      tasks[ i] = new AsyncFuture<Integer>( i);
    }    
    
    final Executor executor = Executors.newCachedThreadPool();

    executor.execute( new Runnable() {
      public void run()
      {
        System.out.println( "Start T1...");
        for( int i=0; i<tasks.length; i++)
        {
          future.addTask( tasks[ i]);
        }
        System.out.println( "Finish T1.");
      }
    });
    
    executor.execute( new Runnable() {
      public void run()
      {
        executor.execute( new Runnable() {
          public void run()
          {
            System.out.println( "Start T2...");
            for( int i=0; i<tasks.length; i++)
            {
              tasks[ i].notifySuccess();
            }
            System.out.println( "Finish T2.");
          }
        });
      }
    });
    
    future.await();
    
//    fail( "Not yet implemented");
  }

}
