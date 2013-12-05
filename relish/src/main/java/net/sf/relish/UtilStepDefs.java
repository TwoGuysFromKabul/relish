package net.sf.relish;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Various utility Cucumber step defs
 */
public final class UtilStepDefs {

	/**
	 * <p>
	 * Prints a table with a newline appended.
	 * </p>
	 * <p>
	 * Usage: <code><pre>
	 * print:
	 * | name    | value    |
	 * | a table | to print |
	 * </pre></code>
	 * </p>
	 */
	@Then("^print table:$")
	public void printTable(List<Map<String, String>> value) {
		for (Map<String, String> map : value) {
			System.out.println(map);
		}
	}

	/**
	 * <p>
	 * Printsdoctext with a newline appended.
	 * </p>
	 * <p>
	 * Usage: <code><pre>
	 * print:
	 * """
	 * a bunch of text
	 * to print
	 * """
	 * </pre></code>
	 * </p>
	 */
	@Then("^print:$")
	public void printDocText(String value) {
		System.out.println(value);
	}

	/**
	 * Prints short strings with a newline appended.
	 */
	@Then("^print \"(.+)\"$")
	public void print(String value) {
		System.out.println(value);
	}

	@When("^sleep for (\\d+) (.+)$")
	public void sleep(int value, TimeUnit unit) throws Exception {
		Thread.sleep(TimeUnit.MILLISECONDS.convert(value, unit));
	}
}
