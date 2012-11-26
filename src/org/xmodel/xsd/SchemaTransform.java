/*
 * JAHM - Java Advanced Hierarchical Model 
 * 
 * SchemaTransform.java
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
package org.xmodel.xsd;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmodel.*;
import org.xmodel.xpath.XPath;
import org.xmodel.xpath.expression.IExpression;


/**
 * A transform between XSD and the XModel simplified schema format represented in schema.xsd. The simplified 
 * schema is intended to make it easier to interrogate the schema using XPath expressions.
 */
public class SchemaTransform
{
  public SchemaTransform()
  {
    this( false);
  }
  
  /**
   * Create a SchemaTransform which optionally transforms xs:sequence as xs:all.
   * @param unordered True if xs:sequence should be transformed as xs:all.
   */
  public SchemaTransform( boolean unordered)
  {
    this.unordered = unordered;
    buildPrimitives();
  }
  
  /**
   * Transform the schema into a more usable form. This method assumes that includes and imports
   * have already been processed.
   * @param schema The schema root.
   * @return Returns the transformed schema.
   */
  public INode transform( INode schema) throws SchemaException
  {
    INode root = new ModelObject( "schema");

    // transform global complex types
    List<INode> types = transformComplexTypes( schema);
    for( INode type: types) root.addChild( type);
    
    // transform global elements
    Map<String, INode> globalElements = new HashMap<String, INode>();
    List<INode> elements = transformElements( schema);
    for( INode element: elements) 
    {
      // complex type references are added to map below
      if ( !element.getType().startsWith( "reference:type"))
        globalElements.put( Xlate.get( element, "name", ""), element);
      root.addChild( element);
    }
    
    // resolve element references
    for( INode reference: typeReferenceExpr.query( root, null))
    {
      INode parent = reference.getParent();
      if ( parent != null)
      {
        String type = Xlate.get( reference, "type", "");
        INode prototype = globalComplexTypes.get( type);
        
        // type string may contain an erroneous prefix if the xs:element is in the default namespace
        if ( prototype == null)
        {
          String stripped = type.replaceAll( "^[^:]+:", "");
          prototype = globalComplexTypes.get( stripped);
        }
        
        INode element = createElementFromPrototype( prototype);
        ModelAlgorithms.copyAttributes( reference, element);
        reference.removeFromParent();
        parent.addChild( element);
        
        // add to global elements map and remove global attribute
        String name = Xlate.get( element, "name", "");
        globalElements.put( name, element);
      }
    }
    
    // resolve element references
    for( INode reference: elementReferenceExpr.query( root, null))
    {
      INode parent = reference.getParent();
      if ( parent != null)
      {
        String name = Xlate.get( reference, "name", "");
        INode element = globalElements.get( name);
        if ( element == null) throw new SchemaException( "Unable to resolve global element: "+name);
        reference.removeFromParent();
        parent.addChild( new Reference( element));
      }
    }
    
    return root;
  }
  
  /**
   * Transform the specified simple type declaration.
   * @param schema The schema.
   * @param simpleType The simple type.
   * @return Returns the transformed simple type.
   */
  private INode transformSimpleType( INode schema, INode simpleType) throws SchemaException
  {
    // only handle restriction for now (ignore list and union)
    INode restriction = simpleType.getFirstChild( "xs:restriction");
    
    // lookup base type
    String base = Xlate.get( restriction, "base", "");
    INode type = lookupGlobalType( schema, base);
    if ( type == null)
    {
      // create new type from scratch
      type = new ModelObject( "type");
    }
    else
    {
      // create type based on existing type
      type = type.cloneTree();
    }
    
    // handle restriction
    transformRestrictionConstraint( schema, type, restriction);
    return type;
  }
  
  /**
   * Transform the global complex types declarations.
   * @param schema The schema.
   * @return Returns the transformed types.
   */
  private List<INode> transformComplexTypes( INode schema) throws SchemaException
  {
    List<INode> result = new ArrayList<INode>();
    List<INode> complexTypes = schema.getChildren( "xs:complexType");
    for( INode complexType: complexTypes) result.add( transformComplexType( schema, complexType));
    return result;
  }
  
