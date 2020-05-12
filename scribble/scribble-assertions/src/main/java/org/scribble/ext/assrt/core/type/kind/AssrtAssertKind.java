package org.scribble.ext.assrt.core.type.kind;

import org.scribble.core.type.kind.AbstractKind;
import org.scribble.core.type.kind.ModuleMemberKind;

public class AssrtAssertKind extends AbstractKind implements ModuleMemberKind
{
	public static final AssrtAssertKind KIND = new AssrtAssertKind();
	
	protected AssrtAssertKind()
	{
		throw new RuntimeException("[TODO]");
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof AssrtAssertKind))
		{
			return false;
		}
		return ((AssrtAssertKind) o).canEquals(this);
	}
	
	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof AssrtAssertKind;
	}
}
