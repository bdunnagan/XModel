package org.xmodel.log;

import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PackageMapTest
{
  @Before
  public void setUp() throws Exception
  {
    map = new PackageMap();
  }

  @After
  public void tearDown() throws Exception
  {
    map = null;
  }

  @Test
  public void createLogTest()
  {
    Log log = map.getCreateOne( "a.b");
    assertTrue( "Log was not created.", log != null);
    
    Log sameLog = map.getCreateOne( "a.b");
    assertTrue( "Incorrect log returned.", sameLog == log);
    
    List<Log> logs = map.getAll( "a.b");
    assertTrue( "Incorrect number of logs.", logs.size() == 1);
    assertTrue( "Log not returned in list.", logs.contains( log));
  }
  
  @Test
  public void starWildcardTest()
  {
    List<Log> wildcardLogs = map.getAll( "a.*");
    assertTrue( "Wildcard log not created.", wildcardLogs.size() == 1);

    Log wildcardLog = wildcardLogs.get( 0);
    map.getCreateOne( "a.b");
    
    List<Log> logs = map.getAll( "a.b");
    assertTrue( "Incorrect number of logs.", logs.size() == 1);
    
    logs = map.getAll( "a.*");
    assertTrue( "Widlcard log not present.", logs.contains( wildcardLog));
    assertTrue( "Should be two logs.", logs.size() == 2);
  }
  
  @Test
  public void starStarWildcardTest()
  {
    List<Log> wildcardLogs = map.getAll( "a.**");
    assertTrue( "Wildcard log not created.", wildcardLogs.size() == 1);

    Log wildcardLog = wildcardLogs.get( 0);
    map.getCreateOne( "a.b");
    
    List<Log> logs = map.getAll( "a.b");
    assertTrue( "Incorrect number of logs.", logs.size() == 1);
    
    logs = map.getAll( "a.**");
    assertTrue( "Widlcard log not present.", logs.contains( wildcardLog));
    assertTrue( "Should be two logs.", logs.size() == 2);
  }
  
  @Test
  public void performanceTest() throws IOException
  {
//    FileWriter writer = new FileWriter( "test/org/xmodel/log/names.txt");
//    HashSet<String> set = new HashSet<String>();
//    
//    for( File folder: new File( "/Users/bdunnagan/git/Cornerstone/common/lib").listFiles())
//    {
//      if ( !folder.isDirectory()) continue;
//      
//      for ( File file: folder.listFiles())
//      {
//        if ( !file.getName().contains( ".jar")) continue;
//        JarFile jar = new JarFile( file);
//        Enumeration<JarEntry> entries = jar.entries();
//        while( entries.hasMoreElements())
//        {
//          JarEntry entry = entries.nextElement();
//          String name = entry.getName().replace( '/', '.');
//          if ( name.endsWith( ".class") && !set.contains( name))
//          {
//            set.add( name);
//            writer.append( name.substring( 0, name.length() - 6));
//            writer.append( "\n");
//          }
//        }
//      }
//    }
//    writer.close();
        
    List<String> names = new ArrayList<String>( 10000);
    BufferedReader reader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( "names.txt")));
    while( reader.ready())
    {
      names.add( reader.readLine());
    }
    
    long t0 = System.nanoTime();
    for( String name: names) map.getCreateOne( name);
    long t1 = System.nanoTime();
    long te = (t1 - t0) / names.size();
    assertTrue( "Log creation takes too long.", te < 10000);
      
    t0 = System.nanoTime();
    List<Log> logs = map.getAll( "org.apache.activemq.camel.*");
    assertTrue( "Incorrect count of logs.", logs.size() == 12 + 1); // including wildcard log
    
    t1 = System.nanoTime();
    assertTrue( "Wildcard (*) lookup takes too long.", (t1 - t0) < 100000);
      
    t0 = System.nanoTime();
    logs = map.getAll( "org.apache.**");
    assertTrue( "Incorrect count of logs.", logs.size() == 4456 + 2); // including both wildcard logs
    t1 = System.nanoTime();
    assertTrue( "Wildcard (**) lookup takes too long.", (t1 - t0) < 10000000);
  }
  
  PackageMap map;
}  
