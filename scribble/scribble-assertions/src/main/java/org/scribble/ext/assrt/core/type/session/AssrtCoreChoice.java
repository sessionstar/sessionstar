package org.scribble.ext.assrt.core.type.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.core.type.kind.ProtoKind;
import org.scribble.core.type.name.DataName;
import org.scribble.core.type.name.Role;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;

// TODO: rename directed choice
public abstract class AssrtCoreChoice<K extends ProtoKind, 
			B extends AssrtCoreSType<K, B>>  // Without Seq complication, take kinded Type directly
		extends AssrtCoreSTypeBase<K, B>
{
	public final Role role;  // N.B. "fixed" dst for global, "relative" peer for local -- CHECKME: why dst for global?
			// CHECKME: deprecate role?
	public final AssrtCoreActionKind<K> kind;
	public final Map<AssrtCoreMsg, B> cases;
	
	// Pre: cases.size() > 1
	protected AssrtCoreChoice(CommonTree source, Role role,
			AssrtCoreActionKind<K> kind, LinkedHashMap<AssrtCoreMsg, B> cases)
	{
		super(source);
		this.role = role;
		this.kind = kind;
		this.cases = Collections.unmodifiableMap(cases);
	}
	
	@Override
	public <T> Stream<T> assrtCoreGather(
			Function<AssrtCoreSType<K, B>, Stream<T>> f)
	{
		return Stream.concat(f.apply(this),
				this.cases.values().stream().flatMap(x -> x.assrtCoreGather(f)));
	}

	@Override
	public Map<AssrtIntVar, DataName> getBoundSortEnv(Map<AssrtIntVar, DataName> ctxt)
	{
		Map<AssrtIntVar, DataName> res = this.cases.keySet().stream()
				.flatMap(x -> x.pay.stream())
				.collect(Collectors.toMap(x -> x.var, x -> x.data));
		Map<AssrtIntVar, DataName> tmp = new HashMap<>(ctxt);
		tmp.putAll(res);
		res.putAll(this.cases.values().stream()
				.flatMap(x -> x.getBoundSortEnv(tmp).entrySet().stream())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
		return res;
	}

	public abstract AssrtCoreActionKind<K> getKind();
	
	@Override
	public int hashCode()
	{
		int hash = 29;
		hash = 31 * hash + this.role.hashCode();
		hash = 31 * hash + this.kind.hashCode();
		hash = 31 * hash + this.cases.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtCoreChoice))
		{
			return false;
		}
		AssrtCoreChoice<?, ?> them = (AssrtCoreChoice<?, ?>) o; 
		return super.equals(o)  // Checks canEquals -- implicitly checks kind
				&& this.role.equals(them.role) && this.kind.equals(them.kind)
				&& this.cases.equals(them.cases);
	}
	
	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof AssrtCoreChoice;
	}
	
	protected String casesToString()
	{
		String s = this.cases.entrySet().stream()
				.map(e -> e.getKey() + "." + e.getValue())
				.collect(Collectors.joining(", "));
		s = (this.cases.size() > 1)
				? "{ " + s + " }"
				: ":" + s;
		return s;
	}
}
