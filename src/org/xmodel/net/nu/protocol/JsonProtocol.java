package org.xmodel.net.nu.protocol;

import org.xmodel.IModelObject;
import org.xmodel.net.nu.IProtocol;
import org.xmodel.util.JsonParser;

public class JsonProtocol implements IProtocol
{
  public JsonProtocol()
  {
    parser = new JsonParser();
  }

  @Override
  public byte[] encode( IModelObject message)
  {
    // TODO:
    throw new UnsupportedOperationException();
  }

  @Override
  public IModelObject decode( byte[] message, int offset, int length)
  {
    return null;
  }

  private JsonParser parser;
}
