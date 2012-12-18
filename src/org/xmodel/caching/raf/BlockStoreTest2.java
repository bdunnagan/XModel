package org.xmodel.caching.raf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.xmodel.caching.raf.BlockStore.Pointer;

public class BlockStoreTest2
{
  public static void main( String[] args) throws Exception
  {
    BlockStore store = new BlockStore( "test.dat", 32, 1.5);

    Random r = new Random();
    int[] allocs = new int[ 100000];
    int[] frees = new int[ allocs.length];
    long total = 0;
    for( int i=0; i<allocs.length; i++)
    {
      allocs[ i] = r.nextInt( 10000) + 1;
      frees[ i] = r.nextInt( 10 - (i % 10));
      total += allocs[ i];
    }

    List<Pointer> allocated = new ArrayList<Pointer>( 100000);
    
    long t0 = System.nanoTime();
    for( int i=0; i<allocs.length; i+=10)
    {
      for( int j=0; j<10; j++)
      {
        Pointer p = store.alloc( allocs[ i + j]);
        allocated.add( p);
      }
      
      for( int j=0; j<10; j++)
      {
        Pointer p = allocated.get( frees[ i + j]);
        store.free( p);
      }
    }
    
    long t1 = System.nanoTime();
    System.out.printf( "Bytes: %1.3fMB\n", total / 1e6);
    System.out.printf( "Time: %1.3fs\n", ((t1 - t0) / 1e9));
    System.out.printf( "Recycle size: %s\n", store.recycle.size());
    
    store.close();
    store = null;
    
    File file = new File( "test.dat");
    long actual = file.length();
    System.out.printf( "Fragmentation: %1.3f\n", ((double)actual / total) * 100);
    
    file.delete();
  }
}
