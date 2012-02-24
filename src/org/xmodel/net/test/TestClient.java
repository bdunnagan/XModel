package org.xmodel.net.test;

import org.xmodel.IModelObject;
import org.xmodel.ModelObject;
import org.xmodel.external.ExternalReference;
import org.xmodel.net.NetworkCachingPolicy;
import org.xmodel.xpath.expression.StatefulContext;

public class TestClient
{
  public TestClient()
  {
  }
  
  public static IModelObject createNetworkReference()
  {
    ModelObject annotation = new ModelObject( "annotation");
    annotation.setAttribute( "host", "127.0.0.1");
    annotation.setAttribute( "port", 9000);
    annotation.setAttribute( "timeout", Integer.MAX_VALUE);
    annotation.setAttribute( "query", ".");
    
    if ( cachingPolicy == null)
    {
      cachingPolicy = new NetworkCachingPolicy();
      cachingPolicy.configure( new StatefulContext(), annotation);
    }
    
    ExternalReference clientModel = new ExternalReference( "client");
    clientModel.setCachingPolicy( cachingPolicy);
    clientModel.setDirty( true);
    
    return clientModel;
  }
  
  private static NetworkCachingPolicy cachingPolicy;
}
