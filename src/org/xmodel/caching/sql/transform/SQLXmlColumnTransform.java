package org.xmodel.caching.sql.transform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.MultiByteArrayInputStream;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.ZipCompressor;
import org.xmodel.log.SLog;
import org.xmodel.xml.IXmlIO.Style;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;

/**
 * An ISQLColumnTransform that for storing XML in a column.  The method of storing the XML depends
 * on the SQL column type and the type of compression chosen by the client.
 */
public class SQLXmlColumnTransform implements ISQLColumnTransform
{
  /**
   * Create an SQLXmlColumnTransform for the table column with the specified name.  T
   * @param metadata The table metadata.
   * @param columnName The name of the table column.
   * @param elementName The name of a row element child that stores the data.
   * @param compressor Null or the compressor to use.
   */
  public SQLXmlColumnTransform( SQLColumnMetaData metadata, String columnName, String elementName, ICompressor compressor)
  {
    this.metadata = metadata;
    this.columnName = columnName;
    this.elementName = elementName;
    this.compressor = compressor;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLColumnTransform#getColumnName()
   */
  @Override
  public String getColumnName()
  {
    return columnName;
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLColumnTransform#importColumn(java.sql.ResultSet, org.xmodel.IModelObject, int)
   */
  @Override
  public void importColumn( ResultSet cursor, IModelObject rowElement, int columnIndex) throws SQLException
  {
    Object value = cursor.getObject( columnIndex);
    if ( value == null) return;
    
    if ( value instanceof byte[])
    {
      if ( compressor == null) compressor = new ZipCompressor( new TabularCompressor());
      try
      {
        IModelObject superroot = compressor.decompress( new ByteArrayInputStream( (byte[])value));
        ModelAlgorithms.moveChildren( superroot, rowElement.getCreateChild( elementName));
      }
      catch( Exception e)
      {
        SLog.exception( this, e);
      }
    }
    else
    {
      try
      {
        String xml = "<superroot>"+value.toString()+"</superroot>";
        if ( xml.length() > 0)
        {
          IModelObject superroot = new XmlIO().read( xml);
          ModelAlgorithms.moveChildren( superroot, rowElement.getCreateChild( elementName));
        }
      }
      catch( XmlException e)
      {
        SLog.exception( this, e);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.xmodel.caching.sql.transform.ISQLColumnTransform#exportColumn(java.sql.PreparedStatement, org.xmodel.IModelObject, int)
   */
  @Override
  public void exportColumn( PreparedStatement statement, IModelObject rowElement, int columnIndex) throws SQLException
  {
    int type = metadata.getColumnType( columnName);
    if ( type == Types.BLOB || type == Types.BINARY || type == Types.VARBINARY || type == Types.LONGVARBINARY)
    {
      if ( compressor == null) throw new IllegalStateException( "XML table column is binary, but compressor is null.");
      
      try
      {
        List<byte[]> buffers = compressor.compress( rowElement.getFirstChild( elementName));
        statement.setBinaryStream( columnIndex, new MultiByteArrayInputStream( buffers));
      }
      catch( IOException e)
      {
        SLog.exception( this, e);
        return;
      }
    }
    else
    {
      StringBuilder sb = new StringBuilder();
      for( IModelObject child: rowElement.getFirstChild( elementName).getChildren())
        sb.append( XmlIO.write( Style.compact, child));
      statement.setString( columnIndex, sb.toString());
    }
  }
  
  private SQLColumnMetaData metadata;
  private String columnName;
  private String elementName;
  private ICompressor compressor;
}
