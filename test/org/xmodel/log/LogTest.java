package org.xmodel.log;

import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogTest
{
  @Before
  public void setUp() throws Exception
  {
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void multiThreadedTest() throws Exception
  {
    List<String> names = new ArrayList<String>( 10000);
    BufferedReader reader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( "names.txt")));
    while( reader.ready())
    {
      names.add( reader.readLine());
    }

    List<Task> tasks = new ArrayList<Task>( 100);
    for( int i=0; i<100; i++)
    {
      List<String> shuffled = new ArrayList<String>( names);
      Collections.shuffle( shuffled);
      tasks.add( new Task( shuffled));
    }
    
    for( Task task: tasks) task.start();
    for( Task task: tasks) task.join();    

    List<Log> logs = Log.map.getAll( "**");
    assertTrue( "Incorrect number of logs.", logs.size() == names.size() + 1); // including wildcard log
  }  
  
  private final class Task extends Thread
  {
    public Task( List<String> names)
    {
      this.names = names;
    }
    
    public void run()
    {
      for( String name: names)
      {
        Log log = Log.getLog( name);
        Log sameLog = Log.getLog( name);
        assertTrue( "Logs don't match.", log == sameLog);
      }
    }
    
    private List<String> names;
  }
}  
