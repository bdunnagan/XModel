package org.xmodel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.xml.XmlIO;

/**
 * A class that creates a map of the associations between top-level HTTP URLs.
 */
public class UrlMapper
{
  public UrlMapper( URL url, long duration)
  {
    queue = new ArrayBlockingQueue<Node>( 100);
    stop = System.currentTimeMillis() + duration;
    executor = Executors.newFixedThreadPool( 100);
    map = Collections.synchronizedMap( new HashMap<String, Node>());
    regex = Pattern.compile( "[<]a[^>]*href\\s*[=]\\s*[\"]([^\"]++)[\"]");
    root = new Node( url);
  }

  /**
   * Start mapping.
   */
  public void start() throws InterruptedException
  {
    executor.execute( root);
    
    while( true)
    {
      Node node = queue.poll( 1, TimeUnit.SECONDS);
      if ( node == null) break;
      System.out.println( "mapping: "+node.url);
      parse( node, node.html);
    }
  }
  
  /**
   * Load the HTML from the specified URL.
   * @param url The url.
   * @return Returns the loaded HTML.
   */
  private String load( URL url) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    InputStream stream = url.openStream();
    BufferedReader reader = new BufferedReader( new InputStreamReader( stream));
    while( reader.ready())
    {
      sb.append( reader.readLine());
    }
    return sb.toString();
  }
  
  /**
   * Parse the specified html looking for top-level links.
   * @param parent The node being mapped.
   * @param html The html.
   */
  private void parse( Node parent, String html)
  {
    if ( executor.isShutdown()) return;
    
    List<Node> list = new ArrayList<Node>();
    Matcher matcher = regex.matcher( html);
    while( matcher.find())
    {
      try
      {
        URL url = new URL( matcher.group( 1));
        String host = url.getHost();
        Node node = map.get( host);
        if ( node != null)
        {
          parent.nodes.add( node);
        }
        else
        {
          Node newNode = new Node( url);
          map.put( host, newNode);
          parent.nodes.add( newNode);
          list.add( newNode);
        }
      }
      catch( MalformedURLException e)
      {
      }
    }

    for( Node node: list) executor.execute( node);
  }
  
  /**
   * Create an element from the internal data-model.
   * @return Returns the new element.
   */
  public IModelObject toElement()
  {
    Random random = new Random();
    Map<Node, IModelObject> map = new HashMap<Node, IModelObject>();
    
    IModelObject rootElement = new ModelObject( "site", Identifier.generate( random, 16));
    rootElement.setAttribute( "url", root.url);
    map.put( root, rootElement);
    
    Stack<Node> stack = new Stack<Node>();
    stack.push( root);
    
    while( !stack.empty())
    {
      Node node = stack.pop();
      
      IModelObject parent = map.get( node);
      for( Node child: node.nodes)
      {
        IModelObject element = map.get( child);
        if ( element != null)
        {
          element = new ModelObject( "siteref", element.getID());
        }
        else
        {
          element = new ModelObject( "site", Identifier.generate( random, 16));
          stack.push( child);
        }
        
        map.put( child, element);
        element.setAttribute( "url", node.url);
        parent.addChild( element);
      }
    }
    
    return rootElement;
  }
  
  public class Node implements Runnable
  {
    public Node( URL url)
    {
      this.url = url;
      this.nodes = Collections.synchronizedSet( new HashSet<Node>());
    }
    
    public void run()
    {
      long time = System.currentTimeMillis();
      if ( time > stop)
      {
        if ( !executor.isShutdown()) executor.shutdownNow();
        return;
      }
      
      try
      {
        html = load( url);
        queue.put( this);
      }
      catch( Exception e)
      {
      }
    }
    
    public URL url;
    public Set<Node> nodes;
    public String html;
  }
  
  private Node root;
  private Map<String, Node> map;
  private Pattern regex;
  private ExecutorService executor;
  private BlockingQueue<Node> queue;
  private long stop;
  
  public static void main( String[] args) throws Exception
  {
    UrlMapper mapper = new UrlMapper( new URL( "http://news.google.com/nwshp?hl=en&tab=wn"), 10000);
    mapper.start();

    System.out.println( "Saving ...");
    
    Stack<Node> stack = new Stack<Node>();
    stack.push( mapper.root);
    
    XmlIO xmlIO = new XmlIO();
    String xml = xmlIO.write( mapper.toElement());
    byte[] bytes = xml.getBytes();
    
    //TabularCompressor compressor = new TabularCompressor( PostCompression.zip);
    //byte[] bytes = compressor.compress( mapper.toElement());
    
    File file = new File( "map.xml");
    FileOutputStream stream = new FileOutputStream( file);
    stream.write( bytes);
    stream.close();
    
    System.out.println( "Done.");
    System.exit( 1);
  }
}
