package org.xmodel.concurrent;

import org.xmodel.Element;
import org.xmodel.IModelObject;
import org.xmodel.log.SLog;
import org.xmodel.xaction.IXAction;
import org.xmodel.xaction.XActionDocument;
import org.xmodel.xml.XmlIO;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.StatefulContext;

public class ConcurrencyTest
{
  public static void go( IContext context) throws Exception
  {
    IModelObject scriptNode = new XmlIO().read( ConcurrencyTest.class.getResourceAsStream( "test.xml"));
    XActionDocument doc = new XActionDocument( scriptNode);
    IXAction script = doc.createScript();
    script.run( context);
  }
  
  public static void main( String[] args) throws Exception
  {
    IModelObject shared = new Element( "shared");
    final IContext context = new StatefulContext( new ReadWriteElement( shared));
    
    for( int i=0; i<10; i++)
    {
      Thread thread = new Thread() {
        public void run()
        {
          try
          {
            go( new StatefulContext( context));
          }
          catch( Exception e)
          {
            SLog.exception( this, e);
          }
        }
      };
      
      thread.setDaemon( false);
      thread.start();
    }
  }
}
