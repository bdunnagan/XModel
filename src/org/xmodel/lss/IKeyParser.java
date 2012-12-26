package org.xmodel.lss;

public interface IKeyParser<K>
{
  public K extract( byte[] record);
}
