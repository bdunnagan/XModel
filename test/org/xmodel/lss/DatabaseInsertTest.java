package org.xmodel.lss;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        return new String( content, 0, length);
      }
    };
    
    recordFormat = new DefaultRecordFormat<String>( keyFormat);
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void insert() throws IOException
  {
    MemoryStore store = new MemoryStore( 1000);
    BTree<String> btree = new BTree<String>( 2, recordFormat, store);
    Database<String> db = new Database<String>( btree, store, recordFormat);

    byte[] record = new byte[ 3];
    for( int i=0; i<7; i++)
    {
      if ( i == 5) 
      {
        btree.store();
        System.out.println( btree);
        System.out.println( store);
      }
      record[ 0] = 1;
      record[ 1] = (byte)(i + 65);
      record[ 2] = '#';
      String key = String.format( "%c", i+65);
      db.insert( key, record);
      System.out.println( store);
      System.out.println();
    }
    
    //btree.store();

    System.out.println( btree);
    System.out.printf( "index: %d\n", btree.root.pointer);
    System.out.println( store);
    
    btree = new BTree<String>( 2, recordFormat, store);
    System.out.println( btree);
    
    db = new Database<String>( btree, store, recordFormat);
    
    byte[] arec = db.query( "A");
    for( int i=0; i<arec.length; i++)
      System.out.printf( "%02x ", arec[ i]);
    System.out.println();
    
    System.out.println( btree);
  }
  
  private IKeyFormat<String> keyFormat;
  private IRecordFormat<String> recordFormat;
}
