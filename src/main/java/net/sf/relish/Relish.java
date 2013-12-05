package net.sf.relish;

import java.util.ArrayList;
import java.util.List;

import cucumber.api.cli.Main;

/**
 * Main entry point for relish
 */
public final class Relish {

	public static void main(String[] args) throws Throwable {

		if (args.length == 0) {
			args = new String[] { "-h" };
		} else {
			List<String> argList = new ArrayList<String>();
			for (String arg : args) {
				argList.add(arg);
			}

			if (!argList.contains("-f") && !argList.contains("--format")) {
				argList.add(0, "-f");
				argList.add(1, "html:html-report");
				argList.add(2, "-f");
				argList.add(3, "pretty");
			}

			if (!argList.contains("-s") && !argList.contains("--strict") && !argList.contains("--no-strict")) {
				argList.add(0, "-s");
			}

			argList.add(0, "-g");
			argList.add(1, "net.sf.relish");

			args = argList.toArray(args);
		}

		Main.main(args);
	}
}
