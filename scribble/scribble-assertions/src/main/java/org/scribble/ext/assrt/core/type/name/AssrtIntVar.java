package org.scribble.ext.assrt.core.type.name;

import java.util.Map;

import org.scribble.core.type.name.AbstractName;
import org.scribble.ext.assrt.core.type.kind.AssrtIntVarKind;

// TODO: integrate with AssrtIntVarFormula
// FIXME: AssrtIntVar a different syntactic category than the SmtFormula vars --
// unify?
// FIXME: now Strings introduced, rename to AssrtAVar, i.e., generic var (and
// record sort?) -- also String formulae coming under AFormula, fix
public class AssrtIntVar extends AbstractName<AssrtIntVarKind>
		implements AssrtPayElemType<AssrtIntVarKind>
{
	private static final long serialVersionUID = 1L;

	//public final String sort; // TODO: refactor -- // CHECKME: AssrtSort?

	public AssrtIntVar(String simplename)
	{
		super(AssrtIntVarKind.KIND, simplename);
		//this.sort = sort;
	}

	public String getSort(Map<AssrtIntVar, String> env)
	{
		if (!env.containsKey(this))
		{
			throw new RuntimeException("[assrt-core] Unknown var: " + this);
		}
		return env.get(this);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtIntVar))
		{
			return false;
		}
		return super.equals(o);  // Checks canEquals
		//&& this.sort.equals(((AssrtIntVar) o).sort);
	}
	
	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof AssrtIntVar;
	}

	@Override
	public int hashCode()
	{
		int hash = 5519;
		hash = 31 * hash + super.hashCode();
		//hash = 31 * hash + this.sort.hashCode();
		return hash;
	}
	
	@Override
	public boolean isAnnotVarName()
	{
		return true;
	}
}
