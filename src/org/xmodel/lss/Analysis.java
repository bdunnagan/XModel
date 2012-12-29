package org.xmodel.lss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmodel.lss.IRecordFormat.RecordType;

public class Analysis<K>
{
  /**
   * Scans the entire store and returns pointers to all garbage records.
   * @param store The store.
   * @param format The record format.
   * @return Returns the list of pointers.
   */
  public List<Long> findGarbage( IRandomAccessStore store, IRecordFormat<K> format) throws IOException
  {
    store.seek( 4 + 8);
    
    List<Long> result = new ArrayList<Long>();
    while( store.position() < store.length())
    {
      long position = store.position();
      RecordType type = format.advance( store);
      if ( type == RecordType.garbage) result.add( position);
    }
    
    return result;
  }
}
