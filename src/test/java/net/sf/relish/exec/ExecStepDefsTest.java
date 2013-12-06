package net.sf.relish.exec;

import net.sf.relish.RelishException;

import org.junit.Test;

public class ExecStepDefsTest {

	ExecStepDefs steps = new ExecStepDefs();

	@Test(expected = RelishException.class)
	public void testApplicationIsExecutedWithCommandLine_NoSuchApp() {

		steps.applicationIsExecutedWithCommandLine("foo", "notanapp");
	}

	@Test
	public void testApplicationIsExecutedWithCommandLine_Success_ArgContainsSpace() {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc\\ 123");
		steps.applicationExitCodeShouldBe("foo", 0);
		steps.applicationOutputShouldMatchThisText("foo", "abc 123\n");
	}

	@Test
	public void testApplicationIsExecutedWithCommandLine_Success() {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc");
		steps.applicationExitCodeShouldBe("foo", 0);
		steps.applicationOutputShouldMatchThisText("foo", "abc\n");
	}

	@Test(expected = RelishException.class)
	public void testApplicationIsExecutedWithCommandLineWithInputThatIsTheOutputOfTheApplication_InputAppHasNotBeenExecuted() throws Exception {

		steps.applicationIsExecutedWithCommandLineWithInputThatIsTheOutputOfTheApplication("bar", "", "foo");
	}

	@Test
	public void testApplicationIsExecutedWithCommandLineWithInputThatIsTheOutputOfTheApplication_Success() throws Exception {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc");
		steps.applicationIsExecutedWithCommandLineWithInputThatIsTheOutputOfTheApplication("bar", "cat", "foo");
		steps.applicationExitCodeShouldBe("bar", 0);
		steps.applicationOutputShouldMatchThisText("bar", "abc\n");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testApplicationIsExecutedWithCommandLineWithInputThatIsThisText_InputIsNull() throws Exception {

		steps.applicationIsExecutedWithCommandLineWithInputThatIsThisText("foo", "echo", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testApplicationIsExecutedWithCommandLineWithInputThatIsThisText_InputIsEmpty() throws Exception {

		steps.applicationIsExecutedWithCommandLineWithInputThatIsThisText("foo", "echo", "");
	}

	@Test
	public void testApplicationIsExecutedWithCommandLineWithInputThatIsThisText_Success() throws Exception {

		steps.applicationIsExecutedWithCommandLineWithInputThatIsThisText("foo", "cat", "abc\n");
		steps.applicationExitCodeShouldBe("foo", 0);
		steps.applicationOutputShouldMatchThisText("foo", "abc\n");
	}

	@Test
	public void testApplicationExitCodeShouldBe_Matches() throws Exception {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc");
		steps.applicationExitCodeShouldBe("foo", 0);
	}

	@Test(expected = AssertionError.class)
	public void testApplicationExitCodeShouldBe_DoesNotMatch() throws Exception {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc");
		steps.applicationExitCodeShouldBe("foo", 1);
	}

	@Test
	public void testApplicationOutputShouldMatchThisText_Matches() throws Exception {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc");
		steps.applicationOutputShouldMatchThisText("foo", "abc\n");
	}

	@Test(expected = AssertionError.class)
	public void testApplicationOutputShouldMatchThisText_DoesNotMatch() throws Exception {

		steps.applicationIsExecutedWithCommandLine("foo", "echo abc");
		steps.applicationOutputShouldMatchThisText("foo", "def");
	}
}
