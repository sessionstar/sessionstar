package org.scribble.ext.assrt.core.type.formula;

import java.util.Map;
import java.util.Set;

import org.scribble.core.type.name.DataName;
import org.scribble.ext.assrt.core.type.formula.AssrtBinBFormula.Op;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.ext.assrt.util.JavaSmtWrapper;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;

public class AssrtNegFormula extends AssrtBFormula
{
	public final AssrtBFormula expr;

	protected AssrtNegFormula(AssrtBFormula expr)
	{
		this.expr = expr;
	}

	@Override
	public AssrtNegFormula disamb(Map<AssrtIntVar, DataName> env)
	{
		return new AssrtNegFormula((AssrtBFormula) this.expr.disamb(env));
	}

	@Override
	public AssrtBFormula getCnf()
	{
		if (this.expr instanceof AssrtNegFormula)
		{
			return ((AssrtNegFormula) this.expr).expr.getCnf();
		}
		else if (this.expr instanceof AssrtBinBFormula)
		{
			AssrtBinBFormula bf = (AssrtBinBFormula) this.expr;
			switch (bf.op)
			{
				case And:
				{
					AssrtBinBFormula tmp
							= AssrtFormulaFactory.AssrtBinBool(Op.Or, AssrtFormulaFactory.AssrtNeg(bf.left), AssrtFormulaFactory.AssrtNeg(bf.right));
					return tmp.getCnf();
				}
				case Imply:
				{
					throw new RuntimeException("[assrt-core] TODO: " + this);
				}
				case Or:
				{
					AssrtBinBFormula tmp
							= AssrtFormulaFactory.AssrtBinBool(Op.And, AssrtFormulaFactory.AssrtNeg(bf.left), AssrtFormulaFactory.AssrtNeg(bf.right));
					return tmp.getCnf();
				}
				default:
				{
					throw new RuntimeException("[assrt-core] Shouldn't get in here: " + this);
				}
			}
		}
		else
		{
			//throw new RuntimeException("[assrt-core] TODO: " + this);
			return this.expr.getCnf();
		}
	}

	@Override
	public boolean isNF(AssrtBinBFormula.Op op)
	{
		return this.expr.hasOp(op == Op.And ? Op.Or : Op.And);
	}

	@Override
	public boolean hasOp(AssrtBinBFormula.Op op)
	{
		return this.expr.hasOp(op);
	}

	@Override
	public AssrtBFormula squash()
	{
		return AssrtFormulaFactory.AssrtNeg(this.expr.squash());
	}

	@Override
	public AssrtBFormula subs(AssrtAVarFormula old, AssrtAVarFormula neu)
	{
		return AssrtFormulaFactory.AssrtNeg(this.expr.subs(old, neu));
	}

	@Override
	public String toSmt2Formula(Map<AssrtIntVar, DataName> env)
	{
		return "(not " + this.expr.toSmt2Formula(env) + ")";
	}

	@Override
	protected BooleanFormula toJavaSmtFormula()
	{
		BooleanFormulaManager fmanager = JavaSmtWrapper.getInstance().bfm;
		return fmanager.not(this.expr.toJavaSmtFormula());
	}

	@Override
	public Set<AssrtIntVar> getIntVars()
	{
		Set<AssrtIntVar> vs = this.expr.getIntVars();
		return vs;
	}
	
	@Override
	public String toString()
	{
		return "!" + this.expr;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtNegFormula))
		{
			return false;
		}
		AssrtNegFormula f = (AssrtNegFormula) o;
		return super.equals(this)  // Does canEqual
				&& this.expr.equals(f.expr);  
	}
	
	@Override
	protected boolean canEqual(Object o)
	{
		return o instanceof AssrtNegFormula;
	}

	@Override
	public int hashCode()
	{
		int hash = 7109;
		hash = 31 * hash + super.hashCode();
		hash = 31 * hash + this.expr.hashCode();
		return hash;
	}
}
