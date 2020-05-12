package org.scribble.ext.assrt.core.type.formula;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.scribble.core.type.name.DataName;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.ext.assrt.util.JavaSmtWrapper;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

// FIXME: not just "A" anymore, e.g., String sort
public abstract class AssrtAVarFormula extends AssrtAFormula
{
	public final String name; 

	protected AssrtAVarFormula(String name)
	{
		this.name = name; 
	}
	
	// i.e., to "type"
	public abstract //AssrtPayElemType<?> 
	AssrtIntVar toName();

	@Override
	public AssrtAVarFormula subs(AssrtAVarFormula old, AssrtAVarFormula neu)
	{
		return this.equals(old) ? neu : this;
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}
		
	@Override
	public String toSmt2Formula(Map<AssrtIntVar, DataName> env)
	{
		/*if (this.name.startsWith("_dum"))  // FIXME
		{
			throw new RuntimeException("[assrt] Use squash first: " + this);
		}*/
		//return "(" + this.name + ")";
		return this.name;
	}
	
	@Override
	public IntegerFormula toJavaSmtFormula()
	{
		IntegerFormulaManager fmanager = JavaSmtWrapper.getInstance().ifm;
		return fmanager.makeVariable(this.name);   
	}
	
	@Override
	public Set<AssrtIntVar> getIntVars()
	{
		Set<AssrtIntVar> vars = new HashSet<>();
		vars.add(toName());  // FIXME: currently may also be a role
		return vars; 
	}
	
	@Override
	public String toString()
	{
		return this.name; 
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtIntVarFormula))
		{
			return false;
		}
		return super.equals(this)  // Does canEqual
				&& this.name.equals(((AssrtIntVarFormula) o).name);
	}

	@Override
	public int hashCode()
	{
		int hash = 9461;
		hash = 31 * hash + super.hashCode();
		hash = 31 * hash + this.name.hashCode();
		return hash;
	}
}
