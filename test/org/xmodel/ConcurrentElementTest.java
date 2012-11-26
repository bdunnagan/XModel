package org.xmodel;

import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * Test concurrent read/write access to elements.
 */
public class ConcurrentElementTest
{
  @Test public void oneWriterOneReader() throws Exception
  {
    runTest( 1, 1);
  }
  
  @Test public void oneWriteManyReaders() throws Exception
  {
    runTest( 1, 100);
  }
  
  @Test public void manyWritersOneReader() throws Exception
  {
    runTest( 100, 1);
  }
  
  @Test public void manyWritersManyReaders() throws Exception
  {
    runTest( 100, 100);
  }
  
  private void runTest( int readers, int writers) throws Exception
  {
    ExecutorService readerPool = Executors.newFixedThreadPool( readers);
    ExecutorService writerPool = Executors.newFixedThreadPool( writers);

    List<INode> nodes = Collections.<INode>singletonList( new ModelObject( "test"));

    Task readerTask = new Task( nodes, false, 10000, 0, new Reader());
    Task writerTask = new Task( nodes, false, 10000, 0, new Writer());
    readerPool.execute( readerTask);
    writerPool.execute( writerTask);
    
    writerPool.shutdown();
    readerPool.shutdown();
    
    readerPool.awaitTermination( 1, TimeUnit.DAYS);
    writerPool.awaitTermination( 1, TimeUnit.DAYS);
    
    assertTrue( "Reader did not finish.", readerTask.loops == 0);
    assertTrue( "Writer did not finish.", writerTask.loops == 0);
  }

  private static boolean sleep( int time)
  {
    try { Thread.sleep( time);} catch( Exception e) { return false;}
    return true;
  }
  
  private static interface Callback<T>
  {
    public void call( T t);
  }
  
  private static class Reader implements Callback<INode>
  {
    public void call( INode node)
    {
      // read all attributes
      for( String attrName: node.getAttributeNames())
        node.getAttribute( attrName);
      
      // iterate children
      for( INode child: node.getChildren())
      {
        System.out.println( child.getType());
      }
    }
  }
  
  private static class Writer implements Callback<INode>
  {
    public void call( INode node)
    {
      // write attributes
      node.setValue( counter++);
      if ( node.getAttribute( "test") == null)
        node.setAttribute( "test", "true");
      else
        node.removeAttribute( "test");
      
      // add/remove children
      if ( node.getFirstChild( "test") != null)
        node.getCreateChild( "test");
      else
        node.removeChildren( "test");
    }
    
    private int counter;
  }
  
  private static class Task implements Runnable
  {
    public Task( List<INode> nodes, boolean shuffle, int loops, int sleep, Callback<INode> callback)
    {
      this.nodes = nodes;
      this.shuffle = shuffle;
      this.loops = loops;
      this.sleep = sleep;
      this.callback = callback;
    }
    
    public void run()
    {
      while( loops-- >= 0 && sleep( sleep))
      {
        if ( shuffle) Collections.shuffle( nodes);
        for( INode node: nodes) callback.call( node);
      }
    }
    
    private List<INode> nodes;
    private boolean shuffle;
    private int sleep;
    private Callback<INode> callback;
    public int loops;
  }
}
