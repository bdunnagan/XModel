package org.xmodel.caching.sql.nu;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
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
  private enum SQLType
  {
    sqlUndefined,
    sqlTinyint,
    sqlSmallint,
    sqlInt,
    sqlLong,
    sqlBigint,
    sqlFloat,
    sqlDecimal,
    sqlDouble,
    sqlChar,
    sqlVarchar,
    sqlBlob
  }
  
  public SchemaTransform( IModelObject schema)
  {
    this.schema = schema;
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
  
  private void importColumn( int columnIndex, IModelObject columnSchema, IModelObject row, ResultSet rset) throws SQLException
  {
    SQLType cachedType = Xlate.get( columnSchema, "typeOrdinal", SQLType.sqlUndefined);
    if ( cachedType == SQLType.sqlUndefined)
    {
      String type = Xlate.get( columnSchema, "type", (String)null);
      if ( type != null)
      {
        cachedType = getTypeOrdinal( type);
        Xlate.set( columnSchema, "typeOrdinal", cachedType);
      }
      else
      {
        SLog.warnf( this, "Column type is undefined for column, %s.%s", 
            columnSchema.getParent().getType(), 
            columnSchema.getType());
      }
    }
    
    String mappedName = getMappedName( columnSchema);
    boolean attribute = mappedName.charAt( 0) == '@';
    
    switch( cachedType)
    {
      case sqlTinyint:  
      case sqlSmallint: 
      case sqlInt:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getInt( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getInt( columnIndex));
      }
      break;
      
      case sqlLong:
      case sqlBigint:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getLong( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getLong( columnIndex));
      }
      break;
      
      case sqlFloat:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getFloat( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getFloat( columnIndex));
      }
      break;
      
      case sqlDecimal:
      case sqlDouble:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getDouble( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getDouble( columnIndex));
      }
      break;
      
      case sqlChar:
      case sqlVarchar:
      {
        if ( attribute) row.setAttribute( mappedName, rset.getString( columnIndex));
        else Xlate.childSet( row, mappedName, rset.getString( columnIndex));
      }
      break;
      
      case sqlBlob:
      {
        // Is getBytes() correct here??
        importBlob( row, columnSchema, rset.getBytes( columnIndex));
      }
      break;
      
      default:
        break;
    }
  }
  
  private void importBlob( IModelObject row, IModelObject columnSchema, Object value)
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
  
  private SQLType getTypeOrdinal( String type)
  {
    type = type.toUpperCase();
    char first = type.charAt( 0);
    switch( first)
    {
      case 'B':
      {
        if ( type.startsWith( "BIGINT"))
          return SQLType.sqlBigint;
      }
      break;
      
      case 'C':
      {
        if ( type.startsWith( "CHAR"))
          return SQLType.sqlChar;
      }
      break;
      
      case 'D':
      {
        if ( type.startsWith( "DECIMAL"))
          return SQLType.sqlDecimal;
        if ( type.startsWith( "DOUBLE"))
          return SQLType.sqlDouble;
      }
      break;
      
      case 'F':
      {
        if ( type.startsWith( "FLOAT"))
          return SQLType.sqlFloat;
      }
      break;
      
      case 'I':
      {
        if ( type.startsWith( "INT"))
          return SQLType.sqlInt;
      }
      break;
      
      case 'L':
      {
        if ( type.startsWith( "LONG"))
          return SQLType.sqlLong;
      }
      break;
      
      case 'S':
      {
        if ( type.startsWith( "SMALLINT"))
          return SQLType.sqlSmallint;
      }
      break;
      
      case 'T':
      {
        if ( type.startsWith( "TINYINT"))
          return SQLType.sqlTinyint;
      }
      break;
      
      case 'V':
      {
        if ( type.startsWith( "VARCHAR"))
          return SQLType.sqlVarchar;
      }
      break;
      
      default:
      {
        if ( type.contains( "BLOB"))
          return SQLType.sqlBlob;
      }
      break;
    }
    
    return SQLType.sqlUndefined;
  }
  
  private IModelObject schema;
  private ICompressor compressor;
  private boolean shallowImport;
}
