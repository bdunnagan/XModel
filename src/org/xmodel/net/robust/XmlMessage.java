/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.net.robust;

import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;

/**
 * A convenience class for creating simple XML messages.
 */
public class XmlMessage
{
  /**
   * Create a simple message with an ordered list of unnamed values.
   * @param type The message type.
   * @param values The ordered list of values.
   * @return Returns the message object.
   */
  public static IModelObject createSimple( String type, String... values)
  {
    ModelObject message = new ModelObject( type);
    if ( values.length > 1)
    {
      IModelObject params = message.getCreateChild( "params");
      for( String value: values)
      {
        ModelObject param = new ModelObject( "param");
        param.setValue( value);
        params.addChild( param);
      }
    }
    else if ( values.length > 0)
    {
      message.setValue( values[ 0]);
    }
    else
    {
      Xlate.set( message, "empty", true);
    }
    return message;
  }
  
  /**
   * Parses a message created with the <code>createSimple</code> method.
   * @param message The simple message.
   * @return Returns the ordered list of values in the message.
   */
  public static String[] parseSimple( IModelObject message)
  {
    if ( !Xlate.get( message, "empty", false))
    {
      IModelObject params = message.getFirstChild( "params");
      if ( params == null) 
      {
        return new String[] { Xlate.get( message, "")};
      }
      else
      {
        List<IModelObject> children = params.getChildren( "param");
        String[] values = new String[ children.size()];
        for( int i=0; i<values.length; i++) 
          values[ i] = Xlate.get( children.get( i), "");
        return values;
      }
    }
    return new String[ 0];
  }
}
