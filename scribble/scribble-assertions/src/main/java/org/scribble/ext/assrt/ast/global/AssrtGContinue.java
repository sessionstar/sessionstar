package org.scribble.ext.assrt.ast.global;

import java.util.List;
import java.util.stream.Collectors;

import org.antlr.runtime.Token;
import org.scribble.ast.ScribNode;
import org.scribble.ast.global.GContinue;
import org.scribble.ast.name.simple.RecVarNode;
import org.scribble.del.DelFactory;
import org.scribble.ext.assrt.ast.AssrtAExprNode;
import org.scribble.ext.assrt.ast.AssrtStateVarArgNode;
import org.scribble.ext.assrt.del.AssrtDelFactory;
import org.scribble.util.ScribException;
import org.scribble.visit.AstVisitor;

@Deprecated
public class AssrtGContinue extends GContinue
		implements AssrtStateVarArgNode
{
	//public static final int RECVAR_CHILD_INDEX = 0;
	public static final int EXPR_CHILDREN_START_INDEX = 1;

	// ScribTreeAdaptor#create constructor
	public AssrtGContinue(Token t)
	{
		super(t);
	}

	// Tree#dupNode constructor
	protected AssrtGContinue(AssrtGContinue node)
	{
		super(node);
	}
	
	@Override
	public List<AssrtAExprNode> getAnnotExprChildren()
	{
		List<? extends ScribNode> cs = getChildren();
		return cs.subList(EXPR_CHILDREN_START_INDEX, cs.size()).stream()
				.map(x -> (AssrtAExprNode) x).collect(Collectors.toList());
	}
	
	@Override
	public void addScribChildren(RecVarNode rv)
	{
		throw new RuntimeException(
				"[assrt] Deprecated for " + getClass() + ":\n\t" + this);
	}

	// "add", not "set"
	public void addScribChildren(RecVarNode rv, List<AssrtAExprNode> aexprs)
	{
		// Cf. above getters and Scribble.g children order
		addChild(rv);
		addChildren(aexprs);
	}

	@Override
	public AssrtGContinue dupNode()
	{
		return new AssrtGContinue(this);
	}

	@Override
	public void decorateDel(DelFactory df)
	{
		((AssrtDelFactory) df).AssrtGContinue(this);
	}

	@Override
	public AssrtGContinue reconstruct(RecVarNode rv)
	{
		throw new RuntimeException(
				"[assrt] Deprecated for " + getClass() + ":\n\t" + this);
	}

	public AssrtGContinue reconstruct(RecVarNode rv, List<AssrtAExprNode> aexprs)
	{
		AssrtGContinue dup = dupNode();
		dup.addScribChildren(rv, aexprs);
		dup.setDel(del());  // No copy
		return dup;
	}

	@Override
	public GContinue visitChildren(AstVisitor v) throws ScribException
	{
		RecVarNode rv = (RecVarNode) visitChild(getRecVarChild(), v);
		List<AssrtAExprNode> aexprs = visitChildListWithClassEqualityCheck(this,
				getAnnotExprChildren(), v);
		return reconstruct(rv, aexprs);
	}

	@Override
	public String toString()
	{
		return super.toString() + annotToString();
	}
}










/*
	public final List<AssrtArithExpr> annotexprs;  // cf. AssrtGDo

	public AssrtGContinue(CommonTree source, RecVarNode recvar)
	{
		this(source, recvar, Collections.emptyList());
	}

	public AssrtGContinue(CommonTree source, RecVarNode recvar,
			List<AssrtArithExpr> annotexprs)
	{
		super(source, recvar);
		//this.annot = annot;
		this.annotexprs = Collections.unmodifiableList(annotexprs);
	}

	// Similar to reconstruct pattern
	public LContinue project(AstFactory af, Role self)
	{
		throw new RuntimeException("[assrt] Shouldn't get in here: " + this);
	}

	public AssrtLContinue project(AstFactory af, Role self, //AssrtArithExpr annot)
			List<AssrtArithExpr> annotexprs)
	{
		RecVarNode recvar = (RecVarNode) af.SimpleNameNode(this.recvar.getSource(), RecVarKind.KIND, this.recvar.toName().toString());
		AssrtLContinue projection = ((AssrtAstFactory) af).AssrtLContinue(this.source, recvar, //annot);
				annotexprs);
		return projection;
	}
*/
