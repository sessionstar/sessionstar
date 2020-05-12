package org.scribble.ext.assrt.parser.assertions;

import org.antlr.runtime.tree.CommonTree;
import org.scribble.ext.assrt.core.type.formula.AssrtFalseFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtSmtFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtTrueFormula;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrAmbigVar;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrArithExpr;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrBoolExpr;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrCompExpr;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrIntVal;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrNegExpr;
import org.scribble.ext.assrt.parser.assertions.formula.AssrtAntlrStrVal;
import org.scribble.parser.ScribAntlrWrapper;

// "Manually" parsing instead of using an ANTLR Adaptor (cf. ScribTreeAdaptor),
// because going directly to "types" API.
// (i.e. no middleman AST, where the (CommonTree) children are set implicitly by
// the Adaptor -- we directly return AssrtSmtFormula, not a Tree.)
// Embedded by AssrtAntlrToScribParser.
public class AssrtAntlrToFormulaParser
{
	private static AssrtAntlrToFormulaParser instance = null;

	protected AssrtAntlrToFormulaParser()
	{
		
	}

	public static AssrtAntlrToFormulaParser getInstance()
	{
		if (AssrtAntlrToFormulaParser.instance == null)
		{
			AssrtAntlrToFormulaParser.instance = new AssrtAntlrToFormulaParser();
		}
		return AssrtAntlrToFormulaParser.instance;
	}

	// Parses directly to Formula "types" (not ast)
	// ct should be the first child of the root node of the assertion subtree (i.e., AssrtAntlrGMessageTransfer.getAssertionChild.getChild(0))
	// Does not parse ROOT directly -- AssrtAntlr[...] methods extract the children
	public AssrtSmtFormula<?> parse(CommonTree ct) //throws AssertionsParseException
	{
		ScribAntlrWrapper.checkForAntlrErrors(ct);
		
		String lab = ct.token.getText();
		switch (lab)  // Cf. AssrtScribTreeAdaptor
		{
		case "ROOT":
			return parse((CommonTree) ct.getChild(0));  // TODO: deprecate, Assertions should offer only AFormula and BFormula parsing

			// Static "parse" methods basically in lieu of actual Antlr (CommonTree) nodes for annot syntax (cf. ScribNode, AssrtScribTreeAdaptor) 
		case "BOOLEXPR":
			return AssrtAntlrBoolExpr.parseBoolExpr(this, ct);
		case "COMPEXPR":
			return AssrtAntlrCompExpr.parseCompExpr(this, ct);
		case "ARITHEXPR":
			return AssrtAntlrArithExpr.parseArithExpr(this, ct);
		case "NEGEXPR":
			return AssrtAntlrNegExpr.parseNegExpr(this, ct);
		//case "UNFUN":     return AssrtAntlrUnFun.parseUnFun(this, ct);
		case "INTVAR":  // FIXME: rename Ambig
			//return AssrtAntlrIntVar.parseIntVar(this, ct);
			return AssrtAntlrAmbigVar.parseAmbigVar(this, ct);  // FIXME: deprecate ambig (all should be IntVar now)
		case "INTVAL":
			return AssrtAntlrIntVal.parseIntVal(this, ct);
		case "NEGINTVAL":
			return AssrtAntlrIntVal.parseNegIntVal(this, ct);
		case "FALSE":
			return AssrtFalseFormula.FALSE;
		case "TRUE":
			return AssrtTrueFormula.TRUE;
		case "STRVAL":
			return AssrtAntlrStrVal.parseStrVal(this, ct);

		default:
			throw new RuntimeException("[assrt] Unexpected ANTLR node type: " + lab);
		}
	}
}
