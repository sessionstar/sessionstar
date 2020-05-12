package org.scribble.ext.assrt.core.type.formula;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.scribble.core.type.name.DataName;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.ext.assrt.core.type.name.AssrtSort;
import org.sosy_lab.java_smt.api.BooleanFormula;


// Make abstract, and use subclasses for specific functions? e.g., for ports
public class AssrtUnintPredicateFormula extends AssrtBFormula implements AssrtUnintFunFormula<BooleanFormula>
{
	public final String name;
	public final List<AssrtAFormula> args;

	protected AssrtUnintPredicateFormula(String name, List<AssrtAFormula> args)
	{
		this.name = name;
		this.args = Collections.unmodifiableList(args);
	}

	@Override
	public AssrtUnintPredicateFormula disamb(Map<AssrtIntVar, DataName> env)
	{
		throw new RuntimeException("Shouldn't get in here: " + this);
	}

	@Override
	public AssrtBFormula getCnf()
	{
		return this;
	}

	@Override
	public boolean isNF(AssrtBinBFormula.Op op)
	{
		return true;
	}

	@Override
	public boolean hasOp(AssrtBinBFormula.Op op)
	{
		return false;
	}

	@Override
	public List<AssrtSort> getParamSorts()
	{
		throw new RuntimeException("[assrt] TODO: " + this);
	}

	@Override
	public AssrtSort getReturnSort()
	{
		throw new RuntimeException("[assrt] TODO: " + this);
	}

	@Override
	public AssrtBFormula squash()
	{
		return this;
	}

	@Override
	public AssrtBFormula subs(AssrtAVarFormula old, AssrtAVarFormula neu)
	{
		return new AssrtUnintPredicateFormula(this.name, 
				this.args.stream().map(a -> a.subs(old, neu)).collect(Collectors.toList()));
	}

	@Override
	public String toSmt2Formula(Map<AssrtIntVar, DataName> env)
	{
		return "(" + this.name + " " + this.args.stream()
				.map(a -> a.toSmt2Formula(env)).collect(Collectors.joining(" ")) + ")";
	}

	@Override
	protected BooleanFormula toJavaSmtFormula()
	{
		throw new RuntimeException("[assrt] TODO: " + this);
	}

	@Override
	public Set<AssrtIntVar> getIntVars()
	{
		return this.args.stream().flatMap(a -> a.getIntVars().stream()).collect(Collectors.toSet());
	}

	@Override
	public String toString()
	{
		return this.name + "(" + this.args.stream().map(a -> a.toString()).collect(Collectors.joining(", ")) + ")";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtUnintPredicateFormula))
		{
			return false;
		}
		AssrtUnintPredicateFormula them = (AssrtUnintPredicateFormula) o;
		return super.equals(this)  // Does canEqual
				&& this.name.equals(them.name) && this.args.equals(them.args);  
	}
	
	@Override
	protected boolean canEqual(Object o)
	{
		return o instanceof AssrtUnintPredicateFormula;
	}

	@Override
	public int hashCode()
	{
		int hash = 7001;
		hash = 31 * hash + this.name.hashCode();
		hash = 31 * hash + this.args.hashCode();
		return hash;
	}
}
