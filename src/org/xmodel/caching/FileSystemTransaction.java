package org.xmodel.caching;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.external.IExternalReference;
import org.xmodel.external.ITransaction;

/**
 * An implementation of ITransaction for FileSystemCachingPolicy.
 */
class FileSystemTransaction implements ITransaction
{
  FileSystemTransaction( FileSystemCachingPolicy cachingPolicy)
  {
    this.cachingPolicy = cachingPolicy;
    this.domain = new ArrayList<IExternalReference>();
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#state()
   */
  @Override
  public State state()
  {
    return state;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#track(org.xmodel.external.IExternalReference)
   */
  @Override
  public void track( IExternalReference reference)
  {
    domain.add( reference);
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#lock(int)
   */
  @Override
  public boolean lock( int timeout)
  {
    state = State.locked;
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#unlock()
   */
  @Override
  public void unlock()
  {
    state = State.ready;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#commit()
   */
  @Override
  public boolean commit()
  {
    if ( state != State.locked) return false;
    
    for( IExternalReference reference: domain)
      cachingPolicy.commit( reference);
    
    return true;
  }

  /* (non-Javadoc)
   * @see org.xmodel.external.ITransaction#rollback()
   */
  @Override
  public boolean rollback()
  {
    throw new UnsupportedOperationException();
  }

  private State state;
  private FileSystemCachingPolicy cachingPolicy;
  private List<IExternalReference> domain;
}
