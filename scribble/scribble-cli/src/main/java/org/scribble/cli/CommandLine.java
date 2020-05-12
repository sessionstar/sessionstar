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
package org.scribble.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.scribble.ast.Module;
import org.scribble.ast.ProtoDecl;
import org.scribble.ast.global.GProtoDecl;
import org.scribble.codegen.java.JEndpointApiGenerator;
import org.scribble.codegen.java.callbackapi.CBEndpointApiGenerator3;
import org.scribble.core.job.CoreArgs;
import org.scribble.core.job.CoreContext;
import org.scribble.core.job.CoreFlags;
import org.scribble.core.model.endpoint.EGraph;
import org.scribble.core.model.global.SGraph;
import org.scribble.core.type.kind.Local;
import org.scribble.core.type.name.GProtoName;
import org.scribble.core.type.name.LProtoName;
import org.scribble.core.type.name.ProtoName;
import org.scribble.core.type.name.Role;
import org.scribble.core.visit.global.InlinedProjector;
import org.scribble.job.Job;
import org.scribble.job.JobContext;
import org.scribble.main.Main;
import org.scribble.main.resource.locator.DirectoryResourceLocator;
import org.scribble.main.resource.locator.ResourceLocator;
import org.scribble.util.AntlrSourceException;
import org.scribble.util.Pair;
import org.scribble.util.RuntimeScribException;
import org.scribble.util.RuntimeScribSyntaxException;
import org.scribble.util.ScribException;
import org.scribble.util.ScribParserException;
import org.scribble.util.ScribUtil;

/**
 * A Scribble extension should override newCLFlags, newCLArgParser, newMain,
 * parseJobArgs, doValidationTasks, tryNonBarrierTask and tryBarrierTask as
 * appropriate
 * 
 * @author anon
 */
public class CommandLine
{
	protected final CLFlags flags;
	protected final List<Pair<String, String[]>> args; 
			// left = CLFlags String constant, right = flag args (if any) -- ordered by parsing order

	public CommandLine(String... args)
	{
		this.flags = newCLFlags();
		try
		{
			this.args = newCLArgParser(this.flags, args).getParsed();
		}
		catch (CommandLineException e)
		{
			System.err.println(e.getMessage());
			System.exit(1);
			throw new RuntimeException("Dummy");  // Already exited above
		}
	}

	public static void main(String[] args)
			throws CommandLineException, AntlrSourceException
	{
		new CommandLine(args).run();
	}
	
	// A Scribble extension should override newCLFlags/CLArgParser/Main/CoreArgs as appropriate
	protected CLFlags newCLFlags()
	{
		return new CLFlags();
	}

	// A Scribble extension should override newCLFlags/CLArgParser/Main/CoreArgs as appropriate
	protected CLArgParser newCLArgParser(CLFlags flags, String[] args)
	{
		return new CLArgParser(flags, args);
	}

	// A Scribble extension should override newCLFlags/CLArgParser/Main/CoreArgs as appropriate
	protected Main newMain() throws ScribParserException, ScribException
	{
		CoreArgs args = newCoreArgs();
		/*if (hasFlag(CLFlags.INLINE_MAIN_MOD_FLAG))
		{
			String inline = getUniqueFlagArgs(CLFlags.INLINE_MAIN_MOD_FLAG)[0];
			//return new Main(inline, args);
			throw new RuntimeException("[TODO] Refactor inline via tmp directory/file creation: " + inline);
		}
		else*/  // FIXME: refactor inline via tmp directory/file creation
		{
			List<Path> impaths = parseImportPaths();
			ResourceLocator locator = new DirectoryResourceLocator(impaths);
			Path mainpath = parseMainPath();
			return new Main(locator, mainpath, args);
		}
	}
	
	// A Scribble extension should override newCLFlags/CLArgParser/Main/CoreArgs as appropriate
	protected CoreArgs newCoreArgs()
	{
		return new CoreArgs(parseCoreFlags());
	}
	
