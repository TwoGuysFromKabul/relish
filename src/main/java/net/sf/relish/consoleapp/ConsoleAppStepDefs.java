package net.sf.relish.consoleapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.relish.RelishException;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;

/**
 * Cucumber steps for managing console apps including starting and stopping console apps within the current project and from a JAR file.
 */
public final class ConsoleAppStepDefs {

	private final Map<String, Method> stopMethodByClassName = new HashMap<String, Method>();

	/**
	 * Runs after each relish scenario
	 */
	@After
	public void after() throws Exception {

		Iterator<Method> iter = stopMethodByClassName.values().iterator();
		while (iter.hasNext()) {
			iter.next().invoke(null);
			iter.remove();
		}
	}

	/**
	 * Starts a console app using a fully qualified java class name either within the current project or from a JAR file by invoking its
	 * <code>public static void main(String[])</code method. If being run from a JAR file then all JARs in the same directory with the specified JAR are
	 * included on the class path. Command line arguments may also be specified. The command line arguments are space delimited. You may surround an argument
	 * with double quotes if it contains a space. The java class must contain a <code>public static void stop()</code> method which will be invoked when the
	 * application is stopped.
	 * 
	 * @param javaClass
	 *            The fully qualified name of the class to run the <code>main</code> method in
	 * @param jarFile
	 *            If specified this is the JAR to load the javaClass from. If not specified the javaClass is loaded from the current project.
	 * @param argString
	 *            Space delimited command line arguments. If an argument contains a space surround it with double quotes.
	 */
	@Given("^console app \"(\\S.+\\S)\" is running(?: from JAR \"(\\S.*)\")?(?: with args: (\\S.*))?$")
	public void javaClassIsRunning(String javaClass, String jarFile, String argString) throws Exception {

		if (stopMethodByClassName.containsKey(javaClass)) {
			throw new RelishException("You may not start console app %s because it is already running", javaClass);
		}

		String[] args = argString == null || argString.isEmpty() ? new String[0] : new ArgsParser(argString).parse();

		ClassLoader classLoader = getClassLoader(jarFile);

		Class<?> clazz = classLoader.loadClass(javaClass);

		Method mainMethod = clazz.getMethod("main", String[].class);
		if (!Modifier.isStatic(mainMethod.getModifiers())) {
			throw new RelishException("Method 'main(String[])' in class %s must be static", javaClass);
		}

		mainMethod.invoke(null, (Object) args);

		Method stopMethod = clazz.getMethod("stop");
		if (!Modifier.isStatic(stopMethod.getModifiers())) {
			throw new RelishException("Method 'stop()' in class %s must be static", javaClass);
		}
		stopMethodByClassName.put(javaClass, stopMethod);
	}

	/**
	 * Stops a console app previously started with "console app "..." is running ...". The class's <code>public static void stop()</code> method is invoked.
	 * 
	 * @param javaClass
	 *            The fully qualified name of the running class to stop.
	 */
	@When("^console app \"(\\S.+\\S)\" is stopped$")
	public void javaClassIsStopped(String javaClass) throws Exception {

		Method stopMethod = stopMethodByClassName.remove(javaClass);
		if (stopMethod == null) {
			throw new RelishException("You cannot stop console app %s because it is not running", javaClass);
		}
		stopMethod.invoke(null);
	}

	private ClassLoader getClassLoader(String jarFile) throws Exception {

		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		if (parent == null) {
			parent = ConsoleAppStepDefs.class.getClassLoader();
		}

		if (jarFile == null) {
			return parent;
		}

		File jar = new File(jarFile);
		if (!jar.isFile()) {
			throw new FileNotFoundException("JAR file not found: " + jarFile);
		}

		File dir = jar.getAbsoluteFile().getParentFile();
		File[] files = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});

		URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			urls[i] = files[i].toURI().toURL();
		}

		return new URLClassLoader(urls, parent);
	}

	private static final class ArgsParser {

		private enum State {
			PRE_QUOTES, IN_QUOTES, POST_QUOTES
		}

		private final String argString;
		private int i;
		private State state = State.PRE_QUOTES;

		public ArgsParser(String argString) {
			this.argString = argString;
		}

		String[] parse() {

			List<String> args = new ArrayList<String>();
			while (i < argString.length()) {

				String arg = nextString();
				if (arg != null && !arg.isEmpty()) {
					args.add(arg);
				}
			}
			return args.isEmpty() ? null : args.toArray(new String[args.size()]);
		}

		private String nextString() {

			state = State.PRE_QUOTES;
			StringBuilder argBuilder = new StringBuilder();
			for (; i < argString.length(); i++) {

				char c = argString.charAt(i);
				boolean isWhitespace = Character.isWhitespace(c);

				if (state == State.PRE_QUOTES) {
					if (c == '"') {
						// if we get quotes in the middle of a string throw an exception
						if (argBuilder.length() > 0) {
							throw new RelishException("Unexpected quotes found at position %d in args: %s", i + 1, argString);
						}
						state = State.IN_QUOTES;
					} else if (isWhitespace) {
						// if we get whitespace at the beginning of a string ignore it, else break out of the loop
						if (argBuilder.length() > 0) {
							break;
						}
					} else {
						argBuilder.append(c);
					}
				} else if (state == State.IN_QUOTES) {
					if (c == '"') {
						state = State.POST_QUOTES;
					} else {
						argBuilder.append(c);
					}
				} else if (state == State.POST_QUOTES) {
					// if we get text after closing quotes throw an exception
					if (!isWhitespace) {
						throw new RelishException("Unexpected character found at position %d in args: %s", i, argString);
					}
					break;
				}
			}

			return argBuilder.toString();
		}
	}
}
