package org.xmodel.net.execution;

import java.util.ArrayList;
import java.util.List;
import org.xmodel.INode;
import org.xmodel.xpath.expression.IContext;
import org.xmodel.xpath.expression.IExpression;
import org.xmodel.xpath.expression.StatefulContext;

/**
 * A class that provides a mechanism for restricting the domain of scripting elements that can be executed remotely.
 * An ExecutePrivilege instance is specified to a Protocol instance.  In the absence of an ExecutePrivilege
 * association, the Protocol class will allow execution of any scripting element.
 * <p>
 * Privileges and restrictions are specified by regular expression patterns that are matched against the name
 * of the scripting element when an execute request is received.  If any scripting element is disallowed, then
 * then entire script is disallowed.
 * <p>
 * Permissions and restrictions are stored in an ordered list that is traversed from earliest to latest entry
 * when determining whether a scripting element will be permitted to execute.  This means that the user has 
 * a choice about whether to provide broad access with narrow restrictions, or broad restriction with narrow
 * permission.
 */
public class ExecutionPrivilege
{
  public ExecutionPrivilege()
  {
    entries = new ArrayList<Entry>();
  }
  
  /**
   * Specify an expression for scripting elements that will be allowed to execute.
   * @param expression The expression.
   */
  public void permit( IExpression expression)
  {
    entries.add( new Entry( expression, true));
  }
  
  /**
   * Specify an expression for scripting elements that will NOT be allowed to execute.
   * @param expression The expression.
   */
  public void restrict( IExpression expression)
  {
    entries.add( new Entry( expression, false));
  }
  
  /**
   * Returns true if the specified script contains only scripting elements that are permitted to execute.
   * @param context The execution context.
   * @param script The script.
   * @return Returns true if the specified script contains only scripting elements that are permitted to execute.
   */
  public boolean isPermitted( IContext context, INode script)
  {
    StatefulContext scriptContext = new StatefulContext( context, script.cloneTree());
    for( Entry entry: entries)
    {
      if ( !entry.isPermitted( scriptContext)) 
        return false;
    }
    return true;
  }
  
  private final static class Entry
  {
    public Entry( IExpression expression, boolean permit)
    {
      this.expression = expression;
      this.permit = permit;
    }
    
    /**
     * Returns true if the specified element is allowed to execute.
     * @param context The context.
     * @return Returns true if the specified element is allowed to execute.
     */
    public boolean isPermitted( IContext context)
    {
      List<INode> elements = expression.query( context, null);
      for( INode element: elements) element.removeFromParent();
      return (elements.size() == 0 || permit);
    }

    private IExpression expression;
    private boolean permit;
  }

  private List<Entry> entries;
}