	protected Set<CoreFlags> parseCoreFlags()
	{
		Set<CoreFlags> flags = new HashSet<>();
		Map<String, CoreFlags> tmp = new HashMap<>();
		tmp.put(CLFlags.VERBOSE_FLAG, CoreFlags.VERBOSE);
		tmp.put(CLFlags.FAIR_FLAG, CoreFlags.FAIR);
		// TODO: -spin
		tmp.put(CLFlags.NO_VALIDATION_FLAG, CoreFlags.NO_VALIDATION);
		tmp.put(CLFlags.NO_PROGRESS_FLAG, CoreFlags.NO_PROGRESS);
		tmp.put(CLFlags.LTSCONVERT_MIN_FLAG, CoreFlags.MIN_EFSM);
		tmp.put(CLFlags.OLD_WF_FLAG, CoreFlags.OLD_WF);
		tmp.put(CLFlags.NO_LOCAL_CHOICE_SUBJECT_CHECK_FLAG,
				CoreFlags.NO_LCHOICE_SUBJ_CHECK);
		tmp.put(CLFlags.NO_ACCEPT_CORRELATION_CHECK_FLAG,
				CoreFlags.NO_ACC_CORRELATION_CHECK);
		tmp.keySet().forEach(x -> { if (hasFlag(x)) { flags.add(tmp.get(x)); } } );
		return flags;
	}
	
	protected boolean hasFlag(String flag)
	{
		return this.args.stream().anyMatch(x -> x.left.equals(flag));
	}

	protected String[] getUniqueFlagArgs(String flag)
	{
		return this.args.stream().filter(x -> x.left.equals(flag)).findAny()
				.get().right;
	}

	// AntlrSourceException super of ScribbleException -- needed for, e.g., AssrtCoreSyntaxException
	// A Scribble extension should override runValidationTasks/tryBarrierTask/tryNonBarrierTask as appropriate
	protected void runValidationTasks(Job job) 
			throws AntlrSourceException, ScribParserException,  // Latter in case needed by subclasses
				CommandLineException
	{
		job.runPasses();
		job.getCore().runPasses();
	}

	// A Scribble extension should override runValidationTasks/tryBarrierTask/tryNonBarrierTask as appropriate
	// TODO: rename, barrier misleading (sounds like a sync)
	protected void tryNonBarrierTask(Job job, Pair<String, String[]> task)
			throws CommandLineException, ScribException
	{
		switch (task.left)  // Flag lab (CLFlags String constants)
		{
			case CLFlags.PROJECT_FLAG:
				printProjection(job, task.right);
				break;
			case CLFlags.EFSM_FLAG:
				outputEGraph(job, task.right, true, true, false);
				break;
			case CLFlags.EFSM_PNG_FLAG:
				outputEGraph(job, task.right, true, true, true);
				break;
			case CLFlags.SGRAPH_FLAG:
				outputSGraph(job, task.right, true, false);
				break;
			case CLFlags.SGRAPH_PNG_FLAG:
				outputSGraph(job, task.right, true, true);
				break;
				
			// CHECKME: -unfair needs to be barrier-ed on syntactic WF? (o/w unfair-transform graph building may crash, e.g., bad.wfchoice.enabling.threeparty.Test02)
			// Currently, "construction-on-demand" CoreContext getters will still cause these to be attempted even if syntactic WF fails
			case CLFlags.VALIDATION_EFSM_FLAG:
				outputEGraph(job, task.right, false, true, false);
				break;
			case CLFlags.VALIDATION_EFSM_PNG_FLAG:
				outputEGraph(job, task.right, false, true, true);
				break;
			case CLFlags.UNFAIR_EFSM_FLAG:
				outputEGraph(job, task.right, false, false, false);
				break;
			case CLFlags.UNFAIR_EFSM_PNG_FLAG:
				outputEGraph(job, task.right, false, false, true);
				break;
			case CLFlags.UNFAIR_SGRAPH_FLAG:
				outputSGraph(job, task.right, false, false);
				break;
			case CLFlags.UNFAIR_SGRAPH_PNG_FLAG:
				outputSGraph(job, task.right, false, true);
				break;

			default:
				throw new RuntimeException("Shouldn't get here: " + task.left);
				// Bad flag should be caught by CLArgParser
		}
	}

