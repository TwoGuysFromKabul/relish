package net.sf.relish;

import java.util.Arrays;

/**
 * This is built into the testapp.jar that is in the project. It is used to test running a console app from a jar file.
 */
public class TestMainClass {

	public static void main(String[] args) {
		System.out.printf("Running %s.main(%s)\n", TestMainClass.class.getName(), Arrays.toString(args));
	}

	public static void stop() {
		System.out.printf("Running %s.stop()\n", TestMainClass.class.getName());
	}
}
