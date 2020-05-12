package org.scribble.ext.assrt.core.model.stp.action;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scribble.core.model.ModelFactory;
import org.scribble.core.type.name.MsgId;
import org.scribble.core.type.name.Role;
import org.scribble.core.type.session.Payload;
import org.scribble.ext.assrt.core.model.endpoint.action.AssrtCoreESend;
import org.scribble.ext.assrt.core.model.global.action.AssrtCoreSSend;
import org.scribble.ext.assrt.core.type.formula.AssrtAFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtBFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtIntVarFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtSmtFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtTrueFormula;

public class AssrtStpESend extends AssrtCoreESend implements AssrtStpEAction
{
	public final Map<AssrtIntVarFormula, AssrtSmtFormula<?>> sigma;
	public final AssrtBFormula A;  // aliases this.ass

	public AssrtStpESend(ModelFactory mf, Role peer, MsgId<?> mid,
			Payload payload, Map<AssrtIntVarFormula, AssrtSmtFormula<?>> sigma,
			AssrtBFormula A)
	{
		super(mf, peer, mid, payload, A, Collections.emptyList(),
				Collections.emptyList(), AssrtTrueFormula.TRUE);
		this.sigma = Collections.unmodifiableMap(sigma);
		this.A = A;
	}

	@Override
	public Map<AssrtIntVarFormula, AssrtSmtFormula<?>> getSigma()
	{
		return this.sigma;
	}
	
	@Override
	public AssrtStpESend toTrueAssertion()  // FIXME: for model building, currently need send assertion to match (syntactical equal) receive assertion (which is always True) to be fireable
	{
		throw new RuntimeException("Shouldn't get in here: " + this);
	}

	@Override
	public AssrtStpEReceive toDual(Role self)
	{
		throw new RuntimeException("Shouldn't get in here: " + this);
	}

	@Override
	public AssrtCoreSSend toGlobal(Role self)
	{
		throw new RuntimeException("Shouldn't get in here: " + this);
	}

	@Override
	public List<AssrtAFormula> getStateExprs()
	{
		throw new RuntimeException("Shouldn't get in here: " + this);
	}
	
	//[ label="C|HELLO(x: int)@\"(x > 5)\"{x:y+1}" ];
	@Override
	public String toString()
	{
		return this.obj + getCommSymbol() + this.mid + this.payload + "@\"" + this.A
				+ "\""
				+ (this.sigma.isEmpty() ? ""
						: "{" + this.sigma.entrySet().stream()
								.map(e -> e.getKey() + ":" + e.getValue())
								.collect(Collectors.joining("")) + "}");
	}
	
	@Override
	public int hashCode()
	{
		int hash = 7841;
		hash = 31 * hash + super.hashCode();
		hash = 31 * hash + this.sigma.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (!(o instanceof AssrtStpESend))
		{
			return false;
		}
		AssrtStpESend as = (AssrtStpESend) o;
		return super.equals(o)  // Does canEquals
				&& this.sigma.equals(as.sigma);
				//&& this.A.equals(as.A);  // Done by via this.ass
	}

	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof AssrtStpESend;
	}
}
