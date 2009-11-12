/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * NumberCheck.java
 * 
 * Copyright 2009 Robert Arvin Dunnagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmodel.xsd.check;

import java.math.BigDecimal;
import org.xmodel.IModelObject;
import org.xmodel.Xlate;


public class NumberCheck extends AbstractCheck
{
  public NumberCheck( IModelObject schemaLocus)
  {
    super( schemaLocus);
    floatNode = schemaLocus.getFirstChild( "float");
    integerNode = schemaLocus.getFirstChild( "integer");
    minNode = schemaLocus.getFirstChild( "min");
    maxNode = schemaLocus.getFirstChild( "max");
    minString = Xlate.get( minNode, "");
    maxString = Xlate.get( maxNode, "");
  }

  /* (non-Javadoc)
   * @see org.xmodel.xsd.nu.ICheck#validateImpl(org.xmodel.IModelObject)
   */
  protected boolean validateImpl( IModelObject documentLocus)
  {
    String string = Xlate.get( documentLocus, "");
    if ( integerNode != null && string.contains( ".")) return false;
    
    try
    {
      if ( minNode != null)
      {
        if ( minString.length() >= 18)
        {
          BigDecimal minValue = new BigDecimal( minString);
          BigDecimal value = new BigDecimal( string);
          if ( Xlate.get( minNode, "exclusive", false))
          {
            if ( value.compareTo( minValue) <= 0) return false;
          }
          else
          {
            if ( value.compareTo( minValue) < 0) return false;
          }
        }
        else
        {
          if ( floatNode != null)
          {
            double minValue = Double.valueOf( minString);
            double value = Double.valueOf( string);
            if ( Xlate.get( minNode, "exclusive", false))
            {
              if ( value <= minValue) return false;
            }
            else
            {
              if ( value < minValue) return false;
            }
          }
          else
          {
            long minValue = Long.valueOf( minString);
            long value = Long.valueOf( string);
            if ( Xlate.get( minNode, "exclusive", false))
            {
              if ( value <= minValue) return false;
            }
            else
            {
              if ( value < minValue) return false;
            }
          }
        }
      }
      
      if ( maxNode != null)
      {
        if ( maxString.length() >= 18)
        {
          BigDecimal maxValue = new BigDecimal( maxString);
          BigDecimal value = new BigDecimal( string);
          if ( Xlate.get( maxNode, "exclusive", false))
          {
            if ( value.compareTo( maxValue) >= 0) return false;
          }
          else
          {
            if ( value.compareTo( maxValue) > 0) return false;
          }
        }
        else
        {
          if ( floatNode != null)
          {
            double maxValue = Double.valueOf( maxString);
            double value = Double.valueOf( string);
            if ( Xlate.get( maxNode, "exclusive", false))
            {
              if ( value >= maxValue) return false;
            }
            else
            {
              if ( value > maxValue) return false;
            }
          }
          else
          {
            long maxValue = Long.valueOf( maxString);
            long value = Long.valueOf( string);
            if ( Xlate.get( maxNode, "exclusive", false))
            {
              if ( value >= maxValue) return false;
            }
            else
            {
              if ( value > maxValue) return false;
            }
          }
        }
      }
    }
    catch( NumberFormatException e)
    {
      return false;
    }
    
    return true;
  }

  IModelObject floatNode;
  IModelObject integerNode;
  IModelObject minNode;
  IModelObject maxNode;
  String minString;
  String maxString;
}