  /**
   * Transform the specified complex type declaration.
   * @param schema The schema.
   * @param complexType The complex type.
   * @return Returns the transformed complex type.
   */
  private INode transformComplexType( INode schema, INode complexType) throws SchemaException
  {
    // see if complex type is already defined
    String type = Xlate.get( complexType, "name", (String)null);
    if ( type != null)
    {
      INode resolved = globalComplexTypes.get( type);
      if ( resolved != null) return resolved;
    }
    
    // create element definition form complex type
    INode element = new ModelObject( "element");
    element.setAttribute( "type", type);
    
    // add to map here to prevent endless looping
    if ( type != null) globalComplexTypes.put( type, element);
    
    // transform embedded descriptors 
    transformEmbedded( schema, element, complexType);
    
    // transform simple content (reference to global simple type)
    INode simpleContent = complexType.getFirstChild( "xs:simpleContent");
    if ( simpleContent != null) transformContent( schema, element, simpleContent);
    
    // transform complex content
    INode complexContent = complexType.getFirstChild( "xs:complexContent");
    if ( complexContent != null) transformContent( schema, element, complexContent);
    
    return element;
  }
  
  /**
   * Transform the specified sac and store the children and constraints the specified element.
   * @param schema The schema.
   * @param sac The sac on the specified element.
   * @param element The transformed element.
   * @return Returns the constraint created for the sac.
   */
  private INode transformSac( INode schema, INode sac, INode element) throws SchemaException
  {    
    if ( sac.isType( "xs:group")) 
      return transformSac( schema, getSacFromGroup( schema, sac), element);
    
    INode children = element.getCreateChild( "children");
    INode constraint = null;
    if ( sac.isType( "xs:sequence")) 
      constraint = new ModelObject( unordered? "set": "list");
    else if ( sac.isType( "xs:choice")) 
      constraint = new ModelObject( "choice");
    else if ( sac.isType( "xs:all")) 
      constraint = new ModelObject( "set");
    else if ( sac.isType( "xs:any"))
      constraint = new ModelObject( "any");
    else if ( sac.isType( "xs:annotation")) { return null; }
    else if ( sac.isType( "xs:documentation")) { return null; }
    else
      throw new SchemaException( "Element is not a sac: "+sac);
    
    // set occurrence
    String min = Xlate.get( sac, "minOccurs", (String)null);
    if ( min != null) constraint.setAttribute( "min", min);
    String max = Xlate.get( sac, "maxOccurs", (String)null);
    if ( max != null) constraint.setAttribute( "max", max);
    
    // process children of sac
    List<INode> sacChildren = sac.getChildren();
    for( INode sacChild: sacChildren)
    {
      if ( sacChild.isType( "xs:element"))
      {
        // transform element and add to parent
        INode transformed = transformElement( schema, sacChild);
        children.addChild( transformed);
        
        // add element to constraint
        String name = Xlate.get( sacChild, "name", (String)null);
        if ( name == null) name = Xlate.get( sacChild, "ref", (String)null);
        INode reference = new ModelObject( "child");
        reference.setValue( name);
        constraint.addChild( reference);
        
        // set minimum and maximum occurences
        min = Xlate.get( sacChild, "minOccurs", (String)null);
        if ( min != null) reference.setAttribute( "min", min);
        max = Xlate.get( sacChild, "maxOccurs", (String)null);
        if ( max != null) reference.setAttribute( "max", max);
      }
      else
      {
        INode transformed = transformSac( schema, sacChild, element);
        if ( transformed != null) 
        {
          // flatten like constraints (ex: list with embedded list)
          if ( transformed.getType().equals( constraint.getType()))
          {
            for( INode child: transformed.getChildren().toArray( new INode[ 0]))
              constraint.addChild( child);
          }
          else
          {
            constraint.addChild( transformed);
          }
        }
      }
    }
    
    return constraint;
  }

