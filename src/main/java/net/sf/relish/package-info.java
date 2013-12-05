/**
 * <p>
 * Relish is a library of Cucumber step definitions. Relish may be run from the command line but if you are reading this presumably you are using the API. To
 * run your cukes (Cucumber feature tests) create the following class:
 * 
 * <pre>
 *  <code>
 *  @RunWith(Cucumber.class)
 *  @CucumberOptions(
 *  	strict = true, 
 *  	monochrome = true,
 *  	format = { "pretty", "html:target/cucumber-html-report" }, 
 *  	glue = { "net.sf.relish", "com.myapp.stepdefs" },
 *  	features = { "classpath:features/" })
 *  public final class RunCukesTest {
 *  }
 *  </code>
 * </pre>
 * 
 * The glue entry <code>"com.myapp.stepdefs"</code> is the base package to search for any application specific Cucumber step definitions you have
 * packaged with your application. If you don't have any then don't include this glue.
 * </p>
 * <p>
 * Put your <code>.feature</code> files in src/test/resources/features and any of its sub-directories.
 * </p>
 * <p>
 * The Cucumber Natural Ecliplse plugin provides syntax highlighing, code completion, and other useful feature for editing <code>.feature</code> files in
 * Eclipse. This is the update site: http://rlogiacco.github.com/Natural.
 * </p>
 * <p>
 * For more information on Cucumber visit the <a href="https://github.com/cucumber/cucumber/wiki/_pages">Cucumber Wiki</a>
 * </p>
 */
package net.sf.relish;

