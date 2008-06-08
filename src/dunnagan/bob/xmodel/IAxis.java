/*
 * XModel
 * Author: Bob Dunnagan
 * Copyright 2005. All rights reserved.
 */
package dunnagan.bob.xmodel;

/**
 * Axis constants. The term "axis" is adopted from the X-path specification. The ROOT designation
 * was added for clarity and to simplify the implementation. An axis which includes ROOT is the
 * first element of an absolute path. An arbitrary path might have an axis which includes both ROOT
 * and SELF. In this case, the location-step and predicate would first be evaluated against the root
 * of the query before resolving the root of the tree. In the X-Path specification, an absolute path
 * does not have a starting point in the tree, so the ROOT axis is not necessary. For our purposes,
 * however, introducing the ROOT axis allows us to treat absolute paths and relative paths
 * consistently.
 * <p>
 * The nested axis is an addition to the standard XPath axes. This axis performs a descendant
 * search, but only searches for objects which form an unbroken lineage of like-type (e.g. /a/a/a).
 */
public interface IAxis
{
  public final static int ROOT = 0x01;
  public final static int SELF = 0x02;
  public final static int ANCESTOR = 0x04;
  public final static int PARENT = 0x08;
  public final static int CHILD = 0x10;
  public final static int DESCENDANT = 0x20;
  public final static int NESTED = 0x40;
  public final static int ATTRIBUTE = 0x80;
  public final static int FOLLOWING = 0x100;
  public final static int FOLLOWING_SIBLING = 0x200;
  public final static int PRECEDING = 0x400;
  public final static int PRECEDING_SIBLING = 0x800;
}