  /**
   * Transform the global element declarations.
   * @param schema The schema.
   * @return Returns the transformed global elements.
   */
  private List<INode> transformElements( INode schema) throws SchemaException
  {
    List<INode> result = new ArrayList<INode>();
    List<INode> elements = schema.getChildren( "xs:element");
    for( INode element: elements) 
    {
      INode transformed = transformElement( schema, element);
      Xlate.set( transformed, "global", true);
      result.add( transformed);
    }
    return result;
  }

  /**
   * Transform the specified element declaration.
   * @param schema The schema.
   * @param element The element.
   * @return Returns the transformed element.
   */
  private INode transformElement( INode schema, INode element) throws SchemaException
  {
    // reference to a global element
    String ref = Xlate.get( element, "ref", (String)null);
    if ( ref != null) 
    {
      INode elementReference = new ModelObject( "reference:element");
      elementReference.setAttribute( "name", ref);
      return elementReference;
    }

    // get element name
    String name = Xlate.get( element, "name", (String)null);
    
    // reference to global builtin, simple or complex type with optional default value
    String type = Xlate.get( element, "type", (String)null);
    if ( type != null)
    {
      // lookup complex type
      INode globalElement = lookupGlobalComplexType( schema, type);
      if ( globalElement != null) 
      {
        INode elementReference = new ModelObject( "reference:type");
        elementReference.setAttribute( "name", name);
        elementReference.setAttribute( "type", type);
        return elementReference;
      }

      // lookup simple type
      INode globalType = lookupGlobalType( schema, type);
      if ( globalType == null) throw new SchemaException( "Unable to resolve type: "+type);
      
      // create element
      INode result = new ModelObject( "element");
      result.setAttribute( "name", name);
      INode value = result.getCreateChild( "value");
      value.addChild( globalType.cloneTree());
      
      // default
      String defaultString = Xlate.get( element, "default", (String)null);
      if ( defaultString != null)
      {
        INode dfault = result.getCreateChild( "default");
        dfault.setValue( defaultString);
      }
      
      return result;
    }

    // transform simple type
    INode simpleType = element.getFirstChild( "xs:simpleType");
    if ( simpleType != null)
    {
      INode transformed = transformSimpleType( schema, simpleType);
      INode result = new ModelObject( "element");
      result.setAttribute( "name", name);
      
      // type
      INode value = result.getCreateChild( "value");
      value.addChild( transformed);
      
      // default
      String defaultString = Xlate.get( element, "default", (String)null);
      if ( defaultString != null)
      {
        INode dfault = value.getCreateChild( "default");
        dfault.setValue( defaultString);
      }
      
      return result;
    }
    
    // transform complex type
    INode complexType = element.getFirstChild( "xs:complexType");
    if ( complexType != null)
    {
      INode transformed = transformComplexType( schema, complexType);
      transformed.setAttribute( "name", name);
      
      // default override (not handled by XmlSpy)
      String defaultString = Xlate.get( element, "default", (String)null);
      if ( defaultString != null)
      {
        INode value = transformed.getFirstChild( "value");
        if ( value == null)
          throw new SchemaException(
            "Attempt to override default value when value is not defined: "+element);
        
        INode dfault = value.getCreateChild( "default");
        dfault.setValue( defaultString);
      }
      
      return transformed;
    }

    // empty element
    INode result = new ModelObject( "element");
    result.setAttribute( "name", name);
    return result;
  }
  
  /**
   * Create an element from the specified element prototype (transformed complex type). The element will
   * have references to all of the children of the transformed type, but the element root will be a new
   * object which can have its own attributes.
   * @param prototype The element prototype.
   * @return Returns the new element.
   */
  private INode createElementFromPrototype( INode prototype)
  {
    INode element = new ModelObject( "element");
    List<INode> children = prototype.getChildren();
    for( INode child: children)
      element.addChild( new Reference( child));
    return element;
  }
  
