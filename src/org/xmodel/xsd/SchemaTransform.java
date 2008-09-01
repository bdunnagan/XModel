/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
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
  public IModelObject transform( IModelObject schema) throws SchemaException
  {
    IModelObject root = new ModelObject( "schema");

    // transform global complex types
    List<IModelObject> types = transformComplexTypes( schema);
    for( IModelObject type: types) root.addChild( type);
    
    // transform global elements
    Map<String, IModelObject> globalElements = new HashMap<String, IModelObject>();
    List<IModelObject> elements = transformElements( schema);
    for( IModelObject element: elements) 
    {
      // complex type references are added to map below
      if ( !element.getType().startsWith( "reference:type"))
        globalElements.put( Xlate.get( element, "name", ""), element);
      root.addChild( element);
    }
    
    // resolve element references
    for( IModelObject reference: typeReferenceExpr.query( root, null))
    {
      IModelObject parent = reference.getParent();
      if ( parent != null)
      {
        String type = Xlate.get( reference, "type", "");
        IModelObject prototype = globalComplexTypes.get( type);
        
        // type string may contain an erroneous prefix if the xs:element is in the default namespace
        if ( prototype == null)
        {
          String stripped = type.replaceAll( "^[^:]+:", "");
          prototype = globalComplexTypes.get( stripped);
        }
        
        IModelObject element = createElementFromPrototype( prototype);
        ModelAlgorithms.copyAttributes( reference, element);
        reference.removeFromParent();
        parent.addChild( element);
        
        // add to global elements map and remove global attribute
        String name = Xlate.get( element, "name", "");
        globalElements.put( name, element);
      }
    }
    
    // resolve element references
    for( IModelObject reference: elementReferenceExpr.query( root, null))
    {
      IModelObject parent = reference.getParent();
      if ( parent != null)
      {
        String name = Xlate.get( reference, "name", "");
        IModelObject element = globalElements.get( name);
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
  private IModelObject transformSimpleType( IModelObject schema, IModelObject simpleType) throws SchemaException
  {
    // only handle restriction for now (ignore list and union)
    IModelObject restriction = simpleType.getFirstChild( "xs:restriction");
    
    // lookup base type
    String base = Xlate.get( restriction, "base", "");
    IModelObject type = lookupGlobalType( schema, base);
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
  private List<IModelObject> transformComplexTypes( IModelObject schema) throws SchemaException
  {
    List<IModelObject> result = new ArrayList<IModelObject>();
    List<IModelObject> complexTypes = schema.getChildren( "xs:complexType");
    for( IModelObject complexType: complexTypes) result.add( transformComplexType( schema, complexType));
    return result;
  }
  
  /**
   * Transform the specified complex type declaration.
   * @param schema The schema.
   * @param complexType The complex type.
   * @return Returns the transformed complex type.
   */
  private IModelObject transformComplexType( IModelObject schema, IModelObject complexType) throws SchemaException
  {
    // see if complex type is already defined
    String type = Xlate.get( complexType, "name", (String)null);
    if ( type != null)
    {
      IModelObject resolved = globalComplexTypes.get( type);
      if ( resolved != null) return resolved;
    }
    
    // create element definition form complex type
    IModelObject element = new ModelObject( "element");
    element.setAttribute( "type", type);
    
    // add to map here to prevent endless looping
    if ( type != null) globalComplexTypes.put( type, element);
    
    // transform embedded descriptors 
    transformEmbedded( schema, element, complexType);
    
    // transform simple content (reference to global simple type)
    IModelObject simpleContent = complexType.getFirstChild( "xs:simpleContent");
    if ( simpleContent != null) transformContent( schema, element, simpleContent);
    
    // transform complex content
    IModelObject complexContent = complexType.getFirstChild( "xs:complexContent");
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
  private IModelObject transformSac( IModelObject schema, IModelObject sac, IModelObject element) throws SchemaException
  {    
    if ( sac.isType( "xs:group")) 
      return transformSac( schema, getSacFromGroup( schema, sac), element);
    
    IModelObject children = element.getCreateChild( "children");
    IModelObject constraint = null;
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
    List<IModelObject> sacChildren = sac.getChildren();
    for( IModelObject sacChild: sacChildren)
    {
      if ( sacChild.isType( "xs:element"))
      {
        // transform element and add to parent
        IModelObject transformed = transformElement( schema, sacChild);
        children.addChild( transformed);
        
        // add element to constraint
        String name = Xlate.get( sacChild, "name", (String)null);
        if ( name == null) name = Xlate.get( sacChild, "ref", (String)null);
        IModelObject reference = new ModelObject( "child");
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
        IModelObject transformed = transformSac( schema, sacChild, element);
        if ( transformed != null) 
        {
          // flatten like constraints (ex: list with embedded list)
          if ( transformed.getType().equals( constraint.getType()))
          {
            for( IModelObject child: transformed.getChildren().toArray( new IModelObject[ 0]))
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
  private List<IModelObject> transformElements( IModelObject schema) throws SchemaException
  {
    List<IModelObject> result = new ArrayList<IModelObject>();
    List<IModelObject> elements = schema.getChildren( "xs:element");
    for( IModelObject element: elements) 
    {
      IModelObject transformed = transformElement( schema, element);
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
  private IModelObject transformElement( IModelObject schema, IModelObject element) throws SchemaException
  {
    // reference to a global element
    String ref = Xlate.get( element, "ref", (String)null);
    if ( ref != null) 
    {
      IModelObject elementReference = new ModelObject( "reference:element");
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
      IModelObject globalElement = lookupGlobalComplexType( schema, type);
      if ( globalElement != null) 
      {
        IModelObject elementReference = new ModelObject( "reference:type");
        elementReference.setAttribute( "name", name);
        elementReference.setAttribute( "type", type);
        return elementReference;
      }

      // lookup simple type
      IModelObject globalType = lookupGlobalType( schema, type);
      if ( globalType == null) throw new SchemaException( "Unable to resolve type: "+type);
      
      // create element
      IModelObject result = new ModelObject( "element");
      result.setAttribute( "name", name);
      IModelObject value = result.getCreateChild( "value");
      value.addChild( globalType.cloneTree());
      
      // default
      String defaultString = Xlate.get( element, "default", (String)null);
      if ( defaultString != null)
      {
        IModelObject dfault = result.getCreateChild( "default");
        dfault.setValue( defaultString);
      }
      
      return result;
    }

    // transform simple type
    IModelObject simpleType = element.getFirstChild( "xs:simpleType");
    if ( simpleType != null)
    {
      IModelObject transformed = transformSimpleType( schema, simpleType);
      IModelObject result = new ModelObject( "element");
      result.setAttribute( "name", name);
      
      // type
      IModelObject value = result.getCreateChild( "value");
      value.addChild( transformed);
      
      // default
      String defaultString = Xlate.get( element, "default", (String)null);
      if ( defaultString != null)
      {
        IModelObject dfault = value.getCreateChild( "default");
        dfault.setValue( defaultString);
      }
      
      return result;
    }
    
    // transform complex type
    IModelObject complexType = element.getFirstChild( "xs:complexType");
    if ( complexType != null)
    {
      IModelObject transformed = transformComplexType( schema, complexType);
      transformed.setAttribute( "name", name);
      
      // default override (not handled by XmlSpy)
      String defaultString = Xlate.get( element, "default", (String)null);
      if ( defaultString != null)
      {
        IModelObject value = transformed.getFirstChild( "value");
        if ( value == null)
          throw new SchemaException(
            "Attempt to override default value when value is not defined: "+element);
        
        IModelObject dfault = value.getCreateChild( "default");
        dfault.setValue( defaultString);
      }
      
      return transformed;
    }

    // empty element
    IModelObject result = new ModelObject( "element");
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
  private IModelObject createElementFromPrototype( IModelObject prototype)
  {
    IModelObject element = new ModelObject( "element");
    List<IModelObject> children = prototype.getChildren();
    for( IModelObject child: children)
      element.addChild( new Reference( child));
    return element;
  }
  
  /**
   * Returns the transformed attribute.
   * @param schema The schema.
   * @param attribute The attribute.
   * @return Returns the transformed attribute.
   */
  private IModelObject transformAttribute( IModelObject schema, IModelObject attribute) throws SchemaException
  {
    String ref = Xlate.get( attribute, "ref", (String)null);
    if ( ref == null)
    {
      IModelObject result = new ModelObject( "attribute");
      
      String name = Xlate.get( attribute, "name", (String)null);
      result.setAttribute( "name", name);
      
      String dfault = Xlate.get( attribute, "default", (String)null);
      result.setAttribute( "default", dfault);
      
      String use = Xlate.get( attribute, "use", "optional");
      result.setAttribute( "use", use);

      String type = Xlate.get( attribute, "type", (String)null);
      if ( type != null)
      {
        IModelObject globalType = lookupGlobalType( schema, type);
        result.addChild( globalType.cloneTree());
        return result;
      }
      
      IModelObject simpleType = attribute.getFirstChild( "xs:simpleType");
      if ( simpleType != null) result.addChild( transformSimpleType( schema, simpleType));
      
      return result;
    }
    else
    {
      IModelObject globalAttribute = globalAttributes.get( ref);
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
  private IModelObject getSacFromGroup( IModelObject schema, IModelObject group) throws SchemaException
  {
    // embedded
    String name = Xlate.get( group, "name", (String)null);
    if ( name != null) return sacExpr.queryFirst( group);
    
    // reference
    String ref = Xlate.get( group, "ref", (String)null);
    if ( ref != null)
    {
      groupFinder.setVariable( "ref", ref);
      IModelObject resolved = groupFinder.queryFirst( schema);
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
  private IModelObject lookupGlobalType( IModelObject schema, String name) throws SchemaException
  {
    // primitive
    IModelObject primitive = primitives.get( name);
    if ( primitive != null) return primitive;
    
    // already transformed global type
    IModelObject type = globalSimpleTypes.get( name);
    if ( type != null) return type;
    
    // create transformed global simple type
    simpleTypeFinder.setVariable( "name", name);
    IModelObject simpleType = simpleTypeFinder.queryFirst( schema);
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
  private Reference lookupGlobalComplexType( IModelObject schema, String type) throws SchemaException
  {
    // see if type has already been transformed
    IModelObject element = globalComplexTypes.get( type);
    if ( element != null) return new Reference( element);

    // type string may contain an erroneous prefix if the xs:element is in the default namespace
    String stripped = type.replaceAll( "^[^:]+:", "");
    element = globalComplexTypes.get( stripped);
    if ( element != null) return new Reference( element);
    
    // find untransformed element
    complexTypeFinder.setVariable( "name", type);
    IModelObject untransformed = complexTypeFinder.queryFirst( schema);
    
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
  private void transformContent( IModelObject schema, IModelObject element, IModelObject content) throws SchemaException
  {
    // restriction
    IModelObject restriction = content.getFirstChild( "xs:restriction");
    if ( restriction != null) transformRestriction( schema, element, restriction);
    
    // extension
    IModelObject extension = content.getFirstChild( "xs:extension");
    if ( extension != null) transformExtension( schema, element, extension);
  }
  
  /**
   * Transform the specified restriction and apply to the given element.
   * @param schema The schema.
   * @param element The transformed element.
   * @param restriction The untransformed restriction.
   */
  private void transformRestriction( IModelObject schema, IModelObject element, IModelObject restriction) throws SchemaException
  {
    // NOTE: global complex and simple types do not have separate namespaces here
    String base = Xlate.get( restriction, "base", "");
    
    // handle global simple type
    IModelObject type = lookupGlobalType( schema, base);
    if ( type != null) 
    {
      IModelObject value = element.getCreateChild( "value");
      IModelObject parent = value.getCreateChild( "type");
      transformRestrictionConstraint( schema, type, restriction);
      parent.addChild( type.cloneTree());
    }
    else
    {
      // handle global complex type (element)
      IModelObject reference = lookupGlobalComplexType( schema, base);
      if ( reference != null)
      {
        IModelObject value = reference.getFirstChild( "value");
        if ( value != null) element.addChild( new Reference( value));
        IModelObject attributes = reference.getFirstChild( "attributes");
        if ( attributes != null) element.addChild( new Reference( attributes));
      }
      else
      {
        // handle restriction constraint (mutually exclusive with global simple type above)
        IModelObject value = element.getCreateChild( "value");
        IModelObject parent = value.getCreateChild( "type");
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
  private void transformRestrictionConstraint( IModelObject schema, IModelObject type, IModelObject restriction) 
  throws SchemaException
  {
    // process base type
    String base = Xlate.get( restriction, "base", "");
    
    // process restriction
    if ( isString( base))
    {
      IModelObject stringConstraint = type.getCreateChild( "string");
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
      IModelObject numberConstraint = type.getCreateChild( "number");
      if ( restriction == null) return;
      
      String minInclusive = Xlate.get( restriction.getFirstChild( "xs:minInclusive"), "value", (String)null);
      if ( minInclusive != null) 
      {
        IModelObject min = numberConstraint.getCreateChild( "min");
        min.setValue( minInclusive);
      }
      
      String minExclusive = Xlate.get( restriction.getFirstChild( "xs:minExclusive"), "value", (String)null);
      if ( minExclusive != null) 
      {
        IModelObject min = numberConstraint.getCreateChild( "min");
        min.setAttribute( "exclusive", "true");
        min.setValue( minExclusive);
      }
      
      String maxInclusive = Xlate.get( restriction.getFirstChild( "xs:maxInclusive"), "value", (String)null);
      if ( maxInclusive != null) 
      {
        IModelObject max = numberConstraint.getCreateChild( "max");
        max.setValue( maxInclusive);
      }
      
      String maxExclusive = Xlate.get( restriction.getFirstChild( "xs:maxExclusive"), "value", (String)null);
      if ( maxExclusive != null) 
      {
        IModelObject max = numberConstraint.getCreateChild( "max");
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
    List<IModelObject> enumerations = restriction.getChildren( "xs:enumeration");
    if ( enumerations.size() > 0)
    {
      IModelObject enumConstraint = type.getCreateChild( "enum");
      for( IModelObject enumeration: enumerations)
      {
        IModelObject value = new ModelObject( "value");
        value.setValue( Xlate.get( enumeration, "value", ""));
        enumConstraint.addChild( value);
      }
    }
    
    // pattern
    IModelObject pattern = restriction.getFirstChild( "xs:pattern");
    if ( pattern != null)
    {
      IModelObject patternConstraint = type.getCreateChild( "pattern");
      patternConstraint.setValue( Xlate.get( pattern, "value", ""));
    }
  }
  
  /**
   * Transform the specified extension and apply to the given element.
   * @param schema The schema.
   * @param element The transformed element.
   * @param extension The untransformed extension.
   */
  private void transformExtension( IModelObject schema, IModelObject element, IModelObject extension) throws SchemaException
  {
    String base = Xlate.get( extension, "base", "");
    
    // simple type
    IModelObject type = lookupGlobalType( schema, base);
    if ( type != null)
    {
      IModelObject value = element.getCreateChild( "value");
      value.addChild( type.cloneTree());
      
      // set default
      IModelObject dfaultAttribute = extensionDefaultExpr.queryFirst( extension);
      String dfault = Xlate.get( dfaultAttribute, (String)null);
      if ( dfault != null) Xlate.set( value.getCreateChild( "default"), dfault);
      
      // add extension attributes
      transformEmbedded( schema, element, extension);
    }
    else
    {
      // complex type
      IModelObject prototype = lookupGlobalComplexType( schema, base);
      if ( prototype != null)
      {
        // clone attributes
        IModelObject attributes = prototype.getFirstChild( "attributes");
        if ( attributes != null) element.addChild( attributes.cloneTree());
        
        // clone children
        IModelObject children = prototype.getFirstChild( "children");
        if ( children != null) element.addChild( children.cloneTree());

        // transform extension first
        transformEmbedded( schema, element, extension);

        // combine base and extension constraints
        IModelObject baseConstraint = prototype.getFirstChild( "constraint");
        if ( baseConstraint != null)
        {
          baseConstraint = baseConstraint.cloneTree();
          IModelObject baseSac = baseConstraint.getChild( 0);
          IModelObject constraint = element.getFirstChild( "constraint");
          if ( constraint != null)
          {
            IModelObject extensionSac = constraint.getChild( 0);
            if ( !extensionSac.getType().equals( baseSac.getType()))
              throw new SchemaException(
                "Extension model must match base model: extension="+element+", base="+base);
            
            // prepend base constraint sac children
            IModelObject[] baseSacChildren = baseSac.getChildren().toArray( new IModelObject[ 0]);
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
          IModelObject constraint = element.getFirstChild( "constraint");
          if ( constraint != null)
          {
            IModelObject extensionSac = element.getFirstChild( "constraint").getChild( 0);
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
  private void transformEmbedded( IModelObject schema, IModelObject element, IModelObject parent) throws SchemaException
  {
    // transform embedded sac
    IModelObject sac = sacExpr.queryFirst( parent);
    if ( sac != null)
    {
      IModelObject constraint = transformSac( schema, sac, element);
      if ( constraint != null) element.getCreateChild( "constraint").addChild( constraint);
    }
    
    // transform embedded attributes
    IModelObject attributes = null;
    List<IModelObject> xsAttributes = parent.getChildren( "xs:attribute");
    for( IModelObject xsAttribute: xsAttributes)
    {
      if ( attributes == null) attributes = element.getCreateChild( "attributes");
      IModelObject attribute = transformAttribute( schema, xsAttribute);
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
    IModelObject primitive = new ModelObject( "type");
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
    IModelObject constraint = primitive.getCreateChild( "number");
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

  private Map<String, IModelObject> globalSimpleTypes = new HashMap<String, IModelObject>();
  private Map<String, IModelObject> globalAttributes = new HashMap<String, IModelObject>();
  private Map<String, IModelObject> globalComplexTypes = new HashMap<String, IModelObject>();
  private Map<String, IModelObject> primitives = new HashMap<String, IModelObject>();
  private boolean unordered;
}
