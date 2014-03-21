package net.sf.relish.consoleapp;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import net.sf.relish.RelishException;
import net.sf.relish.TestMainClass;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsoleAppStepDefsTest {

	@Rule public final ExpectedException expectedException = ExpectedException.none();
	ConsoleAppStepDefs steps = new ConsoleAppStepDefs();
	static Map<Class<?>, String[]> mainArgsByClass;
	static int stopCount;

	static CountDownLatch testOneLatch;
	static CountDownLatch testTwoLatch;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		mainArgsByClass = new HashMap<Class<?>, String[]>();
		testOneLatch = new CountDownLatch(1);
		testTwoLatch = new CountDownLatch(1);
		stopCount = 0;
	}

	@Test
	public void testAfter_NoAppsRunning() throws Exception {

		steps.after();
		assertEquals(0, stopCount);
	}

	@Test
	public void testAfter_AppsRunning() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, null);
		steps.after();
		assertEquals(1, stopCount);

		steps.javaClassIsRunning(TestClass1.class.getName(), null, null);
		steps.javaClassIsRunning(TestClass2.class.getName(), null, null);
		steps.after();
		assertEquals(3, stopCount);
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_AlreadyRunning() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException
				.expectMessage("You may not start console app net.sf.relish.consoleapp.ConsoleAppStepDefsTest$TestClass1 because it is already running");
		steps.javaClassIsRunning(TestClass1.class.getName(), null, null);
		steps.javaClassIsRunning(TestClass1.class.getName(), null, null);
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_FromJar() throws Exception {

		steps.javaClassIsRunning(TestMainClass.class.getName(), "testapp.jar", null);
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_MultipleAppsRunning() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "foo 123");
		steps.javaClassIsRunning(TestClass2.class.getName(), null, "bar 456");
		testOneLatch.await();
		testTwoLatch.await();
		assertEquals(2, mainArgsByClass.size());
		assertArrayEquals(new String[] { "foo", "123" }, mainArgsByClass.get(TestClass1.class));
		assertArrayEquals(new String[] { "bar", "456" }, mainArgsByClass.get(TestClass2.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_NoArgs() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, null);
		testOneLatch.await();
		assertEquals(1, mainArgsByClass.size());
		assertArrayEquals(new String[] {}, mainArgsByClass.get(TestClass1.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_OneArg() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "abc");
		testOneLatch.await();
		assertEquals(1, mainArgsByClass.size());
		assertArrayEquals(new String[] { "abc" }, mainArgsByClass.get(TestClass1.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_MultipleArgs() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "abc 123");
		testOneLatch.await();
		assertEquals(1, mainArgsByClass.size());
		assertArrayEquals(new String[] { "abc", "123" }, mainArgsByClass.get(TestClass1.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_ArgsWithLeadingWhiteSpace() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "  abc 123");
		testOneLatch.await();
		assertEquals(1, mainArgsByClass.size());
		assertArrayEquals(new String[] { "abc", "123" }, mainArgsByClass.get(TestClass1.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_ArgsWithTrailingWhiteSpace() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "abc 123  ");
		testOneLatch.await();
		assertEquals(1, mainArgsByClass.size());
		assertArrayEquals(new String[] { "abc", "123" }, mainArgsByClass.get(TestClass1.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_QuotedArgs() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "\"abc 123\" \"def \n 456\"");
		testOneLatch.await();
		assertEquals(1, mainArgsByClass.size());
		assertArrayEquals(new String[] { "abc 123", "def \n 456" }, mainArgsByClass.get(TestClass1.class));
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_QuoteInMiddleOfArg() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("Unexpected quotes found at position 2 in args: a\"123");

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "a\"123");
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_TextBeforeArgOpeningQuote() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("Unexpected quotes found at position 2 in args: a\"123\"");

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "a\"123\"");
	}

	@Test
	public void testJavaClassIsRunning_FromClasspath_TextAfterArgClosingQuote() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("Unexpected character found at position 5 in args: \"abc\"1");

		steps.javaClassIsRunning(TestClass1.class.getName(), null, "\"abc\"1");
	}

	@Test
	public void testJavaClassIsStopped_FromClasspath_ClassNotRunning() throws Exception {

		expectedException.expect(RelishException.class);
		expectedException.expectMessage("You cannot stop console app net.sf.relish.consoleapp.ConsoleAppStepDefsTest$TestClass1 because it is not running");

		steps.javaClassIsStopped(TestClass1.class.getName());
	}

	@Test
	public void testJavaClassIsStopped_FromJar_ClassRunning() throws Exception {

		steps.javaClassIsRunning(TestMainClass.class.getName(), "testapp.jar", null);
		steps.javaClassIsStopped(TestMainClass.class.getName());
	}

	@Test
	public void testJavaClassIsStopped_FromClasspath_ClassRunning() throws Exception {

		steps.javaClassIsRunning(TestClass1.class.getName(), null, null);
		steps.javaClassIsStopped(TestClass1.class.getName());
		assertEquals(1, stopCount);
	}

	static class TestClass1 {

		private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

		public static void main(String[] args) throws Exception {
			mainArgsByClass.put(TestClass1.class, args);
			testOneLatch.countDown();
			shutdownLatch.await();
		}

		public static void stop() {
			stopCount++;
			shutdownLatch.countDown();
		}
	}

	static class TestClass2 {

		private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

		public static void main(String[] args) throws Exception {
			mainArgsByClass.put(TestClass2.class, args);
			testTwoLatch.countDown();
			shutdownLatch.await();
		}

		public static void stop() {
			stopCount++;
			shutdownLatch.countDown();
		}
	}
}