  /**
   * Returns the transformed attribute.
   * @param schema The schema.
   * @param attribute The attribute.
   * @return Returns the transformed attribute.
   */
  private INode transformAttribute( INode schema, INode attribute) throws SchemaException
  {
    String ref = Xlate.get( attribute, "ref", (String)null);
    if ( ref == null)
    {
      INode result = new ModelObject( "attribute");
      
      String name = Xlate.get( attribute, "name", (String)null);
      result.setAttribute( "name", name);
      
      String dfault = Xlate.get( attribute, "default", (String)null);
      result.setAttribute( "default", dfault);
      
      String use = Xlate.get( attribute, "use", "optional");
      result.setAttribute( "use", use);

      String type = Xlate.get( attribute, "type", (String)null);
      if ( type != null)
      {
        INode globalType = lookupGlobalType( schema, type);
        result.addChild( globalType.cloneTree());
        return result;
      }
      
      INode simpleType = attribute.getFirstChild( "xs:simpleType");
      if ( simpleType != null) result.addChild( transformSimpleType( schema, simpleType));
      
      return result;
    }
    else
    {
      INode globalAttribute = globalAttributes.get( ref);
      if ( globalAttribute == null) throw new SchemaException( "Global attribute not found: "+ref);
      return globalAttribute.cloneTree();
    }
  }
  
  /**
   * Returns the sac embedded in the specified global group.
   * @param schema The schema.
   * @param The group.
   * @return Returns the sac embedded in the specified global group.
   */
  private INode getSacFromGroup( INode schema, INode group) throws SchemaException
  {
    // embedded
    String name = Xlate.get( group, "name", (String)null);
    if ( name != null) return sacExpr.queryFirst( group);
    
    // reference
    String ref = Xlate.get( group, "ref", (String)null);
    if ( ref != null)
    {
      groupFinder.setVariable( "ref", ref);
      INode resolved = groupFinder.queryFirst( schema);
      if ( resolved == null) throw new SchemaException( "Global group is undefined: "+name);
      return getSacFromGroup( schema, resolved);
    }
    
    throw new SchemaException(
      "Group does not define a name or reference another group: "+group);
  }
  
  /**
   * Returns a global transformed type (simple or builtin type).
   * @param schema The schema.
   * @param name The type name.
   * @return Returns a global transformed type.
   */
  private INode lookupGlobalType( INode schema, String name) throws SchemaException
  {
    // primitive
    INode primitive = primitives.get( name);
    if ( primitive != null) return primitive;
    
    // already transformed global type
    INode type = globalSimpleTypes.get( name);
    if ( type != null) return type;
    
    // create transformed global simple type
    simpleTypeFinder.setVariable( "name", name);
    INode simpleType = simpleTypeFinder.queryFirst( schema);
    if ( simpleType != null)
    {
      type = transformSimpleType( schema, simpleType);
      type.setAttribute( "name", name);
      globalSimpleTypes.put( name, type);
    }
    return type;
  }
  
  /**
   * Find or create the element with the specified type and return a reference to it.
   * @param schema The schema.
   * @param type The type of the element.
   * @return Returns a reference to the transformed element.
   */
  private Reference lookupGlobalComplexType( INode schema, String type) throws SchemaException
  {
    // see if type has already been transformed
    INode element = globalComplexTypes.get( type);
    if ( element != null) return new Reference( element);

    // type string may contain an erroneous prefix if the xs:element is in the default namespace
    String stripped = type.replaceAll( "^[^:]+:", "");
    element = globalComplexTypes.get( stripped);
    if ( element != null) return new Reference( element);
    
    // find untransformed element
    complexTypeFinder.setVariable( "name", type);
    INode untransformed = complexTypeFinder.queryFirst( schema);
    
    // type string may contain an erroneous prefix if the xs:element is in the default namespace
    if ( untransformed == null)
    {
      complexTypeFinder.setVariable( "name", stripped);
      untransformed = complexTypeFinder.queryFirst( schema);
    }
    
    if ( untransformed != null)
    {
      element = transformComplexType( schema, untransformed);
      return new Reference( element);
    }
    
    return null;
  }
  
