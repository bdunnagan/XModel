package org.xmodel.lss;

import java.io.IOException;
import org.apache.catalina.tribes.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DatabaseInsertTest
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
      public String extractKeyFromRecord( IRandomAccessStore store) throws IOException
      {
        int length = store.readByte();
        byte[] key = new byte[ length];
        store.read( key, 0, length);
        return new String( key, 0, length);
      }
      public String extractKeyFromRecord( byte[] content) throws IOException
      {
        int length = content[ 0];
        return new String( content, 1, length);
      }
    };
    
    recordFormat = new DefaultRecordFormat<String>( keyFormat);
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void insertWriteFullIndex() throws IOException
  {
    MemoryStore store = new MemoryStore( 1000);
    BTree<String> btree = new BTree<String>( 2, recordFormat, store);
    Database<String> db = new Database<String>( btree, store, recordFormat);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      if ( i == 5) btree.store();
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( key, record);
    }
    
    btree.store();
    
    btree = new BTree<String>( 2, recordFormat, store);
    db = new Database<String>( btree, store, recordFormat);

    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.query( key);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void insertWritePartialIndex() throws IOException
  {
    MemoryStore store = new MemoryStore( 1000);
    BTree<String> btree = new BTree<String>( 2, recordFormat, store);
    Database<String> db = new Database<String>( btree, store, recordFormat);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      if ( i == 5) btree.store();
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( key, record);
    }
    
    btree = new BTree<String>( 2, recordFormat, store);
    db = new Database<String>( btree, store, recordFormat);
    
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.query( key);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void deleteWriteFullIndex() throws IOException
  {
    MemoryStore store = new MemoryStore( 1000);
    BTree<String> btree = new BTree<String>( 2, recordFormat, store);
    Database<String> db = new Database<String>( btree, store, recordFormat);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( key, record);
    }

    btree.store();
    
    db.delete( "A");
    assertTrue( "Record not deleted", db.query( "A") == null);
    
    btree.store();
    
    btree = new BTree<String>( 2, recordFormat, store);
    db = new Database<String>( btree, store, recordFormat);
    
    assertTrue( "Record not deleted", db.query( "A") == null);
    
    for( int i=1; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.query( key);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void deleteWritePartialIndex() throws IOException
  {
    MemoryStore store = new MemoryStore( 1000);
    BTree<String> btree = new BTree<String>( 2, recordFormat, store);
    Database<String> db = new Database<String>( btree, store, recordFormat);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( key, record);
    }

    btree.store();
    
    db.delete( "A");
    assertTrue( "Record not deleted", db.query( "A") == null);

    btree = new BTree<String>( 2, recordFormat, store);
    db = new Database<String>( btree, store, recordFormat);
    
    assertTrue( "Record not deleted", db.query( "A") == null);
    
    for( int i=1; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      byte[] content = db.query( key);
      assertTrue( "Invalid record", Arrays.equals( record, content));
    }
  }
  
  @Test
  public void deleteAndCompact() throws IOException
  {
    MemoryStore store = new MemoryStore( 1000);
    BTree<String> btree = new BTree<String>( 2, recordFormat, store);
    Database<String> db = new Database<String>( btree, store, recordFormat);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      record[ 0] = 1; record[ 1] = (byte)(i + 65); record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( key, record);
    }

    db.delete( "A");
    assertTrue( "Record not deleted", db.query( "A") == null);

    db.compact( 12, store.length() - 12);
    System.out.println( store);
    
    btree.store();
    System.out.println( btree);
    System.out.println( store);
    
    Analysis<String> analysis = new Analysis<String>();
    for( Long pointer: analysis.findGarbage( store, recordFormat))
      System.out.printf( "%X\n", pointer);
  }
  
  private IKeyFormat<String> keyFormat;
  private IRecordFormat<String> recordFormat;
}
