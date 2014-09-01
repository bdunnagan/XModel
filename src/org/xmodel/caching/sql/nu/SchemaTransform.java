package org.xmodel.caching.sql.nu;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.xmodel.IModelObject;
import org.xmodel.ModelAlgorithms;
import org.xmodel.ModelObject;
import org.xmodel.Xlate;
import org.xmodel.compress.ICompressor;
import org.xmodel.compress.TabularCompressor;
import org.xmodel.compress.ZipCompressor;
import org.xmodel.log.SLog;
import org.xmodel.xml.XmlException;
import org.xmodel.xml.XmlIO;

public class SchemaTransform
{
  public SchemaTransform( IModelObject schema)
  {
    this.schema = schema;
  }
  
  public IModelObject getSchema()
  {
    return schema;
  }
  
  public void setShallowImport( boolean shallowImport)
  {
    this.shallowImport = shallowImport;
  }
  
  public IModelObject transformRow( ResultSet resultSet) throws SQLException
  {
    IModelObject tableSchema = schema.getFirstChild( "table");
    IModelObject row = new ModelObject( tableSchema.getType());
    int columnIndex = 0;
    for( IModelObject columnSchema: tableSchema.getChildren())
    {
      importColumn( columnIndex++, columnSchema, row, resultSet);
    }
    return row;
  }
  
  public int getDataType( IModelObject columnSchema)
  {
    int cachedType = Xlate.get( columnSchema, "typeCached", Integer.MAX_VALUE);
    if ( cachedType == Integer.MAX_VALUE)
    {
      String type = Xlate.get( columnSchema, "type", (String)null);
      if ( type != null)
      {
        cachedType = getDataType( type);
        Xlate.set( columnSchema, "typeCached", cachedType);
      }
      else
      {
        SLog.warnf( this, "Column type is undefined for column, %s.%s", 
            columnSchema.getParent().getType(), 
            columnSchema.getType());
      }
    }
    return cachedType;
  }
  
  private void importColumn( int columnIndex, IModelObject columnSchema, IModelObject row, ResultSet rset) throws SQLException
  {
    String mappedName = getMappedName( columnSchema);
    boolean attribute = mappedName.charAt( 0) == '@';

    int cachedType = getDataType( columnSchema);
    switch( cachedType)
    {
      case Types.TINYINT:  
      case Types.SMALLINT: 
      case Types.INTEGER:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getInt( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getInt( columnIndex));
      }
      break;
      
      case Types.BIGINT:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getLong( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getLong( columnIndex));
      }
      break;
      
      case Types.FLOAT:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getFloat( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getFloat( columnIndex));
      }
      break;
      
      case Types.DECIMAL:
      case Types.DOUBLE:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getDouble( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getDouble( columnIndex));
      }
      break;
      
      case Types.CHAR:
      case Types.VARCHAR:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getString( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getString( columnIndex));
      }
      break;
      
      case Types.BLOB:
      case Types.VARBINARY:
      {
        // Is getBytes() correct here??
        importBinary( row, columnSchema, rset.getBytes( columnIndex));
      }
      break;
      
      default:
        break;
    }
  }
  
  private void importBinary( IModelObject row, IModelObject columnSchema, Object value)
  {
    if ( value == null) return;
    
    String column = columnSchema.getType(); 
    
    if ( value instanceof ByteBuffer)
    {
      ByteBuffer buffer = (ByteBuffer)value;
      byte[] bytes = new byte[ buffer.remaining()];
      buffer.get( bytes);
      value = bytes;
    }
    
    if ( value instanceof byte[])
    {
      if ( compressor == null) compressor = new ZipCompressor( new TabularCompressor( false, shallowImport));
      try
      {
        IModelObject superroot = compressor.decompress( new ByteArrayInputStream( (byte[])value));
        ModelAlgorithms.copyAttributes( superroot, row.getFirstChild( column));
        ModelAlgorithms.moveChildren( superroot, row.getFirstChild( column));
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
          ModelAlgorithms.moveChildren( superroot, row.getFirstChild( column));
        }
      }
      catch( XmlException e)
      {
        SLog.errorf( this, "Invalid xml in %s.%s", columnSchema.getParent().getType(), column, e);
      }
    }
  }
  
  private String getMappedName( IModelObject columnSchema)
  {
    String mappedName = Xlate.get( columnSchema, "mappedName", (String)null);
    if ( mappedName != null) return mappedName;
    
    if ( isColumnIndexed( columnSchema))
    {
      mappedName = "@"+columnSchema.getType();
    }
    else
    {
      mappedName = columnSchema.getType();
    }
    
    Xlate.set( columnSchema, "mappedName", mappedName);
    return mappedName;
  }
  
  private boolean isColumnIndexed( IModelObject columnSchema)
  {
    IModelObject indexes = schema.getFirstChild( "indexes");
    for( IModelObject index: indexes.getChildren())
    {
      Object columnName = index.getAttribute( "column");
      if ( columnName != null)
      {
        if ( columnName.equals( columnSchema.getType()))
          return true;
      }
      else
      {
        for( IModelObject indexColumn: index.getChildren())
        {
          columnName = indexColumn.getValue();
          if ( columnName != null)
          {
            if ( columnName.equals( columnSchema.getType()))
              return true;
          }
        }
      }
    }
    return false;
  }
  
  private int getDataType( String type)
  {
    type = type.toUpperCase();
    char first = type.charAt( 0);
    switch( first)
    {
      case 'B':
      {
        if ( type.startsWith( "BIGINT"))
          return Types.BIGINT;
      }
      break;
      
      case 'C':
      {
        if ( type.startsWith( "CHAR"))
          return Types.CHAR;
      }
      break;
      
      case 'D':
      {
        if ( type.startsWith( "DECIMAL"))
          return Types.DECIMAL;
        if ( type.startsWith( "DOUBLE"))
          return Types.DOUBLE;
      }
      break;
      
      case 'F':
      {
        if ( type.startsWith( "FLOAT"))
          return Types.FLOAT;
      }
      break;
      
      case 'I':
      {
        if ( type.startsWith( "INT"))
          return Types.INTEGER;
      }
      break;
      
      case 'L':
      {
        if ( type.startsWith( "LONG"))
          return Types.BIGINT;
      }
      break;
      
      case 'S':
      {
        if ( type.startsWith( "SMALLINT"))
          return Types.SMALLINT;
      }
      break;
      
      case 'T':
      {
        if ( type.startsWith( "TINYINT"))
          return Types.TINYINT;
      }
      break;
      
      case 'V':
      {
        if ( type.startsWith( "VARCHAR"))
          return Types.VARCHAR;
        if ( type.startsWith( "VARBINARY"))
          return Types.VARBINARY;
      }
      break;
      
      default:
      {
        if ( type.contains( "BLOB"))
          return Types.BLOB;
      }
      break;
    }
    
    return Integer.MAX_VALUE;
  }
  
  private IModelObject schema;
  private ICompressor compressor;
  private boolean shallowImport;
}
