/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import java.io.File;
import java.util.*;
import static java.lang.System.*;

/**
 * An implementation of IDeploySite which defines a deployment site in a directory on the local
 * filesystem.
 */
public class DeploySite implements IDeploySite
{
  /**
   * Create a DeploySite which detects deployment in the given directory.
   * @param directory The deployment directory.
   * @param period The number of seconds between checks of the directory.
   */
  public DeploySite( File directory, int period)
  {
    if ( !directory.isDirectory()) 
      throw new RuntimeException( "DeploySite directory argument is not a directory: "+directory);
    
    this.directory = directory;
    this.period = period * 1000;
    this.timer = new Timer();
    this.timestamp = 0;
    this.fileList = new ArrayList<File>();
  }
  
  /**
   * Start discovering new deployment changes.
   */
  public void start()
  {
    TimerTask task = new TimerTask() {
      public void run()
      {
        try
        {
          out.print( ".");
          discover();
        }
        catch( Exception e)
        {
          System.out.println( e);
        }
      }
    };
    timer.scheduleAtFixedRate( task, 0, period);
  }
  
  /**
   * Stop discovering deployment changes.
   */
  public void stop()
  {
    timer.cancel();
  }
  
  /**
   * Discover deployment changes.
   */
  private void discover()
  {
    long starttime = currentTimeMillis();
    boolean modified = false;
    File[] newList = directory.listFiles();
    File[] oldList = fileList.toArray( new File[ 0]);
    Arrays.sort( newList);

    // find added or removed files
    List<File> sameList = new ArrayList<File>();
    int i=0, j=0;
    while( i<oldList.length && j<newList.length)
    {
      int compare = oldList[ i].compareTo( newList[ j]);
      if ( compare == 0) 
      {
        sameList.add( oldList[ i]);
        i++; j++;
      }
      while( compare < 0)
      {
        modified = true;
        notifyRemove( oldList[ i++]);
        compare = oldList[ i].compareTo( newList[ j]);
      }
      while( compare > 0)
      {
        modified = true;
        notifyAdd( newList[ j++]);
        compare = oldList[ i].compareTo( newList[ j]);
      }
    }
    while( i<oldList.length)
    {
      modified = true;
      notifyRemove( oldList[ i++]);
    }
    while( j<newList.length)
    {
      modified = true;
      notifyAdd( newList[ j++]);
    }
    
    // notify modified files
    for ( i=0; i<sameList.size(); i++)
    {
      File file = sameList.get( i);
      if ( file.lastModified() > timestamp)
      {
        modified = true;
        notifyModify( file);
      }
    }
    
    // update current list of files
    if ( modified) fileList = Arrays.asList( newList);
    
    // set timestamp
    timestamp = currentTimeMillis();
    
    out.println( "elapsed = "+(timestamp-starttime));
  }
  
  /**
   * Notify listeners that a file was added.
   * @param file The file which was added.
   */
  public void notifyAdd( File file)
  {
    List<IDeployListener> list = new ArrayList<IDeployListener>( listeners);
    for ( int i=0; i<list.size(); i++)
    {
      IDeployListener listener = list.get( i);
      listener.notifyAddFile( this, file);
    }
  }
  
  /**
   * Notify listeners that a file was modified.
   * @param file The file which was modified.
   */
  public void notifyModify( File file)
  {
    List<IDeployListener> list = new ArrayList<IDeployListener>( listeners);
    for ( int i=0; i<list.size(); i++)
    {
      IDeployListener listener = list.get( i);
      listener.notifyModifyFile( this, file);
    }
  }
  
  /**
   * Notify listeners that a file was removed.
   * @param file The file which was removed.
   */
  public void notifyRemove( File file)
  {
    List<IDeployListener> list = new ArrayList<IDeployListener>( listeners);
    for ( int i=0; i<list.size(); i++)
    {
      IDeployListener listener = list.get( i);
      listener.notifyRemoveFile( this, file);
    }
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IDeploySite#addListener(dunnagan.bob.xmodel.external.IDeployListener)
   */
  public void addListener( IDeployListener listener)
  {
    if ( listeners == null) listeners = new ArrayList< IDeployListener>();
    listeners.add( listener);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.external.IDeploySite#removeListener(dunnagan.bob.xmodel.external.IDeployListener)
   */
  public void removeListener( IDeployListener listener)
  {
    if ( listeners != null) listeners.remove( listener);
  }
  
  File directory;
  int period;
  List< IDeployListener> listeners;
  Timer timer;
  long timestamp;
  List<File> fileList;
  
  public static void main( String[] args)
  {
    DeploySite site = new DeploySite( new File( "C:\\deploy"), 1);
    site.start();
    site.addListener( new IDeployListener() {
      public void notifyAddFile( IDeploySite site, File file)
      {
        out.println( "FILE ADDED: "+file);
      }
      public void notifyModifyFile( IDeploySite site, File file)
      {
        out.println( "FILE MODIFIED: "+file);
      }
      public void notifyRemoveFile( IDeploySite site, File file)
      {
        out.println( "FILE REMOVED: "+file);
      }
    });
    try { Thread.sleep( 5*60000);} catch( Exception e) {}
  }
}
