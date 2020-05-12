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
package org.scribble.core.type.kind;

public class DataKind extends AbstractKind implements PayElemKind, ImportKind, ModuleMemberKind
{
	public static final DataKind KIND = new DataKind();
	
	protected DataKind()
	{
		super("Data");
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (!(o instanceof DataKind))
		{
			return false;
		}
		return ((DataKind) o).canEquals(this);
	}
	
	@Override
	public boolean canEquals(Object o)
	{
		return o instanceof DataKind;
	}
}