  /**
   * Transform the specified simple or complex content and add it to the given element.
   * @param schema The schema.
   * @param element The transformed element.
   * @param content The untransformed content.
   */
  private void transformContent( INode schema, INode element, INode content) throws SchemaException
  {
    // restriction
    INode restriction = content.getFirstChild( "xs:restriction");
    if ( restriction != null) transformRestriction( schema, element, restriction);
    
    // extension
    INode extension = content.getFirstChild( "xs:extension");
    if ( extension != null) transformExtension( schema, element, extension);
  }
  
  /**
   * Transform the specified restriction and apply to the given element.
   * @param schema The schema.
   * @param element The transformed element.
   * @param restriction The untransformed restriction.
   */
  private void transformRestriction( INode schema, INode element, INode restriction) throws SchemaException
  {
    // NOTE: global complex and simple types do not have separate namespaces here
    String base = Xlate.get( restriction, "base", "");
    
    // handle global simple type
    INode type = lookupGlobalType( schema, base);
    if ( type != null) 
    {
      INode value = element.getCreateChild( "value");
      INode parent = value.getCreateChild( "type");
      transformRestrictionConstraint( schema, type, restriction);
      parent.addChild( type.cloneTree());
    }
    else
    {
      // handle global complex type (element)
      INode reference = lookupGlobalComplexType( schema, base);
      if ( reference != null)
      {
        INode value = reference.getFirstChild( "value");
        if ( value != null) element.addChild( new Reference( value));
        INode attributes = reference.getFirstChild( "attributes");
        if ( attributes != null) element.addChild( new Reference( attributes));
      }
      else
      {
        // handle restriction constraint (mutually exclusive with global simple type above)
        INode value = element.getCreateChild( "value");
        INode parent = value.getCreateChild( "type");
        transformRestrictionConstraint( schema, parent, restriction);
      }
    }
  }
  
  /**
   * Transform the constraint of the specified restriction on the specified type.
   * @param schema The schema.
   * @param type The base transformed type.
   * @param restriction The untransformed restriction.
   */
  private void transformRestrictionConstraint( INode schema, INode type, INode restriction) 
  throws SchemaException
  {
    // process base type
    String base = Xlate.get( restriction, "base", "");
    
    // process restriction
    if ( isString( base))
    {
      INode stringConstraint = type.getCreateChild( "string");
      if ( restriction == null) return;
      
      // length restriction
      String minLength = Xlate.get( restriction.getFirstChild( "xs:minLength"), "value", (String)null);
      if ( minLength != null) stringConstraint.getCreateChild( "min").setValue( minLength);
      
      String maxLength = Xlate.get( restriction.getFirstChild( "xs:maxLength"), "value", (String)null);
      if ( maxLength != null) stringConstraint.getCreateChild( "max").setValue( maxLength);

      String length = Xlate.get( restriction.getFirstChild( "xs:length"), "value", (String)null);
      if ( length != null) 
      {
        stringConstraint.getCreateChild( "min").setValue( length);
        stringConstraint.getCreateChild( "max").setValue( length);
      }
    }
    else if ( isNumber( base))
    {
      INode numberConstraint = type.getCreateChild( "number");
      if ( restriction == null) return;
      
      String minInclusive = Xlate.get( restriction.getFirstChild( "xs:minInclusive"), "value", (String)null);
      if ( minInclusive != null) 
      {
        INode min = numberConstraint.getCreateChild( "min");
        min.setValue( minInclusive);
      }
      
      String minExclusive = Xlate.get( restriction.getFirstChild( "xs:minExclusive"), "value", (String)null);
      if ( minExclusive != null) 
      {
        INode min = numberConstraint.getCreateChild( "min");
        min.setAttribute( "exclusive", "true");
        min.setValue( minExclusive);
      }
      
      String maxInclusive = Xlate.get( restriction.getFirstChild( "xs:maxInclusive"), "value", (String)null);
      if ( maxInclusive != null) 
      {
        INode max = numberConstraint.getCreateChild( "max");
        max.setValue( maxInclusive);
      }
      
      String maxExclusive = Xlate.get( restriction.getFirstChild( "xs:maxExclusive"), "value", (String)null);
      if ( maxExclusive != null) 
      {
        INode max = numberConstraint.getCreateChild( "max");
        max.setAttribute( "exclusive", "true");
        max.setValue( maxExclusive);
      }
      
      String totalDigits = Xlate.get( restriction.getFirstChild( "xs:totalDigits"), "value", (String)null);
      if ( totalDigits != null)
      {
        numberConstraint.getCreateChild( "integer").setValue( totalDigits);
      }
    }
    else if ( isBoolean( base))
    {
      type.getCreateChild( "boolean");
      if ( restriction == null) return;
    }
    
    // enumeration
    List<INode> enumerations = restriction.getChildren( "xs:enumeration");
    if ( enumerations.size() > 0)
    {
      INode enumConstraint = type.getCreateChild( "enum");
      for( INode enumeration: enumerations)
      {
        INode value = new ModelObject( "value");
        value.setValue( Xlate.get( enumeration, "value", ""));
        enumConstraint.addChild( value);
      }
    }
    
    // pattern
    INode pattern = restriction.getFirstChild( "xs:pattern");
    if ( pattern != null)
    {
      INode patternConstraint = type.getCreateChild( "pattern");
      patternConstraint.setValue( Xlate.get( pattern, "value", ""));
    }
  }
  
