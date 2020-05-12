/**
 * Copyright 2008 The Scribble Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.scribble.ext.assrt.core.lang.local;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.core.job.Core;
import org.scribble.core.lang.ProtoMod;
import org.scribble.core.lang.local.LProjection;
import org.scribble.core.model.endpoint.EGraph;
import org.scribble.core.model.endpoint.EState;
import org.scribble.core.type.kind.Local;
import org.scribble.core.type.kind.NonRoleParamKind;
import org.scribble.core.type.name.GProtoName;
import org.scribble.core.type.name.LProtoName;
import org.scribble.core.type.name.MemberName;
import org.scribble.core.type.name.Role;
import org.scribble.core.type.session.local.LSeq;
import org.scribble.core.visit.STypeInliner;
import org.scribble.ext.assrt.core.model.endpoint.AssrtCoreEGraphBuilder;
import org.scribble.ext.assrt.core.model.endpoint.AssrtCoreEModelFactory;
import org.scribble.ext.assrt.core.type.formula.AssrtAFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtBFormula;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.ext.assrt.core.type.session.local.AssrtCoreLEnd;
import org.scribble.ext.assrt.core.type.session.local.AssrtCoreLType;

public class AssrtCoreLProjection extends LProjection  // N.B. not an AssrtCoreLProtocol ... FIXME CoreContext G/LProtocol hardcoding
{
	// TODO: duplicated from AssrtCoreGProtocol -- refactor
	public final AssrtCoreLType type;
	public final LinkedHashMap<AssrtIntVar, AssrtAFormula> statevars;
	public final AssrtBFormula assertion;  // non-null (True)
	
	public final LinkedHashMap<AssrtIntVar, AssrtAFormula> phantom;

	public AssrtCoreLProjection(List<ProtoMod> mods, LProtoName fullname,
			List<Role> rs, Role self, List<MemberName<? extends NonRoleParamKind>> ps,
			GProtoName global, AssrtCoreLType type,
			LinkedHashMap<AssrtIntVar, AssrtAFormula> svars,
			AssrtBFormula ass, LinkedHashMap<AssrtIntVar, AssrtAFormula> phantom)
	{
		super(mods, fullname, rs, self, ps, global, null);
		this.type = type;
		this.statevars = new LinkedHashMap<>(svars);  // TODO: unmod
		this.assertion = ass;
		this.phantom = new LinkedHashMap<>(phantom);
	}

	@Override
	public LProjection reconstruct(CommonTree source, List<ProtoMod> mods,
			LProtoName fullname, List<Role> rs, Role self,
			List<MemberName<? extends NonRoleParamKind>> ps, LSeq body)
	{
		throw new RuntimeException("Deprecated for " + getClass() + ":\n" + this);
	}

	public LProjection reconstruct(CommonTree source, List<ProtoMod> mods,
			LProtoName fullname, List<Role> rs, Role self,
			List<MemberName<? extends NonRoleParamKind>> ps, AssrtCoreLType type,
			LinkedHashMap<AssrtIntVar, AssrtAFormula> svars, AssrtBFormula ass,
			LinkedHashMap<AssrtIntVar, AssrtAFormula> phantom)
	{
		return new AssrtCoreLProjection(mods, fullname, rs, this.self, ps,
				this.global, type, svars, ass, phantom);
	}

	// Pre: stack.peek is the sig for the calling Do (or top-level entry)
	// i.e., it gives the roles/args at the call-site
	@Override
	public AssrtCoreLProjection getInlined(STypeInliner<Local, LSeq> v)
	{
		throw new RuntimeException("[TODO]:\n" + this);
	}

	@Override
	public EGraph toEGraph(Core core)
	{
		if (this.type.equals(AssrtCoreLEnd.END))
		{
			EState s = ((AssrtCoreEModelFactory) core.config.mf.local)
					.EState(Collections.emptySet());
			return new EGraph(s, s);  // TODO: refactor constructor inside mf
		}
		AssrtCoreEGraphBuilder b = (AssrtCoreEGraphBuilder) core.config.vf.local
				.EGraphBuilder(core);  // N.B. currently, does not actually implement the visitor pattern
		return b.build(this.statevars, this.assertion, this.type, this.phantom);
	}
	
	@Override
	public String toString()
	{
		return this.mods.stream().map(x -> x.toString() + " ")
				.collect(Collectors.joining())
				+ "local protocol " + this.fullname.getSimpleName()
				+ paramsToString()
				+ rolesToString()
				+ " projects " + this.global
				+ " @<" + this.statevars.entrySet().stream()
						//.map(x -> x.getKey() + " := \"" + x.getValue() + "\"")
						.map(x -> x.getKey() + " := " + x.getValue())
						.collect(Collectors.joining(", "))
				+ ">"
				+ "[" + this.phantom.entrySet().stream()
						.map(x -> x.getKey() + " := " + x.getValue())
						.collect(Collectors.joining(", "))
				+ "] "
				+ this.assertion //"\"" + this.assertion + "\""
				+ " {\n" + this.type + "\n}";
	}

	@Override
	public int hashCode()
	{
		int hash = 3167;
		hash = 31 * hash + this.mods.hashCode();
		hash = 31 * hash + this.fullname.hashCode();
		hash = 31 * hash + this.roles.hashCode();
		hash = 31 * hash + this.params.hashCode();
		hash = 31 * hash + this.global.hashCode();
		hash = 31 * hash + this.type.hashCode();
		hash = 31 * hash + this.statevars.hashCode();
		hash = 31 * hash + this.assertion.hashCode();
		hash = 31 * hash + this.phantom.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtCoreLProjection))
		{
			return false;
		}
		AssrtCoreLProjection them = (AssrtCoreLProjection) o;
		return them.canEquals(this) && this.mods.equals(them.mods)
				&& this.fullname.equals(them.fullname) && this.roles.equals(them.roles)
				&& this.self.equals(them.self) && this.params.equals(them.params)
				&& this.global.equals(them.global) && this.type.equals(them.type)
				&& this.statevars.equals(them.statevars)
				&& this.assertion.equals(them.assertion)
				&& this.phantom.equals(them.phantom);
	}

	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof AssrtCoreLProjection;
	}
}
