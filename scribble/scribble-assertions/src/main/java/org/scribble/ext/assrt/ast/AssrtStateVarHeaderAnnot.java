package org.scribble.ext.assrt.ast;

import org.antlr.runtime.Token;
import org.scribble.ast.ScribNodeBase;
import org.scribble.del.DelFactory;
import org.scribble.ext.assrt.del.AssrtDelFactory;
import org.scribble.util.ScribException;
import org.scribble.visit.AstVisitor;

// Currently serves as an "intemediary" parse object between Assertions.g and
// AssrtScribble.g
// Finally, AssrtGProtoHeader retains only the statevardecllist and assertion,
// not this headerannot itself
public class AssrtStateVarHeaderAnnot extends ScribNodeBase
{	
	// Moved/copied here from AssrtGProtoHeader
	public static final int ASSRT_STATEVARDECLLIST_CHILD_INDEX = 0;  // null if no @-annot; o/w may be empty but not null (cf. ParamDeclList child)
	public static final int ASSRT_ASSERTION_CHILD_INDEX = 1;  // null if no @-annot; o/w may still be null

	// ScribTreeAdaptor#create constructor
	public AssrtStateVarHeaderAnnot(Token t)
	{
		super(t);
	}

	/*public final AssrtStateVarDeclList svars;
	public final AssrtBExprNode ass;*/

	// Created "manually" from Assertions.g, not "directly parsed"
	public AssrtStateVarHeaderAnnot(Token t, AssrtStateVarDeclList svars,
			AssrtBExprNode ass)
	{
		super(t);
		addScribChildren(svars, ass);
	}

	// Tree#dupNode constructor
	protected AssrtStateVarHeaderAnnot(AssrtStateVarHeaderAnnot node)
	{
		super(node);
	}

	public AssrtStateVarDeclList getStateVarDeclListChild()
	{
		return (AssrtStateVarDeclList) getChild(ASSRT_STATEVARDECLLIST_CHILD_INDEX);
	}

	// N.B. null if not specified -- currently duplicated from AssrtGMessageTransfer
	public AssrtBExprNode getAnnotAssertChild()
	{
		return (AssrtBExprNode) getChild(ASSRT_ASSERTION_CHILD_INDEX);
	}

	// "add", not "set"
	public void addScribChildren(AssrtStateVarDeclList svars, AssrtBExprNode ass)
	{
		// Cf. above getters
		addChild(svars);
		addChild(ass);
	}
	
	@Override
	public AssrtStateVarHeaderAnnot dupNode()
	{
		return new AssrtStateVarHeaderAnnot(this);
	}
	
	@Override
	public void decorateDel(DelFactory df)
	{
		((AssrtDelFactory) df).AssrtStateVarAnnotNode(this);
	}
	
	public AssrtStateVarHeaderAnnot reconstruct(AssrtStateVarDeclList svars,
			AssrtBExprNode ass)
	{
		AssrtStateVarHeaderAnnot dup = dupNode();
		dup.addScribChildren(svars, ass);
		dup.setDel(del());  // No copy
		return dup;
	}

	@Override
	public AssrtStateVarHeaderAnnot visitChildren(AstVisitor v)
			throws ScribException
	{
		AssrtStateVarDeclList svars = getStateVarDeclListChild();
		if (svars != null)  // CHECKME: now never null? (or shouldn't be?)
		{
			svars = (AssrtStateVarDeclList) visitChild(svars, v);
		}
		AssrtBExprNode ass = getAnnotAssertChild();
		if (ass != null)
		{
			ass = (AssrtBExprNode) visitChild(ass, v);
		}
		return reconstruct(svars, ass);
	}

	@Override
	public String toString()
	{
		return getStateVarDeclListChild() + " " + getAnnotAssertChild();
	}
}