  /**
   * Transform the specified extension and apply to the given element.
   * @param schema The schema.
   * @param element The transformed element.
   * @param extension The untransformed extension.
   */
  private void transformExtension( INode schema, INode element, INode extension) throws SchemaException
  {
    String base = Xlate.get( extension, "base", "");
    
    // simple type
    INode type = lookupGlobalType( schema, base);
    if ( type != null)
    {
      INode value = element.getCreateChild( "value");
      value.addChild( type.cloneTree());
      
      // set default
      INode dfaultAttribute = extensionDefaultExpr.queryFirst( extension);
      String dfault = Xlate.get( dfaultAttribute, (String)null);
      if ( dfault != null) Xlate.set( value.getCreateChild( "default"), dfault);
      
      // add extension attributes
      transformEmbedded( schema, element, extension);
    }
    else
    {
      // complex type
      INode prototype = lookupGlobalComplexType( schema, base);
      if ( prototype != null)
      {
        // clone attributes
        INode attributes = prototype.getFirstChild( "attributes");
        if ( attributes != null) element.addChild( attributes.cloneTree());
        
        // clone children
        INode children = prototype.getFirstChild( "children");
        if ( children != null) element.addChild( children.cloneTree());

        // transform extension first
        transformEmbedded( schema, element, extension);

        // combine base and extension constraints
        INode baseConstraint = prototype.getFirstChild( "constraint");
        if ( baseConstraint != null)
        {
          baseConstraint = baseConstraint.cloneTree();
          INode baseSac = baseConstraint.getChild( 0);
          INode constraint = element.getFirstChild( "constraint");
          if ( constraint != null)
          {
            INode extensionSac = constraint.getChild( 0);
            if ( !extensionSac.getType().equals( baseSac.getType()))
              throw new SchemaException(
                "Extension model must match base model: extension="+element+", base="+base);
            
            // prepend base constraint sac children
            INode[] baseSacChildren = baseSac.getChildren().toArray( new INode[ 0]);
            for( int i=0; i<baseSacChildren.length; i++)
              extensionSac.addChild( baseSacChildren[ i], i);
          }
          else
          {
            element.addChild( baseConstraint);
          }
        }
        else
        {
          INode constraint = element.getFirstChild( "constraint");
          if ( constraint != null)
          {
            INode extensionSac = element.getFirstChild( "constraint").getChild( 0);
            element.getCreateChild( "constraint").addChild( extensionSac);
          }
        }
      }
    }
  }
  
