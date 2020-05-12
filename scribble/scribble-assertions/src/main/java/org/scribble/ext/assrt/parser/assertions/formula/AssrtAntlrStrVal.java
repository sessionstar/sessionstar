package org.scribble.ext.assrt.parser.assertions.formula;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.ext.assrt.core.type.formula.AssrtFormulaFactory;
import org.scribble.ext.assrt.core.type.formula.AssrtStrValFormula;
import org.scribble.ext.assrt.parser.assertions.AssrtAntlrToFormulaParser;

public class AssrtAntlrStrVal
{
	public static AssrtStrValFormula parseStrVal(AssrtAntlrToFormulaParser parser,
			CommonTree root)
	{
		return AssrtFormulaFactory.AssrtStrVal(root.getChild(0).getText());
	}
}
