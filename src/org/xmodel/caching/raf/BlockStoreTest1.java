package org.xmodel.caching.raf;

import java.io.File;
import java.util.Random;
import org.xmodel.caching.raf.BlockStore.Pointer;

public class BlockStoreTest1
{
  public static void main( String[] args) throws Exception
  {
    BlockStore store = new BlockStore( "test.dat", 1, 1.5);

    Random r = new Random();
    int[] allocs = new int[ 1000];
    long total = 0;
    for( int i=0; i<allocs.length; i++)
    {
      allocs[ i] = r.nextInt( 10000) + 1;
      total += allocs[ i];
    }
    
    long t0 = System.nanoTime();
    for( int i=0; i<allocs.length; i++)
    {
      Pointer p = store.alloc( allocs[ i]);
      store.free( p);
    }
    
    long t1 = System.nanoTime();
    System.out.printf( "Bytes: %1.3fMB\n", total / 1e6);
    System.out.printf( "Time: %1.3fs\n", ((t1 - t0) / 1e9));
        
    store.close();
    store = null;
    
    File file = new File( "test.dat");
    long actual = file.length();
    System.out.printf( "Fragmentation: %1.3f\n", ((double)actual / total) * 100);
    
    file.delete();
  }
}
