package net.sf.relish.rule;

import static org.junit.Assert.*;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Asserts that a tests elapsed time was greater than or equal to a minimum, less than or equal to a maximum, or between a minimum and maximum, inclusive.
 */
public final class ElapsedTime implements TestRule {

	private volatile long minMillis = 0;
	private volatile long maxMillis = Long.MAX_VALUE;

	/**
	 * @see org.junit.rules.TestRule#apply(org.junit.runners.model.Statement, org.junit.runner.Description)
	 */
	@Override
	public Statement apply(Statement base, Description description) {
		return new ElapsedTimeStatement(base);
	}

	/**
	 * Sets the minimum time the test must take to pass.
	 */
	public void expectMinMillis(long minMillis) {
		this.minMillis = minMillis;
	}

	/**
	 * Sets the maximum time the test must take to pass.
	 */
	public void expectMaxMillis(long maxMillis) {
		this.maxMillis = maxMillis;
	}

	private class ElapsedTimeStatement extends Statement {

		private final Statement next;

		public ElapsedTimeStatement(Statement next) {
			this.next = next;
		}

		/**
		 * @see org.junit.runners.model.Statement#evaluate()
		 */
		@Override
		public void evaluate() throws Throwable {

			long start = System.currentTimeMillis();
			try {
				next.evaluate();
			} finally {
				long elapsed = System.currentTimeMillis() - start;
				assertTrue(String.format("Statement took less than %d milliseconds: %d", minMillis, elapsed), elapsed >= minMillis);
				assertTrue(String.format("Statement took more than %d milliseconds: %d", maxMillis, elapsed), elapsed <= maxMillis);
			}
		}
	}
}
