package org.xmodel.lss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
   * @param unique True if keys in the tree are unique.
   * @param storageController The storage controller for the database.
   */
  public BTree( int degree, boolean unique, StorageController<K> storageController) throws IOException
  {
    this( degree, unique, storageController, (Comparator<K>)null);
  }
  
  /**
   * Create a b+tree with the specified degree, which places bounds on the number of entries in each node.  The minimum number
   * of entries in a node is degree - 1, and the maximum number of nodes is 2 * degree - 1.  The implementation uses the specified
   * instance of IRandomAccessStore to store and retrieve nodes.
   * @param degree The degree of the b+tree.
   * @param unique True if keys in the tree are unique.
   * @param recordFormat The record format.
   * @param storageController The store controller for the database.
   * @param comparator The key comparator.
   */
  public BTree( int degree, boolean unique, StorageController<K> storageController, Comparator<K> comparator) throws IOException
  {
    this.degree = degree;
    this.unique = unique;
    this.garbage = new ArrayList<BNode<K>>();
    this.storageController = storageController;

    int minKeys = degree - 1;
    int maxKeys = 2 * degree - 1;
    root = new BNode<K>( this, minKeys, maxKeys, 0, 0, comparator);
  }
  
  /**
   * Insert a record.
   * @param key The key.
   * @param pointer The pointer.
   * @return Returns 0 or the previous record associated with the key.
   */
  public long insert( K key, long pointer) throws IOException
  {
    return root.insert( key, pointer);
  }
  
  /**
   * Delete a record with a unique key.
   * @param key The key.
   * @return Returns 0 or the pointer associated with the key.
   */
  public long delete( K key) throws IOException
  {
    return delete( key, -1);
  }
  
  /**
   * Delete a record.
   * @param key The key.
   * @param pointer The pointer (-1 for unique keys).
   * @return Returns 0 or the pointer associated with the key.
   */
  public long delete( K key, long pointer) throws IOException
  {
    pointer = root.delete( key, pointer);
    if ( root.count() == 0 && root.getChildren().size() > 0) root = root.getChildren().get( 0);
    return pointer;
  }
  
  /**
   * Returns the pointer associated with the specified unique key.
   * @param key The key.
   * @return Returns 0 or the pointer associated with the specified key.
   */
  public long get( K key) throws IOException
  {
    return root.get( key);
  }
  
  /**
   * Get a cursor for navigating keys in order.
   * @param key The unique starting key.
   * @return Returns a cursor.
   */
  public Cursor<K> getCursorUnique( K key) throws IOException
  {
    return root.getCursorUnique( key);
  }
  
  /**
   * Get a cursor for navigating keys in order.
   * @param key The unique starting key.
   * @return Returns a cursor.
   */
  public Cursor<K> getCursorNonUnique( K key) throws IOException
  {
    return root.getCursorNonUnique( key);
  }
  
  /**
   * Get a cursor for navigating keys in order.
   * @param key The starting key.
   * @param value The value (-1 for unique keys).
   * @return Returns a cursor.
   */
  public Cursor<K> getCursor( K key, long value) throws IOException
  {
    return root.getCursor( key, value);
  }
  
  /**
   * Add the specified node to the garbage.
   * @param node The node.
   */
  protected void markGarbage( BNode<K> node)
  {
    garbage.add( node);
  }
  
  /**
   * Load the root node of this b+tree from the specified pointer into the storage controller.
   */
  protected void loadFrom( long pointer) throws IOException
  {
    root.pointer = pointer;
    root.load();
  }
  
  /**
   * @return Returns the degree of the tree.
   */
  public int getDegree()
  {
    return degree;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return root.toString();
  }

  private int degree;
  protected boolean unique;
  protected StorageController<K> storageController;
  protected BNode<K> root;
  protected List<BNode<K>> garbage;
  
  public static void main( String[] args) throws Exception
  {
    BTree<String> tree = new BTree<String>( 3, true, null, new Comparator<String>() {
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
    
    Cursor<String> cursor = tree.root.getCursorUnique( "5");
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
