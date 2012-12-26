package org.xmodel.lss;

import java.util.Comparator;

/**
 * A B+Tree implementation that supports arbitrary keys and uses an instance of IRandomAccessStore to load and store nodes.
 */
public class BTree<K>
{
  /**
   * Create a b+tree with the specified degree, which places bounds on the number of entries in each node.  The minimum number
   * of entries in a node is degree - 1, and the maximum number of nodes is 2 * degree - 1.  The implementation uses the specified
   * instance of IRandomAccessStore to store and retrieve nodes.
   * @param degree The degree of the b+tree.
   * @param store The store.
   * @param comparator The key comparator.
   */
  public BTree( int degree, IRandomAccessStore<K> store, Comparator<K> comparator)
  {
    int minKeys = degree - 1;
    int maxKeys = 2 * degree - 1;
    
    this.store = store;
    store.seek( 0);

    int storeDegree = store.readInt();
    if ( storeDegree == 0)
    {
      store.writeInt( degree);
      store.writeLong( 0);
      root = new BNode<K>( this, minKeys, maxKeys, 0, 0, comparator);
    }
    else if ( storeDegree == degree)
    {
      long position = store.readLong();
      root = new BNode<K>( this, minKeys, maxKeys, position, 0, comparator);
      root.load();
    }
    else
    {
      throw new IllegalStateException();
    }
  }
  
  /**
   * Insert a record.
   * @param key The key.
   * @param pointer The pointer.
   * @return Returns 0 or the previous record associated with the key.
   */
  public long insert( K key, long pointer)
  {
    return root.insert( key, pointer);
  }
  
  /**
   * Delete a record.
   * @param key The key.
   * @return Returns 0 or the pointer associated with the key.
   */
  public long delete( K key)
  {
    long pointer = root.delete( key);
    if ( root.count() == 0 && root.children().size() > 0) root = root.children().get( 0);
    return pointer;
  }
  
  /**
   * Returns the pointer associated with the specified key.
   * @param key The key.
   * @return Returns 0 or the pointer associated with the specified key.
   */
  public long get( K key)
  {
    return root.get( key);
  }

  IRandomAccessStore<K> store;
  BNode<K> root;
  
  public static void main( String[] args) throws Exception
  {
    BTree<String> tree = new BTree<String>( 3, null, new Comparator<String>() {
      public int compare( String lhs, String rhs)
      {
        return lhs.compareTo( rhs);
      }
    });

    String s = "AFSHZ3UCY6RVLXDN7Q4MEP5BL2G89BW1I";

    for( int i=0; i<s.length(); i++)
    {
      String key = ""+s.charAt( i);
      System.out.printf( "Insert %s\n", key);
      tree.insert( key, 0);
      System.out.println( tree.root);
      System.out.println( "------------------------------------------------------------");
    }
    
    Cursor<String> cursor = tree.root.getCursor( "5");
    while( cursor.hasPrevious())
    {
      System.out.printf( "Traverse: %s\n", cursor.get().getKey());
      cursor = cursor.previous();
    }
    
//    for( int i=0; i<s.length(); i++)
//    {
//      String key = ""+s.charAt( i);
//      System.out.printf( "Delete %s\n", key);
//      tree.delete( key);
//      System.out.println( tree.root);
//      System.out.println( "------------------------------------------------------------");
//    }
  }
}
