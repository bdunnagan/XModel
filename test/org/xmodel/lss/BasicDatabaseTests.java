package org.xmodel.lss;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import org.apache.catalina.tribes.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmodel.lss.store.IRandomAccessStore;
import org.xmodel.lss.store.IRandomAccessStoreFactory;
import org.xmodel.lss.store.MemoryStore;

public class BasicDatabaseTests
{
  @Before
  public void setUp() throws Exception
  {
    keyFormat = new IKeyFormat<String>() {
      public String readKey( IRandomAccessStore store) throws IOException
      {
        int length = store.readByte();
        byte[] bytes = new byte[ length];
        store.read( bytes, 0, length);
        return new String( bytes);
      }
      public void writeKey( IRandomAccessStore store, String key) throws IOException
      {
        store.writeByte( (byte)key.length());
        byte[] bytes = key.getBytes();
        store.write( bytes, 0, bytes.length);
      }
      public String[] extractKeysFromRecord( IRandomAccessStore store, long recordLength) throws IOException
      {
        int length = store.readByte();
        byte[] key = new byte[ length];
        store.read( key, 0, length);
        return new String[] { new String( key, 0, length)};
      }
      public String[] extractKeysFromRecord( byte[] content) throws IOException
      {
        int length = content[ 0];
        return new String[] { new String( content, 1, length)};
      }
    };

    factory = new IRandomAccessStoreFactory() {
      public IRandomAccessStore createInstance( int id) throws IOException
      {
        return new MemoryStore( 1000);
//        return new FileStore( "store-"+id+".dat");
      }
    };
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void insertWriteFullIndex() throws IOException
  {
    storageController = new StorageController<String>( factory, keyFormat, 20);
    BTree<String> btree = new BTree<String>( 2, true, storageController);
    Database<String> db = new Database<String>( Collections.singletonList( btree), storageController);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      if ( i == 5) db.storeIndex();
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( new String[] { key}, record);
    }
    
    db.storeIndex();
    
    btree = new BTree<String>( 2, true, storageController);
    db = new Database<String>( Collections.singletonList( btree), storageController);

    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.search( key, 0);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void insertWritePartialIndex() throws IOException
  {
    storageController = new StorageController<String>( factory, keyFormat, 20);
    BTree<String> btree = new BTree<String>( 2, true, storageController);
    Database<String> db = new Database<String>( Collections.singletonList( btree), storageController);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      if ( i == 5) db.storeIndex();
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert(  new String[] { key}, record);
    }
    
    btree = new BTree<String>( 2, true, storageController);
    db = new Database<String>( Collections.singletonList( btree), storageController);
    
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.search( key, 0);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void deleteWriteFullIndex() throws IOException
  {
    storageController = new StorageController<String>( factory, keyFormat, 20);
    BTree<String> btree = new BTree<String>( 2, true, storageController);
    Database<String> db = new Database<String>( Collections.singletonList( btree), storageController);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert(  new String[] { key}, record);
    }

    db.storeIndex();
    
    db.delete( "A", 0);
    assertTrue( "Record not deleted", db.search( "A", 0) == null);
    
    db.storeIndex();
    
    btree = new BTree<String>( 2, true, storageController);
    db = new Database<String>( Collections.singletonList( btree), storageController);
    
    assertTrue( "Record not deleted", db.search( "A", 0) == null);
    
    for( int i=1; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.search( key, 0);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void deleteWritePartialIndex() throws IOException
  {
    storageController = new StorageController<String>( factory, keyFormat, 20);
    BTree<String> btree = new BTree<String>( 2, true, storageController);
    Database<String> db = new Database<String>( Collections.singletonList( btree), storageController);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( new String[] { key}, record);
    }

    db.storeIndex();
    
    db.delete( "A", 0);
    assertTrue( "Record not deleted", db.search( "A", 0) == null);

    btree = new BTree<String>( 2, true, storageController);
    db = new Database<String>( Collections.singletonList( btree), storageController);
    
    assertTrue( "Record not deleted", db.search( "A", 0) == null);
    
    for( int i=1; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.search( key, 0);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void randomInsertDelete() throws IOException
  {
    int degree = 100;
    storageController = new StorageController<String>( factory, keyFormat, 1000);
    BTree<String> btree = new BTree<String>( degree, true, storageController);
    Database<String> db = new Database<String>( Collections.singletonList( btree), storageController);

    Random random = new Random( 1);
    byte[] record = new byte[ 3];
    for( int i=0; i<100000; i++)
    {
      int j = random.nextInt( 26);
      record[ 0] = 1; record[ 1] = (byte)(j + 65); record[ 2] = '#';
      String key = String.format( "%c", 65 + j);
      db.insert( new String[] { key}, record);
      
      j = random.nextInt( 26);
      key = String.format( "%c", 65 + j);
      db.delete( key, 0);
      
      if ( (i % 10000) == 0)
      {
        long t0 = System.nanoTime();
        btree = new BTree<String>( degree, true, storageController);
        db = new Database<String>( Collections.singletonList( btree), storageController);
        
        long t1 = System.nanoTime();
        db.storeIndex();
        
        long t2 = System.nanoTime();
        System.out.printf( "index=%1.3fms, store=%1.3fms\n", (t1 - t0) / 1e6, (t2 - t1) / 1e6);
      }
    }
  }
  
  @Test
  public void garbageCollection() throws IOException
  {
    int degree = 3;
    storageController = new StorageController<String>( factory, keyFormat, 300);
    BTree<String> btree = new BTree<String>( degree, true, storageController);
    Database<String> db = new Database<String>( Collections.singletonList( btree), storageController);

    byte[] record = new byte[ 3];
    for( int i=0; i<15; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", 65 + i);
      db.insert( new String[] { key}, record);
    }
    
    db.storeIndex();
    
    for( int i=15; i<26; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", 65 + i);
      db.insert( new String[] { key}, record);
    }
    
    for( int i=0; i<12; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", 65 + i);
      db.delete( key, 0);
    }
    
    storageController.garbageCollect( db);
    
    for( int i=0; i<26; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", 65 + i);
      if ( i < 12) assertTrue( db.search( key, 0) == null);
      else assertTrue( db.search( key, 0) != null);
    }
  }
  
  private IKeyFormat<String> keyFormat;
  private IRandomAccessStoreFactory factory;
  private StorageController<String> storageController;
}
