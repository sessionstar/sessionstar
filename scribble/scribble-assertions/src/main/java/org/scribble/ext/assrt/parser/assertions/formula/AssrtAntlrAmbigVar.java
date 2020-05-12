package org.scribble.ext.assrt.parser.assertions.formula;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.ext.assrt.core.type.formula.AssrtAmbigVarFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtFormulaFactory;
import org.scribble.ext.assrt.parser.assertions.AssrtAntlrToFormulaParser;

public class AssrtAntlrAmbigVar
{
	public static AssrtAmbigVarFormula parseAmbigVar(
			AssrtAntlrToFormulaParser parser, CommonTree root)
	{
		return AssrtFormulaFactory.AssrtAmbigVar(root.getChild(0).getText());
	}
}
