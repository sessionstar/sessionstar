package org.scribble.ext.assrt.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.scribble.core.lang.global.GProtocol;
import org.scribble.core.type.name.DataName;
import org.scribble.ext.assrt.core.job.AssrtCore;
import org.scribble.ext.assrt.core.lang.global.AssrtCoreGProtocol;
import org.scribble.ext.assrt.core.type.formula.AssrtBFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtBinFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtQuantifiedIntVarsFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtSmtFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtTrueFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtUnintPredicateFormula;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.scribble.util.ScribException;
import org.scribble.util.ScribUtil;

// "Native" Z3 -- not Z3 Java API
public class Z3Wrapper
{

	// Based on CommandLine::runDot, JobContext::runAut, etc
	public static boolean checkSat(AssrtCore core, GProtocol intermed,
			Set<AssrtBFormula> bforms)  //throws ScribbleException
	{
		bforms = bforms.stream().filter(f -> !f.equals(AssrtTrueFormula.TRUE))
				.collect(Collectors.toSet());
		if (bforms.isEmpty())
		{
			return true;
		}

		Map<AssrtIntVar, DataName> sorts = /*((AssrtCoreGProtocol) core.getContext()
																				.getInlined(intermed.fullname)).type
																				.getSortEnv();*/
				((AssrtCoreGProtocol) core.getContext()
						.getInlined(intermed.fullname)).getSortEnv();
		String smt2 = toSmt2(intermed, bforms, sorts);

		core.verbosePrintln(
				"[assrt-core] Running Z3 on:\n  " + smt2.replaceAll("\\n", "\n  "));

		return checkSat(smt2);
	}

	// smt2 is the full Z3 source
	private static boolean checkSat(String smt2) //throws ScribbleException
	{
		File tmp;
		try
		{
			tmp = File.createTempFile("gpd.header.name", ".smt2.tmp");
			try
			{
				String tmpName = tmp.getAbsolutePath();				
				ScribUtil.writeToFile(tmpName, smt2);
				String[] res = ScribUtil.runProcess("z3", tmpName);
				String trim = res[0].trim();
				if (trim.equals("sat"))  // FIXME: factor out
				{
					return true;
				}
				else if (trim.equals("unsat"))
				{
					return false;
				}
				else
				{
					throw new RuntimeException("[assrt] Z3 error: " + Arrays.toString(res));
				}
			}
			catch (ScribException e)
			{
				throw new RuntimeException(e);
			}
			finally
			{
				tmp.delete();
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	// fs shouldn't be empty (but OK)
	private static String toSmt2(GProtocol intermed, Set<AssrtBFormula> bforms,
			Map<AssrtIntVar, DataName> env)
	{
		String smt2 = "";
		List<String> rs = intermed.roles.stream().map(Object::toString).sorted()
				.collect(Collectors.toList());
		smt2 += IntStream
				.range(0, rs.size()).mapToObj(i -> "(declare-const " + rs.get(i)
						+ " Int)\n(assert (= " + rs.get(i) + " " + i + "))\n")
				.collect(Collectors.joining(""));
						// FIXME: make a Role sort?

		Set<AssrtUnintPredicateFormula> preds = bforms.stream()
				.flatMap(f -> getUnintPreds.func.apply(f).stream())
				.collect(Collectors.toSet());
		smt2 += preds.stream()
				.map(p -> "(declare-fun " + p.name + " ("
						+ IntStream.range(0, p.args.size()).mapToObj(i -> ("(Int)"))
								.collect(Collectors.joining(" "))
						+ ") Bool)\n")
				.collect(Collectors.joining(""));
		if (preds.stream().anyMatch(p -> p.name.equals("port")))  // FIXME: factor out
		{
			smt2 += "(assert (forall ((p Int) (r Int)) (=> (port p r) (open p r))))\n";
		}
		
		return smt2
				+ bforms.stream().map(f -> "(assert " + f.toSmt2Formula(env) + ")\n")
						.collect(Collectors.joining())
				+ "(check-sat)\n"
				+ "(exit)";
	}

	// FIXME: move to utils?
	public static final 
			RecursiveFunctionalInterface<
					Function<AssrtSmtFormula<?>, Set<AssrtUnintPredicateFormula>>> 
			getUnintPreds = 
			new RecursiveFunctionalInterface<
					Function<AssrtSmtFormula<?>, Set<AssrtUnintPredicateFormula>>>()
	{{
		this.func = x ->
		{
			if (x instanceof AssrtBinFormula)
			{
				AssrtBinFormula<?> bf = (AssrtBinFormula<?>) x;
									return Stream.of(bf.getLeft(), bf.getRight())
											.flatMap(y -> this.func.apply(y).stream())
											.collect(Collectors.toSet());
			}
			else if (x instanceof AssrtQuantifiedIntVarsFormula)
			{
				return this.func.apply(((AssrtQuantifiedIntVarsFormula) x).expr);
			}
			else if (x instanceof AssrtUnintPredicateFormula)
			{
				return Stream.of((AssrtUnintPredicateFormula) x)
						.collect(Collectors.toSet());
						// Nested predicates not possible
			}
			else
			{
				return Collections.emptySet();
			}
		};
	}};
}

	/*private static Set<AssrtUnPredicateFormula> getUnPredicates(AssrtSmtFormula<?> f) 
	{
		Recursive<Function<AssrtSmtFormula<?>, Set<AssrtUnPredicateFormula>>> rec = new Recursive<>();
		rec.func = ff ->
		{
			if (ff instanceof AssrtBinaryFormula)
			{
				AssrtBinaryFormula<?> bf = (AssrtBinaryFormula<?>) ff;
				return Stream.of(bf.getLeft(), bf.getRight()).flatMap(x -> rec.func.apply(x).stream()).collect(Collectors.toSet());
			}
			else if (ff instanceof AssrtQuantifiedIntVarsFormula)
			{
				return rec.func.apply(((AssrtQuantifiedIntVarsFormula) ff).expr);
			}
			else if (ff instanceof AssrtUnPredicateFormula)
			{
				return Stream.of((AssrtUnPredicateFormula) ff).collect(Collectors.toSet());  // Nested predicates not possible
			}
			else
			{
				return Collections.emptySet();
			}
		};
		return rec.func.apply(f);
	}*/
