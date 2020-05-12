package org.scribble.ext.assrt.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.scribble.ext.assrt.core.type.formula.AssrtBFormula;
import org.scribble.ext.assrt.core.type.formula.AssrtLogFormula;
import org.scribble.ext.assrt.core.type.name.AssrtIntVar;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.QuantifiedFormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class JavaSmtWrapper
{
	private static JavaSmtWrapper instance = null;
	
	public final ShutdownManager shutdownManager;
	
	// TODO: MOVE to config
	private static final int SOLVER_TIMEOUT_MS = 10000;

	private final FormulaManager fmanager;    

	public final BooleanFormulaManager bfm; 
	public final IntegerFormulaManager ifm; 
	public final QuantifiedFormulaManager qfm;

	private final LogManager logger; 
	private final SolverContext context;
	
	protected JavaSmtWrapper() throws InvalidConfigurationException
	{
		// TODO: maybe use parameter solver.z3.usePhantomReferences to garbage collect Z3 formula references
		Configuration config = Configuration.fromCmdLineArguments(new String[]{"--usePhantomReferences=true"}); 
				//defaultConfiguration(); // fromCmdLineArguments([]);
	    logger = BasicLogManager.create(config);
	    shutdownManager = ShutdownManager.create();
	    
	    // SolverContext is a class wrapping a solver context.
	    // Solver can be selected either using an argument or a configuration option
	    // inside `config`.
	    this.context = SolverContextFactory.createSolverContext(
	        config, logger, shutdownManager.getNotifier(), Solvers.Z3);
	    // solver.z3.usePhantomReferences
	    
	    fmanager = context.getFormulaManager();
	    this.qfm = fmanager.getQuantifiedFormulaManager(); 
	    this.bfm = fmanager.getBooleanFormulaManager();
	    this.ifm = fmanager.getIntegerFormulaManager();
	}
	
	public static JavaSmtWrapper getInstance() 
	{
		try
		{
			if (instance == null)
			{
				instance = new JavaSmtWrapper();
			}
		}
		catch (InvalidConfigurationException e)
		{
			//System.err.println("InvalidConfigurationException: " + e.getMessage());
			throw new RuntimeException(e);
		}

		return instance;
	}
	
	public AssrtLogFormula addFormula(AssrtBFormula f1, AssrtBFormula f2) //throws AssertionParseException
	{
		BooleanFormula formula = this.bfm.and( f1.getJavaSmtFormula(), f2.getJavaSmtFormula());
		Set<AssrtIntVar> vars = new HashSet<>(f1.getIntVars()); 
		vars.addAll(f2.getIntVars());
		return new AssrtLogFormula(formula, vars);
	}
	
	public Boolean isSat(AssrtBFormula assertionFormula, AssrtLogFormula context)
	{
		return isSat(buildFormula(assertionFormula, context));
	}

	public Boolean isSat(BooleanFormula formula)
	{
		boolean isUnsat = false;
		
		//try
		{
			final ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1);
			executor.setRemoveOnCancelPolicy(true);
			Runnable solverInterruptThread = () -> shutdownManager.requestShutdown("Timeout");
			
			try (ProverEnvironment prover = this.context
					.newProverEnvironment(ProverOptions.GENERATE_UNSAT_CORE))  // Cf. ProverOptions.GENERATE_MODELS, timeout too short
			{
				prover.addConstraint(formula);

				ScheduledFuture<?> timeoutFuture = executor.schedule(solverInterruptThread, JavaSmtWrapper.SOLVER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
				isUnsat = prover.isUnsat();
				cancelTimeoutFuture(timeoutFuture);
			}

			catch (SolverException e)
			{
				//this.logger.logUserException(Level.INFO, e, "Error thrown by the SMT solver");
				throw new RuntimeException("[assrt] SMT solver error: ", e);
			}
			catch (InterruptedException e)
			{
				//this.logger.logUserException(Level.INFO, e, "The formula was interrupted. Took too long");
				throw new RuntimeException("[assrt] SMT solver timeout: ", e);
			}
			finally
			{
				executor.shutdown();
			}
		}
		/*catch (AssertionParseException e) {
			this.logger.logUserException(Level.INFO, e, "The assertion is not a valid Z3 expression");
			System.err.print("The assertion is not a valid Z3 expression" + e.getMessage()); 
		}*/
		
		return  !isUnsat;
	}

	private BooleanFormula buildFormula(AssrtBFormula assertionFormula, AssrtLogFormula context) //throws AssertionParseException
	{
		BooleanFormula currFormula;
		currFormula = (BooleanFormula) assertionFormula.getJavaSmtFormula();
		List<IntegerFormula> contextVars; 
		List<IntegerFormula> vars;
		Set<String> setVars = assertionFormula.getIntVars()
				.stream().map(v -> v.toString()).collect(Collectors.toSet()); 
				
		BooleanFormula formula = null; 

		if (context != null)  // FIXME: refactor (shouldn't be null?)
		{
			BooleanFormula contextF = (BooleanFormula) context.getJavaSmtFormula(); 
			
			if (!contextF.equals(this.bfm.makeTrue()))  // FIXME: factor out as a constant
			{
				setVars.removeAll(context.getIntVars().stream().map(v -> v.toString()).collect(Collectors.toList()));
				vars = this.makeVars(new ArrayList<>(setVars));
				
				contextVars = this.makeVars(new ArrayList<>(context.getIntVars()
																			.stream().map(v -> v.toString()).collect(Collectors.toSet())
						));
				
				formula = vars.isEmpty()
						? this.qfm.forall(contextVars, this.bfm.implication(contextF, currFormula))
						: this.qfm.forall(contextVars, this.bfm.implication(contextF, this.qfm.exists(vars, currFormula)));  
					
			}		
		}

		if (formula == null)
		{
			
				vars = this.makeVars(new ArrayList<String>(setVars));
				formula = vars.isEmpty()? 
							currFormula:	
							this.qfm.exists(vars, currFormula);
		}
		return formula;
	}
	
	private void cancelTimeoutFuture(ScheduledFuture<?> timeoutFuture)
	{
		try
		{
			timeoutFuture.cancel(true);
		}
		catch (Throwable ignore)
		{
		}
	}
	    
	/*
	TODO: Should we close the ShutdownManger  
	public void close() {
		if (this.context != null) {
			// this.executor.shutdownNow();
			this.shutdownManager.requestShutdown("Closed");
	        this.context.close();
	       }
	    }
	    
	*/
	 
	public List<IntegerFormula> makeVars(List<String> vars)
	{
		return vars.stream().map(v -> this.ifm.makeVariable(v)).collect(Collectors.toList()); 
	}
}
