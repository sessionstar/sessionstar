package org.scribble.ext.assrt.core.type.session.global;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.core.type.kind.Global;
import org.scribble.core.type.name.DataName;
import org.scribble.core.type.name.RecVar;
import org.scribble.core.type.name.Role;
import org.scribble.core.type.name.Substitutions;
import org.scribble.ext.assrt.core.job.AssrtCore;
import org.scribble.ext.assrt.core.type.formula.AssrtAFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtBFormula;
import org.scribble.ext.assrt.core.type.name.AssrtAnnotDataName;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.ext.assrt.core.type.session.AssrtCoreRecVar;
import org.scribble.ext.assrt.core.type.session.local.AssrtCoreLRecVar;
import org.scribble.ext.assrt.core.type.session.local.AssrtCoreLTypeFactory;
import org.scribble.ext.assrt.core.visit.global.AssrtCoreGTypeInliner;

	
public class AssrtCoreGRecVar extends AssrtCoreRecVar<Global, AssrtCoreGType>
		implements AssrtCoreGType
{
	protected AssrtCoreGRecVar(CommonTree source, RecVar rv,
			List<AssrtAFormula> sexprs)
	{
		super(source, rv, sexprs);
	}

	@Override
	public AssrtCoreGType disamb(AssrtCore core, Map<AssrtIntVar, DataName> env)
	{
		return new AssrtCoreGRecVar(getSource(), this.recvar,
				this.stateexprs.stream().map(x -> (AssrtAFormula) x.disamb(env))
						.collect(Collectors.toList()));
	}

	@Override
	public AssrtCoreGType substitute(AssrtCore core, Substitutions subs)
	{
		return this;
	}

	@Override
	public AssrtCoreGType inline(AssrtCoreGTypeInliner v)
	{
		RecVar rv = v.getInlinedRecVar(this.recvar);
		return ((AssrtCoreGTypeFactory) v.core.config.tf.global)
				.AssrtCoreGRecVar(getSource(), rv, this.stateexprs);
	}

	@Override
	public AssrtCoreGType pruneRecs(AssrtCore core)
	{
		return this;
	}

	@Override
	public AssrtCoreLRecVar projectInlined(AssrtCore core, Role self,
			AssrtBFormula f, Map<Role, Set<AssrtIntVar>> known,
			Map<RecVar, LinkedHashMap<AssrtIntVar, Role>> located,
			List<AssrtAnnotDataName> phantom, AssrtBFormula phantAss)  // CHECKME: phantoms discarded for recvar?
	{
		Iterator<Entry<AssrtIntVar, Role>> it = located.get(this.recvar).entrySet()
				.iterator();
		List<AssrtAFormula> sexprs = this.stateexprs.stream()
				.filter(x ->
					{
						Role r = it.next().getValue();
						return r == null || r.equals(self);  // TODO null
					})
				.collect(Collectors.toList());
		return ((AssrtCoreLTypeFactory) core.config.tf.local).AssrtCoreLRecVar(null,
				this.recvar, sexprs);
	}

	@Override
	public List<AssrtAnnotDataName> collectAnnotDataVarDecls(
			Map<AssrtIntVar, DataName> env)
	{
		return Collections.emptyList();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof AssrtCoreGRecVar))
		{
			return false;
		}
		return super.equals(obj);  // Does canEquals
	}
	
	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof AssrtCoreGRecVar;
	}

	@Override
	public int hashCode()
	{
		int hash = 2411;
		hash = 31*hash + super.hashCode();
		return hash;
	}
}
