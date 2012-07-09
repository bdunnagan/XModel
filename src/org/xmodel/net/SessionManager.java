package org.xmodel.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.xmodel.net.Protocol.SessionInfo;

class SessionManager
{
  public SessionManager()
  {
    this.random = new Random();
    this.sessions = new HashMap<ILink, Map<Integer, SessionInfo>>();
  }
  
  /**
   * Allocate a new session identifier.
   * @param link The link.
   * @return Returns the new identifier.
   */
  public synchronized int allocate( ILink link)
  {
    Integer session = random.nextInt( Integer.MAX_VALUE);
    register( link, session);
    return session;
  }
  
  /**
   * Deallocate a session identifier.
   * @param link The link.
   * @param session The session identifier.
   */
  public synchronized void deallocate( ILink link, int session)
  {
    Map<Integer, SessionInfo> map = getSessionMap( link);
    map.remove( session);
  }
  
  /**
   * Register a session identifier.
   * @param link The link.
   * @param session The session identifier.
   */
  public synchronized void register( ILink link, int session)
  {
    Map<Integer, SessionInfo> map = getSessionMap( link);
    map.put( session, new Protocol.SessionInfo( session));
  }
  
  /**
   * Returns the SessionInfo object associated with the specified session.
   * @param link The link.
   * @param session The session identifier.
   * @return Returns null or the SessionInfo object.
   */
  public synchronized SessionInfo getSessionInfo( ILink link, int session)
  {
    Map<Integer, SessionInfo> map = getSessionMap( link);
    return map.get( session);
  }
  
  /**
   * Returns the SessionInfo objects associated with the specified link.
   * @param link The link.
   * @return Returns the list of SessionInfo objects.
   */
  public synchronized List<SessionInfo> getSessionInfoList( ILink link)
  {
    Map<Integer, SessionInfo> map = getSessionMap( link);
    return new ArrayList<SessionInfo>( map.values());
  }
  
  /**
   * Returns the session map for the specified link, creating it if necessary.
   * @param link The link.
   * @return Returns a session map.
   */
  private final Map<Integer, SessionInfo> getSessionMap( ILink link)
  {
    Map<Integer, SessionInfo> map = sessions.get( link);
    if ( map == null)
    {
      map = new HashMap<Integer, SessionInfo>();
      sessions.put( link, map);
    }
    return map;
  }

  private Random random;
  private Map<ILink, Map<Integer, SessionInfo>> sessions; 
}
