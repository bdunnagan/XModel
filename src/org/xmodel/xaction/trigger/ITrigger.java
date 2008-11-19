package org.xmodel.xaction.trigger;

import org.apache.log4j.Logger;
import org.xmodel.logging.Log;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xpath.expression.IContext;

/**
 * An interface for triggers which observe a model an execute a script autonomously 
 * when certain critera are met. 
 */
public interface ITrigger
{
  /**
   * Configure the trigger from the specified document.
   * @param document The document.
   */
  public void configure( XActionDocument document);
  
  /**
   * Returns the document for this trigger.
   * @return Returns the document for this trigger.
   */
  public XActionDocument getDocument();
  
  /**
   * Activate the trigger in the specified context.
   * @param context The context.
   */
  public void activate( IContext context);
  
  /**
   * Deactivate the trigger in the specified context.
   * @param context The context.
   */
  public void deactivate( IContext context);
  
  /**
   * Logging for the trigger package.
   */
  public static Logger log = Log.getLog( "org.xmodel.xaction.trigger");
}