  /**
   * Transform an xs:group, xs:sequence, xs:all, xs:choice, xs:attribute, xs:attributeGroup or xs:anyAttribute 
   * and apply the result to the specified element.  The parent argument is the parent with one of the previous
   * embedded descriptors.
   * @param schema The schema.
   * @param element The transformed element.
   * @param parent The parent of the untransformed embedded descriptor.
   */
  private void transformEmbedded( INode schema, INode element, INode parent) throws SchemaException
  {
    // transform embedded sac
    INode sac = sacExpr.queryFirst( parent);
    if ( sac != null)
    {
      INode constraint = transformSac( schema, sac, element);
      if ( constraint != null) element.getCreateChild( "constraint").addChild( constraint);
    }
    
    // transform embedded attributes
    INode attributes = null;
    List<INode> xsAttributes = parent.getChildren( "xs:attribute");
    for( INode xsAttribute: xsAttributes)
    {
      if ( attributes == null) attributes = element.getCreateChild( "attributes");
      INode attribute = transformAttribute( schema, xsAttribute);
      if ( attribute != null) attributes.addChild( attribute);
    }
    
    // transform embedded attribute group
    // (do later)
    
    // transform any attributes
    // (do later)
  }
  
  /**
   * Returns true if the specified primitive type is a string.
   * @param type A primitive type such as xs:string.
   * @return Returns true if the specified primitive type is a string.
   */
  private boolean isString( String type)
  {
    return (type.equals( "xs:string") || type.equals( "xs:token") || type.equals( "xs:normalizedString"));
  }
  
  /**
   * Returns true if the specified primitive type is numeric.
   * @param type A primitive type such as xs:int.
   * @return Returns true if the specified primitive type is numeric.
   */
  private boolean isNumber( String type)
  {
    return (type.equals( "xs:int") || 
            type.equals( "xs:long") ||
            type.equals( "xs:short") ||
            type.contains( "nteger") || 
            type.contains( "decimal") || 
            type.startsWith( "unsigned"));
  }
  
  /**
   * Returns true if the specified primitive type is boolean.
   * @param type A primitive type such as xs:boolean.
   * @return Returns true if the specified primitive type is boolean.
   */
  private boolean isBoolean( String type)
  {
    return type.equals( "xs:boolean");
  }
  
