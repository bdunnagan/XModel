/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.net.message;

import java.util.ArrayList;
import java.util.List;

import dunnagan.bob.xmodel.IModelObject;
import dunnagan.bob.xmodel.ModelObject;
import dunnagan.bob.xmodel.Xlate;
import dunnagan.bob.xmodel.net.IdentityTable;

/**
 * Refer to messages.xsd for documentation.
 */
public class ElementRequest extends Request
{
  public ElementRequest( String type)
  {
    super( type);
  }
  
  /**
   * Add the server id of an element for which updates are being requested.
   * @param id The server id of the element.
   */
  public void addRemoteID( String id)
  {
    IModelObject entry = new ModelObject( "id");
    entry.setValue( id);
    content.addChild( entry);
  }
  
  /**
   * Returns the elements from the identity table matching the ids of this message.
   * @param request The update request to be parsed.
   * @param identities The identities table to use to resolve server ids.
   * @return Returns the elements from the identity table matching the ids of this message.
   */
  public static List<IModelObject> getElements( IModelObject request, IdentityTable identities) throws MessageException
  {
    List<IModelObject> result = new ArrayList<IModelObject>();
    for( IModelObject entry: request.getChildren())
    {
      String id = Xlate.get( entry, "");
      IModelObject element = identities.get( id);
      if ( element == null) throw new MessageException( "Element with server id not found: "+id);
      result.add( element);
    }
    return result;
  }
}