	// A Scribble extension should override runValidationTasks/tryBarrierTask/tryNonBarrierTask as appropriate
	// TODO: rename, barrier misleading (sounds like a sync)
	protected void tryBarrierTask(Job job,
			Pair<String, String[]> task) throws ScribException, CommandLineException
	{
		switch (task.left)
		{
			case CLFlags.SESSION_API_GEN_FLAG:
				outputEndpointApi(job, task.right, true, false, false);
				break;
			case CLFlags.STATECHAN_API_GEN_FLAG:
				outputEndpointApi(job, task.right, false, true, false);
				break;
			case CLFlags.API_GEN_FLAG:
				outputEndpointApi(job, task.right, true, true, false);
				break;
			case CLFlags.EVENTDRIVEN_API_GEN_FLAG:
				outputEndpointApi(job, task.right, false, true, true);  // FIXME: currently need to gen sess API separately?
				break;
			default:
				throw new RuntimeException("Shouldn't get here: " + task.left);
					// Bad flag should be caught by CLArgParser
		}
	}

	public void run() throws CommandLineException, 
			AntlrSourceException  // For JUnit harness (ScribException)
	{
		try  // TODO: refactor, flatten try's
		{
			try
			{
				runTasks();
			}
			catch (ScribException e)  // Wouldn't need to do this if not Runnable (so maybe change)
			{
				if (hasFlag(CLFlags.JUNIT_FLAG)  // JUnit harness looks for an exception
						|| hasFlag(CLFlags.VERBOSE_FLAG))  // Output full stack trace for -V
				{
					throw e;
				}
				else  // Otherwise, print exception message and suppress trace
				{
					System.err.println(e.getMessage());
					System.exit(1);
				}
			}
		}
		catch (ScribParserException | CommandLineException e)
		{
			System.err.println(e.getMessage());  // No need to give full stack trace, even for debug, for CommandLine errors
			System.exit(1);
		}
		catch (RuntimeScribException e)
		{
			if (e instanceof RuntimeScribSyntaxException)  // Needed for test harness
			{
				throw e;
			}
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	protected void runTasks()
			throws AntlrSourceException, ScribParserException, CommandLineException
			// AntlrSourceException super of ScribbleException
	{
		Main mc = newMain();  // Represents current instance of tooling for given CL args
		Job job = mc.newJob();  // A Job is some series of passes performed on each Module in the MainContext (e.g., cf. Job::runVisitorPass)
		ScribException err = null;
		try { runValidationTasks(job); } catch (ScribException x) { err = x; }
		for (Pair<String, String[]> a : this.args)
		{
			CLFlag flag = this.flags.explicit.get(a.left);  // null for CLFlags.MAIN_MOD_FLAG
			if (a.left.equals(CLFlags.MAIN_MOD_FLAG) || !flag.enact)
			{
				continue;
			}
			if (!flag.barrier)
			{
				try { tryNonBarrierTask(job, a); }
				catch (ScribException x) { if (err == null) { err = x; } }
			}
			else
			{
				if (err == null)
				{
					try { tryBarrierTask(job, a); }
					catch (ScribException x) { err = x; }
				}
			}
		}
		if (err != null)
		{
			throw err;
		}
	}

	// TODO: option to write to file, like API gen
	private void printProjection(Job job, String[] args)
			throws CommandLineException, ScribException
	{
		JobContext jobc = job.getContext();
		for (int i = 0; i < args.length; i += 2)
		{
			GProtoName fullname = checkGlobalProtocolArg(jobc, args[i]);
			Role self = checkRoleArg(jobc, fullname, args[i+1]);
			Map<ProtoName<Local>, Module> projs = job.getProjections(fullname, self);
			LProtoName rootFullname = InlinedProjector.getFullProjectionName(fullname,
					self);
			Module root = projs.get(rootFullname);
			System.out.println(
					"\nProjection modules for " + fullname + "@" + self + ":\n\n" + root);
			for (ProtoName<Local> pfullname : projs.keySet())
			{
				// CHECKME: projection decl name is currently *compound* full name (not simple name), OK?
				if (!pfullname.equals(rootFullname))
				{
					System.out.println("\n" + projs.get(pfullname));
				}
			}
		}
	}

	// dot/aut text output
	// forUser: true means for API gen and general user info (may be minimised), false means for validation (non-minimised, fair or unfair)
	// (forUser && !fair) should not hold, i.e. unfair doesn't make sense if forUser
	private void outputEGraph(Job job, String[] args, boolean forUser,
			boolean fair, boolean draw) throws ScribException, CommandLineException
	{
		JobContext jobc = job.getContext();
		
			GProtoName fullname = checkGlobalProtocolArg(jobc, args[0]);
			Role role = checkRoleArg(jobc, fullname, args[1]);
			EGraph fsm = getEGraph(job, fullname, role, forUser, fair);
			if (draw)
			{
				String png = args[2];
				runDot(fsm.toDot(), png);
			}
			else // print
			{
				String out = hasFlag(CLFlags.AUT_FLAG) 
						? fsm.toAut()
						: fsm.toDot();  // default: dot
				System.out.println("\n" + out);  // Endpoint graphs are "inlined" (a single graph is built)
			}
	}

	private void outputSGraph(Job job, String[] args, boolean fair,
			boolean draw) throws ScribException, CommandLineException
	{
		JobContext jobc = job.getContext();
		{
			GProtoName fullname = checkGlobalProtocolArg(jobc, args[0]);
			SGraph model = getSGraph(job, fullname, fair);
			if (draw)
			{
				String png = args[1];
				runDot(model.toDot(), png);
			}
			else // print
			{
				String out = hasFlag(CLFlags.AUT_FLAG) 
						? model.toAut()
						: model.toDot();
				System.out.println("\n" + out);
			}
		}
	}

	private void outputEndpointApi(Job job, String[] args, boolean sess,
			boolean schan, boolean cb) throws ScribException, CommandLineException
	{
		JobContext jobc = job.getContext();
		JEndpointApiGenerator jgen = new JEndpointApiGenerator(job);  // FIXME: refactor (generalise -- use new API)
		{
			GProtoName fullname = checkGlobalProtocolArg(jobc, args[0]);
			if (sess)
			{
				Map<String, String> out = jgen.generateSessionApi(fullname);
				outputClasses(out);
			}
			if (schan)  // CHECKME: does not implicitly generate sess API?
			{
				Role self = checkRoleArg(jobc, fullname, args[1]);
				if (cb)
				{
					CBEndpointApiGenerator3 cbgen = new CBEndpointApiGenerator3(job,
							fullname, self, hasFlag(CLFlags.STATECHAN_SUBTYPES_FLAG));
					Map<String, String> out = cbgen.build();
					outputClasses(out);
				}
				else
				{
					Map<String, String> out = jgen.generateStateChannelApi(fullname,
							self, hasFlag(CLFlags.STATECHAN_SUBTYPES_FLAG));
					outputClasses(out);
				}
			}
		}
	}

  // Endpoint graphs are "inlined", so only a single graph is built (cf. projection output)
	private EGraph getEGraph(Job job, GProtoName fullname, Role role,
			boolean forUser, boolean fair)
			throws ScribException, CommandLineException
	{
		JobContext jobc = job.getContext();
		CoreContext corec = job.getCore().getContext();
		GProtoDecl gpd = jobc.getMainModule()
				.getGProtoDeclChild(fullname.getSimpleName());
		if (gpd == null || !gpd.getHeaderChild().getRoleDeclListChild().getRoles()
				.contains(role))
		{
			throw new CommandLineException("Bad FSM construction args: "
					+ gpd + ", " + role);
		}
		EGraph graph;
		if (forUser)  // The (possibly minimised) user-output EFSM for API gen
		{
			graph = hasFlag(CLFlags.LTSCONVERT_MIN_FLAG)
					? corec.getMinimisedEGraph(fullname, role)
					: corec.getEGraph(fullname, role);
		}
		else  // The (possibly unfair-transformed) internal EFSM for validation
		{
			graph = //(!this.args.containsKey(ArgFlag.FAIR) && !this.args.containsKey(ArgFlag.NO_LIVENESS))  // Cf. GlobalModelChecker.getEndpointFSMs
					!fair
					? corec.getUnfairEGraph(fullname, role) 
					: corec.getEGraph(fullname, role);
		}
		if (graph == null)
		{
			throw new RuntimeScribException("Shouldn't see this: " + fullname);
					// Should be caught by some earlier failure
		}
		return graph;
	}

	private SGraph getSGraph(Job job, GProtoName fullname, boolean fair)
			throws ScribException
	{
		CoreContext corec = job.getCore().getContext();
		SGraph model = fair 
				? corec.getSGraph(fullname)
				: corec.getUnfairSGraph(fullname);
		if (model == null)
		{
			throw new RuntimeScribException("Shouldn't see this: " + fullname);
					// Should be caught by some earlier failure
		}
		return model;
	}

	// classes: filepath -> class source
	protected void outputClasses(Map<String, String> classes)
			throws ScribException
	{
		Consumer<String> f;
		if (hasFlag(CLFlags.API_OUTPUT_DIR_FLAG))
		{
			String dir = getUniqueFlagArgs(CLFlags.API_OUTPUT_DIR_FLAG)[0];
			f = path -> { ScribUtil.handleLambdaScribbleException(() ->
					{
						String tmp = dir + "/" + path;
						if (hasFlag(CLFlags.VERBOSE_FLAG))
						{
							System.out.println("[DEBUG] Writing to: " + tmp);
						}
						ScribUtil.writeToFile(tmp, classes.get(path));
						return null;
					});
			};
		}
		else
		{
			f = path -> { System.out.println(path + ":\n" + classes.get(path)); };
		}
		classes.keySet().forEach(f);
	}

	protected Path parseMainPath()
	{
		String path = getUniqueFlagArgs(CLFlags.MAIN_MOD_FLAG)[0];
		return Paths.get(path);
	}

	protected List<Path> parseImportPaths()
	{
		if (!hasFlag(CLFlags.IMPORT_PATH_FLAG))
		{
			return Collections.emptyList();
		}
		String impaths = getUniqueFlagArgs(CLFlags.IMPORT_PATH_FLAG)[0];
		return Arrays.stream(impaths.split(File.pathSeparator))
				.map(x -> Paths.get(x)).collect(Collectors.toList());
	}

	protected static GProtoName checkGlobalProtocolArg(JobContext jobc,
			String simpname) throws CommandLineException
	{
		GProtoName simpgpn = new GProtoName(simpname);
		Module main = jobc.getMainModule();
		if (!main.hasGProtoDecl(simpgpn))
		{
			throw new CommandLineException("Global protocol not found: " + simpname);
		}
		ProtoDecl<?> pd = main.getGProtoDeclChild(simpgpn);
		if (pd == null || !pd.isGlobal())
		{
			throw new CommandLineException("Global protocol not found: " + simpname);
		}
		if (pd.isAux())  // CHECKME: maybe don't check for all, e.g. -project
		{
			throw new CommandLineException(
					"Invalid aux protocol specified as root: " + simpname);
		}
		return new GProtoName(jobc.job.config.main, simpgpn);  // TODO: take Job param instead of Jobcontext
	}

	protected static Role checkRoleArg(JobContext jobc, GProtoName fullname,
			String rolename) throws CommandLineException
	{
		ProtoDecl<?> pd = jobc.getMainModule()
				.getGProtoDeclChild(fullname.getSimpleName());
		Role role = new Role(rolename);
		if (!pd.getHeaderChild().getRoleDeclListChild().getRoles().contains(role))
		{
			throw new CommandLineException(
					"Role not declared for " + fullname + ": " + role);
		}
		return role;
	}

	// CHECKME: relocate?
	protected static void runDot(String dot, String png)
			throws ScribException, CommandLineException
	{
		File tmp = null;
		try
		{
			tmp = File.createTempFile(png, ".tmp");
			String tmpName = tmp.getAbsolutePath();				
			ScribUtil.writeToFile(tmpName, dot);
			String[] res = ScribUtil.runProcess("dot", "-Tpng", "-o" + png, tmpName);
			System.out.print(!res[1].isEmpty() ? res[1] : res[0]);  // already "\n" terminated
		}
		catch (IOException e)
		{
			throw new CommandLineException(e);
		}
		finally
		{
			if (tmp != null)
			{
				tmp.delete();
			}
		}
	}
}
