/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel.external;

import java.util.*;

import dunnagan.bob.xmodel.*;
import dunnagan.bob.xmodel.diff.DefaultXmlMatcher;
import dunnagan.bob.xmodel.diff.IXmlMatcher;
import dunnagan.bob.xmodel.diff.XmlDiffer;
import dunnagan.bob.xmodel.xpath.XPath;
import dunnagan.bob.xmodel.xpath.expression.IExpression;

/**
 * An XmlDiffer which allows paths to be associated to instances of ICachingPolicy so that
 * ExternalReferences and CachedObjects can be created correctly when diffing entities.
 * The diff has the following properties:
 * <ul>
 * <li>Only the root node of secondary stages is considered by the diff.
 * <li>The dirty flag of secondary stages is temporarily cleared for the diff.
 * <li>Only the left-hand-side of the diff is considered to contain references.
 * </ul>
 * Here, the term <i>secondary stage</i> refers to a reference which will be created 
 * because of a call to the <code>addCachingPolicy</code> method. When a secondary stage
 * is defined, the dirty flag is specified. If the dirty flag is false, then the diff will
 * consider the entire reference subtree in the diff. If the dirty flag is true, then the 
 * diff will only consider the root node.
 */
public class EntityDiffer extends XmlDiffer
{
  public EntityDiffer()
  {
    entries = new ArrayList<PathEntry>();
    entryMap = new HashMap<IModelObject, PathEntry>();
    dirtySet = new HashSet<IExternalReference>();
    setFactory( factory);
    setMatcher( entityMatcher);
  }

  /**
   * Define the ICachingPolicy to be applied to objects which lie on the specified path.
   * CachedObject instances are created for objects which do not lie on the path.
   * @param path The path relative to the root of the entity.
   * @param cachingPolicy The caching policy to apply.
   * @param dirty Initial state of reference.
   * @deprecated Use the IExpression form of the method instead.
   */
  public void addCachingPolicy( IPath path, ICachingPolicy cachingPolicy, boolean dirty)
  {
    PathEntry entry = new PathEntry();
    entry.path = XPath.convertToExpression( path);
    entry.cachingPolicy = cachingPolicy;
    entry.dirty = dirty;
    entries.add( entry);
  }

  /**
   * Define the ICachingPolicy to be applied to objects which lie on the specified path.
   * CachedObject instances are created for objects which do not lie on the path.
   * @param path The path relative to the root of the entity.
   * @param cachingPolicy The caching policy to apply.
   * @param dirty Initial state of reference.
   */
  public void addCachingPolicy( IExpression path, ICachingPolicy cachingPolicy, boolean dirty)
  {
    PathEntry entry = new PathEntry();
    entry.path = path;
    entry.cachingPolicy = cachingPolicy;
    entry.dirty = dirty;
    entries.add( entry);
  }
  
  /* (non-Javadoc)
   * @see dunnagan.bob.xmodel.diff.AbstractXmlDiffer#setMatcher(dunnagan.bob.xmodel.diff.IXmlMatcher)
   */
  @Override
  public void setMatcher( IXmlMatcher matcher)
  {
    if ( matcher == entityMatcher)
    {
      super.setMatcher( matcher);
    }
    else
    {
      clientMatcher = matcher;
    }
  }

  /**
   * Transform the descendants of the specified object according to the caching policy definitions.
   * @param object The object who may have 
   */
  public void applyTransform( IModelObject object)
  {
    for( PathEntry entry: entries)
    {
      for( IModelObject match: entry.path.query( object, null))
      {
        ExternalReference reference = new ExternalReference( match.getType());
        ModelAlgorithms.copyAttributes( match, reference);
        reference.setCachingPolicy( entry.cachingPolicy, entry.dirty);
        
        IModelObject parent = match.getParent();
        int index = parent.getChildren().indexOf( match);
        match.removeFromParent();
        parent.addChild( reference, index);
      }
    }
  }
  
