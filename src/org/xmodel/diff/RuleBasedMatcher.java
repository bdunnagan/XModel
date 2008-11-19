/*
 * XModel (XML Application Data Modeling Framework)
 * Author: Bob Dunnagan (bdunnagan@nc.rr.com)
 * Copyright Bob Dunnagan 2009. All rights reserved.
 */
package org.xmodel.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.xmodel.IChangeSet;
import org.xmodel.IModelObject;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;


/**
 * An implementation of IXmlMatcher which delegates to one or more IXmlMatcher implementations based on
 * rules defined by boolean XPath expressions.  Rules are processed from last to first and the first match 
 * gives the delegate that will be used.  In other words, if the last rule in the rule list matches then
 * that is the rule which will provide the matcher.
 */
public class RuleBasedMatcher implements IXmlMatcher
{
  public RuleBasedMatcher()
  {
    stack = new Stack<IXmlMatcher>();
    defaultMatcher = new DefaultXmlMatcher();
  }
  
  /**
   * Convenience method for setting the default rule used when no other rules match.
   * @param matcher The matcher.
   */
  public void setDefaultRule( IXmlMatcher matcher)
  {
    defaultMatcher = matcher;
  }
  
  /**
   * Convenience method for creating and adding an ExpressionRule.
   * @param condition The condition under which the rule matches.
   * @param matcher The matcher to be used when both conditions match.
   */
  public void addRule( IExpression condition, IXmlMatcher matcher)
  {
    addRule( new ExpressionRule( condition, matcher));
  }
  
  /**
   * Add the specified rule.
   * @param rule The rule.
   */
  public void addRule( IRule rule)
  {
    if ( rules == null) rules = new ArrayList<IRule>();
    rules.add( rule);
  }
  
  /**
   * Add the specified rule at the specified index.
   * @param index The index.
   * @param rule The rule.
   */
  public void addRule( int index, IRule rule)
  {
    if ( rules == null) rules = new ArrayList<IRule>();
    rules.add( index, rule);
  }
  
  /**
   * Remove the specified rule.
   * @param rule The rule.
   */
  public void removeRule( IRule rule)
  {
    if ( rules == null) return;
    rules.remove( rule);
  }
  
  /**
   * Set the rules used by this matcher (not copied).
   * @param rules The rules.
   */
  public void setRules( List<IRule> rules)
  {
    this.rules = rules;
  }
  
  /**
   * Returns the rules used by this matcher (not a copy).
   * @return Returns the rules used by this matcher (not a copy).
   */
  public List<IRule> getRules()
  {
    if ( rules == null) rules = new ArrayList<IRule>();
    return rules;
  }
  
  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, java.lang.String, boolean)
   */
  public boolean shouldDiff( IModelObject object, String attrName, boolean lhs)
  {
    return delegate.shouldDiff( object, attrName, lhs);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#shouldDiff(org.xmodel.IModelObject, boolean)
   */
  public boolean shouldDiff( IModelObject object, boolean lhs)
  {
    return delegate.shouldDiff( object, lhs);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#startDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void startDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    delegate = findRule( lhs, rhs);
    if ( delegate == null) delegate = defaultMatcher;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#endDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void endDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    delegate = null;
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#enterDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void enterDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    stack.push( delegate);
    IXmlMatcher matcher = findRule( lhs, rhs);
    if ( matcher != null) delegate = matcher;
    // FIXME: this should call startDiff
    delegate.enterDiff( lhs, rhs, changeSet);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#exitDiff(org.xmodel.IModelObject, org.xmodel.IModelObject, 
   * org.xmodel.IChangeSet)
   */
  public void exitDiff( IModelObject lhs, IModelObject rhs, IChangeSet changeSet)
  {
    // FIXME: this should call endDiff
    delegate.exitDiff( lhs, rhs, changeSet);
    delegate = stack.pop();
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#findMatch(java.util.List, org.xmodel.IModelObject)
   */
  public int findMatch( List<IModelObject> children, IModelObject child)
  {
    return delegate.findMatch( children, child);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#isList(org.xmodel.IModelObject)
   */
  public boolean isList( IModelObject parent)
  {
    return delegate.isList( parent);
  }

  /* (non-Javadoc)
   * @see org.xmodel.diff.IXmlMatcher#isMatch(org.xmodel.IModelObject, org.xmodel.IModelObject)
   */
  public boolean isMatch( IModelObject leftChild, IModelObject rightChild)
  {
    return delegate.isMatch( leftChild, rightChild);
  }

  /**
   * Determine the matching rule for the specified lhs and rhs.
   * @param lhs The left-hand-side.
   * @param rhs The right-hand-side.
   * @return Returns the delegate from the matching rule.
   */
  private IXmlMatcher findRule( IModelObject lhs, IModelObject rhs)
  {
    if ( rules != null)
    {
      for( int i=rules.size()-1; i>=0; i--)
      {
        IRule rule = rules.get( i);
        if ( rule.doesRuleApply( lhs, rhs))
          return rule.getMatcher();
      }
    }
    return null;
  }
  
  /**
   * An interface of a rule in the RuleBasedMatcher (duh). The purpose of this interface is 
   * to provide some flexibility as to how rules are defined.
   */
  public static interface IRule
  {
    /**
     * Returns true if this rule matches the specified lhs and rhs elements.
     * @param lhs The left-hand-side of the diff which is about to be performed.
     * @param rhs The right-hand-side of the diff which is about to be performed.
     * @return Returns true if this rule matches the specified lhs and rhs elements.
     */
    public boolean doesRuleApply( IModelObject lhs, IModelObject rhs);
    
    /**
     * Returns the delegate matcher to be used when this rule matches.
     * @return Returns the delegate matcher to be used when this rule matches.
     */
    public IXmlMatcher getMatcher();
  }

  /**
   * An implementation of the Rule interface which matches on an XPath expression. The $lhs and $rhs variables
   * are set to the left-hand-side and right-hand-side elements of the diff before the expression is evaluated.
   */
  public static class ExpressionRule implements IRule
  {
    /**
     * Create an ExpressionRule with one or two conditions.
     * @param condition The condition under which the rule matches.
     * @param matcher The matcher.
     */
    public ExpressionRule( IExpression condition, IXmlMatcher matcher)
    {
      this.condition = condition;
      this.matcher = matcher;
    }
    
    /* (non-Javadoc)
     * @see org.xmodel.diff.RuleBasedMatcher.Rule#doesRuleApply(org.xmodel.IModelObject, org.xmodel.IModelObject)
     */
    public boolean doesRuleApply( IModelObject lhs, IModelObject rhs)
    {
      StatefulContext context = new StatefulContext( lhs);
      context.set( "lhs", lhs);
      context.set( "rhs", rhs);
      return condition.evaluateBoolean( context);
    }

    /* (non-Javadoc)
     * @see org.xmodel.diff.RuleBasedMatcher.Rule#getMatcher()
     */
    public IXmlMatcher getMatcher()
    {
      return matcher;
    }
    
    private IExpression condition;
    private IXmlMatcher matcher;
  }
  
  private IXmlMatcher defaultMatcher;
  private List<IRule> rules;
  private Stack<IXmlMatcher> stack;
  private IXmlMatcher delegate;
}
