package net.sf.relish.rule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ElapsedTimeTest {

	@Rule public final ElapsedTime elapsedTime = new ElapsedTime();
	@Rule public final ExpectedException expectedException = ExpectedException.none();

	@Before
	public void before() {
		expectedException.handleAssertionErrors();
	}

	@Test
	public void testLessThanMinMillis() {

		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("Statement took less than 10000 milliseconds:");

		elapsedTime.expectMinMillis(10000);
	}

	@Test
	public void testMoreThanMaxMillis() throws InterruptedException {

		expectedException.expect(AssertionError.class);
		expectedException.expectMessage("Statement took more than 100 milliseconds:");

		elapsedTime.expectMaxMillis(100);
		Thread.sleep(200);
	}

	@Test
	public void testInRange() throws InterruptedException {

		elapsedTime.expectMinMillis(100);
		elapsedTime.expectMaxMillis(1000);
		Thread.sleep(500);
	}
}
