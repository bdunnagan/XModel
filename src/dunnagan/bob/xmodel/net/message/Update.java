/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

import dunnagan.bob.xmodel.IModel;
import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.external.CachingException;
import dunnagan.bob.xmodel.external.ICachingPolicy;
import dunnagan.bob.xmodel.external.IExternalReference;
import dunnagan.bob.xmodel.net.IdentityTable;

/**
 * Refer to messages.xsd for documentation.
 */
public class Update extends Message
{
  public Update()
  {
    super( "update");
  }
  
  /**
   * Perform the update specified in this message.
   * @param model The model to which the update should be dispatched.
   * @param identities The identity table.
   */
  public void performUpdate( IModel model, IdentityTable identities) throws MessageException
  {
    IModelObject result = content.getFirstChild( "result");
    if ( result == null) throw createException( "Result is empty.");
    UpdateRunnable runnable = new UpdateRunnable( identities, result.getID(), result);
    model.dispatch( runnable);
  }
  
  /**
   * (XModel Thread)
   * A runnable for processing an update operation.
   */
  private class UpdateRunnable implements Runnable
  {
    public UpdateRunnable( IdentityTable identities, String id, IModelObject result)
    {
      this.identities = identities;
      this.id = id;
      this.result = result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
      try
      {
        IModelObject element = result.getChild( 0); element.removeFromParent();
        IExternalReference updatee = (IExternalReference)identities.get( id);
        ICachingPolicy cachingPolicy = updatee.getCachingPolicy();
        cachingPolicy.update( updatee, element);
      }
      catch( CachingException e)
      {
        e.printStackTrace( System.err);
      }
    }

    private IdentityTable identities;
    private String id;
    private IModelObject result;
  }
}
