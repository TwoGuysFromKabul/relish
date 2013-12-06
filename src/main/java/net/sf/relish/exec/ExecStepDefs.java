package net.sf.relish.exec;

import static net.sf.relish.RelishUtil.*;
import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.relish.DataFormat;
import net.sf.relish.RelishException;
import net.sf.relish.RelishUtil;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * Cucumber step definitions for the exec feature which allows interacting with external applications
 */
public final class ExecStepDefs {

	private final Map<String, ProcessResult> processResultByName = new HashMap<String, ExecStepDefs.ProcessResult>();

	/**
	 * Executes the application with no console input
	 * 
	 * @param appName
	 *            Name given to the application for reference in the DSL
	 * @param commandLine
	 *            Command line to execute. The command line args are space delimited. If an arg contains a space escape it with '\\'. For example: 'ls a b c'
	 *            will execute ls with args 'a', 'b', and 'c' but 'ls a\\ b\\ c' will execute ls with one arg of 'a b c'.
	 */
	@Given("^application \"(\\S.*)\" is executed with command line \"(\\S.*)\"$")
	public void applicationIsExecutedWithCommandLine(String appName, String commandLine) {

		exec(appName, commandLine, null);
	}

	/**
	 * Executes the application using the output of a previously run application as the input. This is equivalent to piping the output from the previous
	 * application into the input to this application.
	 * 
	 * @param appName
	 *            Name given to the application for reference in the DSL
	 * @param commandLine
	 *            Command line to execute. The command line args are space delimited. If an arg contains a space escape it with '\\'. For example: 'ls a b c'
	 *            will execute ls with args 'a', 'b', and 'c' but 'ls a\\ b\\ c' will execute ls with one arg of 'a b c'.
	 * @param pipeFromApp
	 *            Reference name of the previously executed application to use the output from.
	 */
	@Given("^application \"(\\S.*)\" is executed with command line \"(\\S.*)\" with input that is the output of the \"(\\S.*)\" application$")
	public void applicationIsExecutedWithCommandLineWithInputThatIsTheOutputOfTheApplication(String appName, String commandLine, String pipeFromApp) {

		ProcessResult result = getProcessResult(pipeFromApp);
		String input = result.output;
		exec(appName, commandLine, input);
	}

	/**
	 * Executes the application using the specified text as the console input.
	 * 
	 * @param appName
	 *            Name given to the application for reference in the DSL
	 * @param commandLine
	 *            Command line to execute. The command line args are space delimited. If an arg contains a space escape it with '\\'. For example: 'ls a b c'
	 *            will execute ls with args 'a', 'b', and 'c' but 'ls a\\ b\\ c' will execute ls with one arg of 'a b c'.
	 * @param input
	 *            Text to use as the console input to the application
	 */
	@Given("^application \"(\\S.*)\" is executed with command line \"(\\S.*)\" with input that is this text:$")
	public void applicationIsExecutedWithCommandLineWithInputThatIsThisText(String appName, String commandLine, String input) {

		RelishUtil.validateNotEmpty("input", input);
		exec(appName, commandLine, input);
	}

	/**
	 * Validates the application's exit code. Typically 0 means success and anything else is an error.
	 * 
	 * @param appName
	 *            Name given to the application for reference in the DSL
	 * @param exitCode
	 *            The expected exit code
	 */
	@Then("^application \"(\\S.*)\" exit code should be ([\\d]+)$")
	public void applicationExitCodeShouldBe(String appName, int exitCode) {

		ProcessResult result = getProcessResult(appName);
		assertThat(result.exitCode, equalTo(exitCode), "Exit code does not match for application %s", appName);
	}

	/**
	 * Validates the output of the application matches a specified regular expression
	 * 
	 * @param appName
	 *            Name given to the application for reference in the DSL
	 * @param outputMatchingRegex
	 *            The regular expression used to validaste the output
	 */
	@Then("^application \"(\\S.*)\" output should match this text:$")
	public void applicationOutputShouldMatchThisText(String appName, String outputMatchingRegex) {

		ProcessResult result = getProcessResult(appName);
		String output = DataFormat.TEXT.normalizeText(result.output);
		outputMatchingRegex = DataFormat.TEXT.normalizeRegex(outputMatchingRegex);
		assertThat(output, matches(outputMatchingRegex), "Output does not match for application %s", appName);
	}

	private ProcessResult getProcessResult(String appName) {

		ProcessResult result = processResultByName.get(appName);
		if (result == null) {
			throw new RelishException("Application %s has not been executed", appName);
		}
		return result;
	}

	private void exec(String appName, String commandLine, String input) {

		try {
			String[] command = buildProcessCommand(commandLine);
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.redirectErrorStream(true);
			Process process = builder.start();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			StreamCopyThread copyIn = new StreamCopyThread(process.getInputStream(), baos);
			copyIn.start();
			StreamCopyThread copyOut = null;
			if (input != null) {
				copyOut = new StreamCopyThread(new ByteArrayInputStream(input.getBytes()), process.getOutputStream());
				copyOut.start();
			}

			int exitCode = process.waitFor();
			if (copyOut != null) {
				copyOut.join();
			}
			copyIn.join();
			String output = new String(baos.toByteArray());

			processResultByName.put(appName, new ProcessResult(exitCode, output));

		} catch (Exception e) {
			throw new RelishException(e, "Failed to execute application: %s", commandLine);
		}
	}

	private String[] buildProcessCommand(String commandLine) {

		commandLine = commandLine.replace("\\ ", "\u0000");
		String[] command = RelishUtil.quickSplit(commandLine, ' ');
		for (int i = 0; i < command.length; i++) {
			command[i] = command[i].replace('\u0000', ' ');
		}
		return command;
	}

	private static class ProcessResult {

		private final int exitCode;
		private final String output;

		public ProcessResult(int exitCode, String output) {
			this.exitCode = exitCode;
			this.output = output;
		}
	}

	private static class StreamCopyThread extends Thread {

		private final InputStream in;
		private final OutputStream out;

		public StreamCopyThread(InputStream in, OutputStream out) {
			this.in = new BufferedInputStream(in);
			this.out = new BufferedOutputStream(out);
		}

		/**
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				for (int i = in.read(); i >= 0; i = in.read()) {
					out.write(i);
				}
			} catch (Exception ignore) {
			} finally {
				RelishUtil.closeQuietly(in);
				RelishUtil.closeQuietly(out);
			}
		}
	}
}
