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
package org.scribble.core.lang.local;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.core.job.Core;
import org.scribble.core.lang.ProtoMod;
import org.scribble.core.lang.Protocol;
import org.scribble.core.lang.SubprotoSig;
import org.scribble.core.model.endpoint.EGraph;
import org.scribble.core.model.endpoint.EState;
import org.scribble.core.type.kind.Local;
import org.scribble.core.type.kind.NonRoleParamKind;
import org.scribble.core.type.name.LProtoName;
import org.scribble.core.type.name.MemberName;
import org.scribble.core.type.name.RecVar;
import org.scribble.core.type.name.Role;
import org.scribble.core.type.session.local.LRecursion;
import org.scribble.core.type.session.local.LSeq;
import org.scribble.core.visit.STypeInliner;
import org.scribble.core.visit.STypeUnfolder;
import org.scribble.core.visit.local.EGraphBuilder;
import org.scribble.core.visit.local.LDoPruner;
import org.scribble.core.visit.local.LRoleDeclAndDoArgPruner;
import org.scribble.core.visit.local.SubprotoExtChoiceSubjFixer;
import org.scribble.util.Constants;
import org.scribble.util.ScribException;

public class LProtocol extends Protocol<Local, LProtoName, LSeq>
		implements LNode
{
	public final Role self;

	public LProtocol(CommonTree source, List<ProtoMod> mods,
			LProtoName fullname, List<Role> rs, Role self,
			List<MemberName<? extends NonRoleParamKind>> ps, LSeq def)
	{
		super(source, mods, fullname, rs, ps, def);
		this.self = self;
	}

	// For factoring between G/LProtocol (e.g., SubprotoExtChoiceSubjFixer::visitProtocol) -- worth it? or confusing?
	@Override
	public LProtocol reconstruct(CommonTree source,
			List<ProtoMod> mods, LProtoName fullname, List<Role> rs,
			//Role self,  // CHECKME: reconstruct pattern not working here?
			List<MemberName<? extends NonRoleParamKind>> ps, LSeq def)
	{
		return reconstruct(source, mods, fullname, rs, this.self, ps, def);  // N.B. this.self
	}

	public LProtocol reconstruct(CommonTree source,
			List<ProtoMod> mods, LProtoName fullname, List<Role> rs,
			Role self, List<MemberName<? extends NonRoleParamKind>> ps, LSeq def)
	{
		return new LProtocol(source, mods, fullname, rs, self, ps, def);
	}
	
	// N.B. Do directly does visitSeq on target def -- here is only top-level entry
	@Override
	public LProtocol getInlined(STypeInliner<Local, LSeq> v)
	{
		SubprotoSig sig = new SubprotoSig(this);
		v.pushSig(sig);

		LSeq inlined = v.visitSeq(this.def);
		RecVar rv = v.getInlinedRecVar(sig);
		LRecursion rec = v.core.config.tf.local.LRecursion(null, rv, inlined);  // CHECKME: or protodecl source?
		LSeq seq = v.core.config.tf.local.LSeq(null, Arrays.asList(rec));
		LSeq def = v.core.config.vf.<Local, LSeq>RecPruner().visitSeq(seq);
		/*//CHECKME: necessary for LProtocol/Projection? cf. global
		Set<Role> used = def.gather(new RoleGatherer<Global, GSeq>()::visit)
				.collect(Collectors.toSet());
		List<Role> rs = this.roles.stream().filter(x -> used.contains(x))  // Prune role decls
				.collect(Collectors.toList());*/
		return reconstruct(getSource(), this.mods, this.fullname, this.roles,
				this.self, this.params, def);  // CHECKME: or null source?
	}
	
	@Override
	public LProtocol unfoldAllOnce(STypeUnfolder<Local, LSeq> v)
	{
		LSeq unf = v.visitSeq(this.def);
		return reconstruct(getSource(), this.mods, this.fullname, this.roles,
				this.self, this.params, unf);
	}

	public void checkReachability(Core core) throws ScribException
	{
		this.def.visitWith(core.config.vf.local.ReachabilityChecker());
	}
	
	// Implicitly means Subproto visitor
	public LProtocol pruneRoleDeclsAndDoArgs(LRoleDeclAndDoArgPruner v)
	{
		return v.visitLProtocol(this);
	}
	
	// Implicitly means Subproto visitor
	public LProtocol pruneDos(LDoPruner v)
	{
		return v.visitLProtocol(this);
	}
	
	public LProtocol fixExtChoiceSubjs(SubprotoExtChoiceSubjFixer v)
	{
		return (LProtocol) v.visitProtocol(this);
	}

	public EGraph toEGraph(Core core)
	{
		if (this.def.isEmpty())  // Empty Seq special case for top-level -- in general, Seq must be non-empty, cf. LSeq::buildGraph entry/exit
		{
			EState s = core.config.mf.local.EState(Collections.emptySet());
			return new EGraph(s, s);  // TODO: refactor constructor inside mf
		}
		EGraphBuilder b = core.config.vf.local.EGraphBuilder(core);
		this.def.visitWithNoThrow(b);
		return b.finalise();
	}
	
	@Override
	public String toString()
	{
		return this.mods.stream().map(x -> x.toString() + " ")
				.collect(Collectors.joining(" ")) + "local " + super.toString();
	}
	
	@Override
	protected String rolesToString()
	{
		return "("
				+ this.roles.stream()
						.map(x -> (x.equals(this.self)
								? Constants.SELF_KW
								: Constants.ROLE_KW) + " " + x)
						.collect(Collectors.joining(", "))
				+ ")";
	}

	@Override
	public int hashCode()
	{
		int hash = 11;
		hash = 31 * hash + super.hashCode();
		hash = 31 * hash + this.self.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof LProtocol))
		{
			return false;
		}
		LProtocol them = (LProtocol) o;
		return super.equals(o)  // Does canEquals
				&& this.self.equals(them.self); 
	}

	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof LProtocol;
	}
}
