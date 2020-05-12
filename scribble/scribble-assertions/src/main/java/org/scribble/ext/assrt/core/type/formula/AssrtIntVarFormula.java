package org.scribble.ext.assrt.core.type.formula;

import java.util.Map;

import org.scribble.core.type.name.DataName;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;

// TODO: integrate with AssrtIntVar
// Variable occurrence
// FIXME: currently also used for roles -- probably need to parse as "ambig" and disamb later
public class AssrtIntVarFormula extends AssrtAVarFormula
{
	// TODO: sort field -- cf. AssrtIntVar -- and deprecate AssrtStrVarFormula

	protected AssrtIntVarFormula(String name)
	{
		super(name);
	}

	@Override
	public AssrtIntVarFormula disamb(Map<AssrtIntVar, DataName> env)
	{
		throw new RuntimeException("Won't get in here: " + this);  // Should not be re-disambiguating 
	}

	// i.e., to "type"
	@Override
	public AssrtIntVar toName()
	{
		return new AssrtIntVar(this.name);
	}

	@Override
	public AssrtIntVarFormula squash()
	{
		return AssrtFormulaFactory.AssrtIntVar(this.name);
	}

	@Override
	public DataName getSort(Map<AssrtIntVar, DataName> env)
	{
		return env.get(toName());
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
	protected boolean canEqual(Object o)
	{
		return o instanceof AssrtIntVarFormula;
	}

	@Override
	public int hashCode()
	{
		int hash = 5903;
		hash = 31 * hash + super.hashCode();
		return hash;
	}
}