  /**
   * Build the primitive types.
   */
  private void buildPrimitives()
  {
    // xs:string
    // xs:token
    // xs:normalizedString
    // xs:anyURI
    // xs:base64Binary
    // xs:hexBinary
    INode primitive = new ModelObject( "type");
    primitive.getCreateChild( "string");
    primitives.put( "xs:string", primitive);
    primitives.put( "xs:token", primitive);
    primitives.put( "xs:normalizedString", primitive);
    primitives.put( "xs:anyURI", primitive);
    primitives.put( "xs:base64Binary", primitive);
    primitives.put( "xs:hexBinary", primitive);
    
    // xs:boolean
    primitive = new ModelObject( "type");
    primitive.getCreateChild( "boolean");
    primitives.put( "xs:boolean", primitive);
    
    // xs:byte
    primitive = new ModelObject( "type");
    INode constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "min"), Byte.MIN_VALUE);
    Xlate.set( constraint.getCreateChild( "max"), Byte.MAX_VALUE);
    primitives.put( "xs:byte", primitive);
    
    // xs:short
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "min"), Short.MIN_VALUE);
    Xlate.set( constraint.getCreateChild( "max"), Short.MAX_VALUE);
    primitives.put( "xs:short", primitive);

    // xs:int
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "min"), Integer.MIN_VALUE);
    Xlate.set( constraint.getCreateChild( "max"), Integer.MAX_VALUE);
    primitives.put( "xs:int", primitive);

    // xs:integer
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    primitives.put( "xs:integer", primitive);
    
    // xs:long
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "min"), Long.MIN_VALUE);
    Xlate.set( constraint.getCreateChild( "max"), Long.MAX_VALUE);
    primitives.put( "xs:long", primitive);

    // xs:negativeInteger
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "max"), -1);
    primitives.put( "xs:negativeInteger", primitive);
    
    // xs:positiveInteger
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "min"), 1);
    primitives.put( "xs:positiveInteger", primitive);
    
    // xs:nonNegativeInteger
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "min"), 0);
    primitives.put( "xs:nonNegativeInteger", primitive);
    
    // xs:nonPositiveInteger
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    Xlate.set( constraint.getCreateChild( "max"), 0);
    primitives.put( "xs:nonPositiveInteger", primitive);

    // xs:unsignedLong
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    BigInteger two = BigInteger.valueOf( 2);
    BigInteger max = BigInteger.valueOf( Long.MAX_VALUE).multiply( two);
    constraint.getCreateChild( "min").setValue( BigInteger.ZERO);
    constraint.getCreateChild( "max").setValue( max);
    primitives.put( "xs:unsignedLong", primitive);
    
    // xs:unsignedInt
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    max = BigInteger.valueOf( Integer.MAX_VALUE).multiply( two);
    constraint.getCreateChild( "min").setValue( BigInteger.ZERO);
    constraint.getCreateChild( "max").setValue( max);
    primitives.put( "xs:unsignedLong", primitive);
    
    // xs:unsignedShort
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    max = BigInteger.valueOf( Short.MAX_VALUE).multiply( two);
    constraint.getCreateChild( "min").setValue( BigInteger.ZERO);
    constraint.getCreateChild( "max").setValue( max);
    primitives.put( "xs:unsignedShort", primitive);
    
    // xs:unsignedByte
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    max = BigInteger.valueOf( Byte.MAX_VALUE).multiply( two);
    constraint.getCreateChild( "min").setValue( BigInteger.ZERO);
    constraint.getCreateChild( "max").setValue( max);
    primitives.put( "xs:unsignedByte", primitive);
    
     // xs:decimal
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    constraint.getCreateChild( "float");
    primitives.put( "xs:decimal", primitive);
    
    // xs:float
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    constraint.getCreateChild( "float");
    Xlate.set( constraint.getCreateChild( "min"), Float.MIN_VALUE);
    Xlate.set( constraint.getCreateChild( "max"), Float.MAX_VALUE);
    primitives.put( "xs:float", primitive);
    
    // xs:double
    primitive = new ModelObject( "type");
    constraint = primitive.getCreateChild( "number");
    constraint.getCreateChild( "float");
    Xlate.set( constraint.getCreateChild( "min"), Double.MIN_VALUE);
    Xlate.set( constraint.getCreateChild( "max"), Double.MAX_VALUE);
    primitives.put( "xs:double", primitive);
    
    // xs:time
    // xs:date
    // xs:dateTime
    // xs:duration
    // xs:gDay
    // xs:gMonth
    // xs:gMonthDay
    // xs:gYear
    // xs:gYearMonth
    primitive = new ModelObject( "type");
    primitive.getCreateChild( "string");
    primitives.put( "xs:time", primitive);
    primitives.put( "xs:date", primitive);
    primitives.put( "xs:dateTime", primitive);
    primitives.put( "xs:duration", primitive);
    primitives.put( "xs:gDay", primitive);
    primitives.put( "xs:gMonth", primitive);
    primitives.put( "xs:gMonthDay", primitive);
    primitives.put( "xs:gYear", primitive);
    primitives.put( "xs:gYearMonth", primitive);
  }
  
  private IExpression complexTypeFinder = XPath.createExpression(
    "xs:complexType[ @name = $name]");
  
  private IExpression simpleTypeFinder = XPath.createExpression(
    "xs:simpleType[ @name = $name]");
  
  private IExpression groupFinder = XPath.createExpression(
    "xs:group[ @name = $ref]");

  private IExpression sacExpr = XPath.createExpression(
    "xs:sequence | xs:all | xs:choice | xs:group");
  
  private IExpression extensionDefaultExpr = XPath.createExpression(
    "ancestor::xs:element/@default");
  
  private IExpression typeReferenceExpr = XPath.createExpression(
    ".//reference:type");
  
  private IExpression elementReferenceExpr = XPath.createExpression(
    ".//reference:element");

  private Map<String, INode> globalSimpleTypes = new HashMap<String, INode>();
  private Map<String, INode> globalAttributes = new HashMap<String, INode>();
  private Map<String, INode> globalComplexTypes = new HashMap<String, INode>();
  private Map<String, INode> primitives = new HashMap<String, INode>();
  private boolean unordered;
}