  /**
   * This method is called just before the diff begins. It clears the dirty flag of secondary 
   * stages which are not bulk loaded with the primary stage. It also populates a map of the
   * secondary stages to prevent removing the children of secondary stages which have not yet
   * been synced.
   * @param lhs The root of the lhs tree.
   * @param rhs The root of the rhs tree.
   */
  protected void startDiff( IModelObject lhs, IModelObject rhs)
  {
    for( PathEntry entry: entries)
    {
      // query lhs tree
      List<IModelObject> nodes = entry.path.query( lhs, null);
      for( IModelObject node: nodes) 
      {
        entryMap.put( node, entry);
        IExternalReference reference = (IExternalReference)node;
        if ( reference.isDirty()) dirtySet.add( reference);
        reference.setDirty( false);
      }
      
      // query rhs tree
      nodes = entry.path.query( rhs, null);
      for( IModelObject node: nodes) 
        entryMap.put( node, entry);
    }
  }
  
  /**
   * This method is called just after the diff ends and cleans up what startDiff does.
   */
  protected void endDiff()
  {
    for( IModelObject key: entryMap.keySet())
    {
      if ( key instanceof IExternalReference)
      {
        IExternalReference reference = (IExternalReference)key;
        reference.setDirty( dirtySet.contains( reference));
      }
    }
    entryMap.clear();
    dirtySet.clear();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for( PathEntry entry: entries)
    {
      sb.append( "  "); sb.append( "Dirty: "); sb.append( entry.dirty); sb.append( "\n");
      sb.append( "  "); sb.append( "Path: "); sb.append( entry.path); sb.append( "\n");
      sb.append( "  "); sb.append( entry.cachingPolicy.toString()); sb.append( "\n");
    }
    return sb.toString();
  }

  final IModelObjectFactory factory = new ModelObjectFactory() {
    public IModelObject createClone( IModelObject object)
    {
      PathEntry entry = entryMap.get( object);
      if ( entry != null)
      {
        ExternalReference clone = new ExternalReference( object.getType());
        ModelAlgorithms.copyAttributes( object, clone);
        clone.setCachingPolicy( entry.cachingPolicy, entry.dirty);
        return clone;
      }
      else
      {
        return object.cloneObject();
      }
    }
  };
  
  final IXmlMatcher entityMatcher = new DefaultXmlMatcher() {
    public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
    {
      if ( clientMatcher != null) clientMatcher.startDiff( lhs, rhs, changeSet);
      EntityDiffer.this.startDiff( lhs, rhs);
    }
    public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
    {
      if ( clientMatcher != null) clientMatcher.endDiff( lhs, rhs, changeSet);
      EntityDiffer.this.endDiff();
    }
    public int findMatch( List<IModelObject> children, IModelObject child)
    {
      if ( clientMatcher == null) return super.findMatch( children, child);
      return clientMatcher.findMatch( children, child);
    }
    public boolean isList( IModelObject parent)
    {
      if ( clientMatcher == null) return super.isList( parent);
      return clientMatcher.isList( parent);
    }
    public boolean isMatch( IModelObject localChild, IModelObject foreignChild)
    {
      if ( clientMatcher == null) return super.isMatch( localChild, foreignChild);
      return clientMatcher.isMatch( localChild, foreignChild);
    }
    public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
    {
      if ( attrName != null && attrName.equals( "xm:wasDirty")) return false;
      boolean clientResult = true;
      if ( clientMatcher != null) clientResult = clientMatcher.shouldDiff( object, attrName, lhs);
      PathEntry entry = entryMap.get( object);
      if ( entry != null && entry.dirty) return false;
      return clientResult;
    }
    public boolean shouldDiff( IModelObject object, boolean lhs)
    {
      boolean clientResult = true;
      if ( clientMatcher != null) clientResult = clientMatcher.shouldDiff( object, lhs);
      IModelObject parent = object.getParent();
      if ( parent != null)
      {
        PathEntry entry = entryMap.get( parent);
        if ( entry != null && entry.dirty) return false;
        return clientResult;
      }
      return clientResult;
    }
  };
  
  public class PathEntry
  {
    IExpression path;
    ICachingPolicy cachingPolicy;
    boolean dirty;
  }

  private List<PathEntry> entries;
  private IXmlMatcher clientMatcher;
  private Map<IModelObject, PathEntry> entryMap;
  private Set<IExternalReference> dirtySet;
}
