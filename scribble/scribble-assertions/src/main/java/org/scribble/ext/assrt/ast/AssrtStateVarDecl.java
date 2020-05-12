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
package org.scribble.ext.assrt.ast;

import org.antlr.runtime.Token;
import org.scribble.ast.ParamDecl;
import org.scribble.ast.name.NameNode;
import org.scribble.ast.name.simple.RoleNode;
import org.scribble.del.DelFactory;
import org.scribble.ext.assrt.ast.name.simple.AssrtIntVarNameNode;
import org.scribble.ext.assrt.core.type.kind.AssrtIntVarKind;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.ext.assrt.del.AssrtDelFactory;
import org.scribble.util.ScribException;
import org.scribble.visit.AstVisitor;

// Names that are declared in a protocol header (roles and parameters -- not the protocol name though)
// RoleKind or (NonRole)ParamKind
public class AssrtStateVarDecl extends ParamDecl<AssrtIntVarKind>
{
	//public static final int NAMENODE_CHILD_INDEX = 0;  // Cf. NameDeclNode
	public static final int ASSRT_STATEVAREXPR_CHILD_INDEX = 1;

	public static final int ASSRT_ROLE_CHILD_INDEX = 2;  // null means "all roles" ("global" statevar) -- back compat
	// Cf. AssrtGMsgTransfer.DST_CHILD_INDEX, RoleNode

	// ScribTreeAdaptor#create constructor
	public AssrtStateVarDecl(Token t)
	{
		super(t);
	}

	// Tree#dupNode constructor
	public AssrtStateVarDecl(AssrtStateVarDecl node)
	{
		super(node);
	}
	
	@Override
	public AssrtIntVarNameNode getNameNodeChild()
	{
		return (AssrtIntVarNameNode) getRawNameNodeChild();  // NameDeclNode.NAMENODE_CHILD_INDEX == 0
	}
	
	public AssrtAExprNode getStateVarExprChild()
	{
		return (AssrtAExprNode) getChild(ASSRT_STATEVAREXPR_CHILD_INDEX);
	}

	// null means "all roles" ("global" statevar) -- back compat
	public RoleNode getRoleNodeChild()  // Cf. AssrtGMsgTransfer.getDestinationChild
	{
		return (RoleNode) getChild(ASSRT_ROLE_CHILD_INDEX);
	}

	public boolean hasRoleNodeChild()
	{
		//return getChild(ASSRT_ROLE_CHILD_INDEX) != null;
		return getChildCount() > 2;
	}

	@Override
	public void addScribChildren(NameNode<AssrtIntVarKind> name)
	{
		throw new RuntimeException("Deprecated for " + getClass() + ": " + this);
	}

	// "add", not "set"
	public void addScribChildren(NameNode<AssrtIntVarKind> name,
			AssrtAExprNode sexpr, RoleNode role)
	{
		// Cf. above getters and Scribble.g children order
		super.addScribChildren(name);
		addChild(sexpr);
		addChild(role);
	}
	
	@Override
	public AssrtStateVarDecl dupNode()
	{
		return new AssrtStateVarDecl(this);
	}
	
	@Override
	public void decorateDel(DelFactory df)
	{
		((AssrtDelFactory) df).AssrtStateVarDecl(this);
	}

	@Override
	public ParamDecl<AssrtIntVarKind> reconstruct(NameNode<AssrtIntVarKind> name)
	{
		throw new RuntimeException("Deprecated for " + getClass() + ": " + this);
	}

	public ParamDecl<AssrtIntVarKind> reconstruct(NameNode<AssrtIntVarKind> name,
			AssrtAExprNode sexpr, RoleNode role)
			// Always a "simple" name (e.g., like Role), but Type/Sig names are not SimpleNames
	{
		AssrtStateVarDecl dup = dupNode();
		dup.addScribChildren(name, sexpr, role);
		dup.setDel(del());  // No copy
		return dup;
	}
	
	@Override
	public ParamDecl<AssrtIntVarKind> visitChildren(AstVisitor v)
			throws ScribException
	{
		NameNode<AssrtIntVarKind> svar = 
				visitChildWithClassEqualityCheck(this, getNameNodeChild(), v);
		AssrtAExprNode sexpr = (AssrtAExprNode) visitChild(getStateVarExprChild(), v);
		RoleNode role = getRoleNodeChild();
		if (role != null)
		{
			role = (RoleNode) visitChild(role, v);
		}
		return reconstruct(svar, sexpr, role);
	}

	@Override
	public AssrtIntVar getDeclName()
	{
		return getNameNodeChild().toName();
	}

	@Override
	public String getKeyword()
	{
		throw new RuntimeException("Deprecated for " + getClass() + ": " + this);
	}
	
	@Override
	public String toString()
	{
		RoleNode role = getRoleNodeChild();
		return getDeclName().toString()
				+ (role == null ? " := " : " :" + role + " = ")
				+ getStateVarExprChild();
	}
}


/*
























public abstract class AssrtStateVarDecl extends NameDeclNode<AssrtIntVarKind>
{
	//public static final int NAMENODE_CHILD_INDEX = 0;
	public static final int NAMENODE_CHILD_INDEX = 0;

	// ScribTreeAdaptor#create constructor
	public AssrtStateVarDecl(Token t)
	{
		super(t);
	}

	// Tree#dupNode constructor
	public AssrtStateVarDecl(AssrtStateVarDecl node)
	{
		super(node);
	}
	
	@Override
	public abstract NameNode<AssrtIntVarKind> getNameNodeChild();  // Always a "simple" name (e.g., like Role), but Type/Sig names are not SimpleNames

	public abstract String getKeyword();

	public abstract AssrtStateVarDecl dupNode();

	public AssrtStateVarDecl reconstruct(NameNode<AssrtIntVarKind> name)  // Always a "simple" name (e.g., like Role), but Type/Sig names are not SimpleNames
	{
		AssrtStateVarDecl dup = dupNode();
		dup.addScribChildren(name);
		dup.setDel(del());  // No copy
		return dup;
	}
	
	@Override
	public AssrtStateVarDecl visitChildren(AstVisitor nv)
			throws ScribException
	{
		NameNode<AssrtIntVarKind> name = 
				visitChildWithClassEqualityCheck(this, getNameNodeChild(), nv);
		return reconstruct(name);
	}
	
	@Override
	public String toString()
	{
		return getKeyword() + " " + getDeclName().toString();
	}
}*/
