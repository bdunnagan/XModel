package org.xmodel.log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * An efficient implementation of a mapping of packages to all Log instances contained
 * within the package or any of its descendant packages.  Using this map, logs can be
 * associated with either packages or classes.  Two types of wildcards are supported:
 * "*" returns all the logs in a single package, and "**" returns all logs in a package
 * and all of its subpackages.
 */
public class PackageMap
{
  public PackageMap()
  {
    root = new Item();
  }
  
  public Log getCreateOne( String name)
  {
    List<String> tokens = tokenize( name);
    
    int last = tokens.size() - 1;
    Item item = findCreate( tokens, last);
    
    String lastToken = tokens.get( last);
    Log log = item.logs.get( lastToken);
    if ( log == null)
    {
      Log wildcard = item.logs.get( "*");
      if ( wildcard == null)
      {
        Item ancestor = item.parent;
        while( ancestor != null && wildcard == null)
        {
          wildcard = ancestor.logs.get( "**");
          ancestor = ancestor.parent;
        }
      }
      
      log = (wildcard != null)? new Log( wildcard): new Log();
      item.logs.put( lastToken, log);
    }
    
    return log;
  }
    
  public List<Log> getAll( String name)
  {
    List<String> tokens = tokenize( name);
    int last = tokens.size() - 1;
    if ( tokens.get( last).equals( "**"))
    {
      Item item = findCreate( tokens, last);
      
      List<Log> logs = new ArrayList<Log>();
      getAllLogs( item, logs);

      if ( !item.logs.containsKey( "**"))
      {
        item.logs.put( "**", starStarLog);
        logs.add( starStarLog);
      }
      
      return logs;
    }
    else if ( tokens.get( last).equals( "*"))
    {
      Item item = findCreate( tokens, last);
      
      List<Log> logs = new ArrayList<Log>();
      logs.addAll( item.logs.values());
      
      if ( !item.logs.containsKey( "*"))
      {
        item.logs.put( "*", starLog);
        logs.add( starLog);
      }
      
      return logs;
    }
    else
    {
      Item item = findCreate( tokens, last);
      
      Log log = item.logs.get( tokens.get( last));
      if ( log == null) return Collections.emptyList();
      
      return Collections.singletonList( log);
    }
  }
  
  private Item findCreate( List<String> tokens, int count)
  {
    Item item = root;

    for( int i=0; i<count; i++)
    {
      String token = tokens.get( i);
      
      if ( item.map == null) 
        item.map = new HashMap<String, Item>();
      
      Item next = item.map.get( token);
      if ( next == null)
      {
        next = new Item( item);
        item.map.put( token, next);
      }
      
      item = next;
    }

    return item;
  }
  
  private void getAllLogs( Item item, List<Log> logs)
  {
    Stack<Item> stack = new Stack<Item>();
    stack.push( item);
    while( !stack.empty())
    {
      item = stack.pop();
      logs.addAll( item.logs.values());
      
      if ( item.map != null)
      {
        for( Item child: item.map.values())
          stack.push( child);
      }
    }
  }
  
  private List<String> tokenize( String name)
  {
    List<String> list = new ArrayList<String>();
    
    char[] chars = name.toCharArray();
    int s = 0;
    for( int i=0; i<name.length(); i++)
    {
      final char c = name.charAt( i);
      if ( c == '.')
      {
        list.add( new String( chars, s, (i-s)));
        s = i+1;
      }
    }

    list.add( new String( chars, s, chars.length - s));
    return list;
  }

  private final static class Item
  {
    public Item()
    {
      this( null);
    }
    
    public Item( Item parent)
    {
      this.parent = parent;
      this.logs = new HashMap<String, Log>();
    }
    
    public Item parent;
    public Map<String, Item> map;
    public Map<String, Log> logs;
  }
  
  private static Log starLog = new Log();
  private static Log starStarLog = new Log();
  
  private Item root;
}
