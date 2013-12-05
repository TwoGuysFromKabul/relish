package net.sf.relish.file;

import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sf.relish.rule.ElapsedTime;
import net.sf.relish.RelishUtil;
import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FileStepDefsTest {

	@Rule public final ElapsedTime elapsedTime = new ElapsedTime();

	FileStepDefs steps = new FileStepDefs();

	@After
	public void after() {
		new File("foo.txt").delete();
	}

	@Test
	public void testDirectoryExists_NoTimeout_WantedExists_Exists() {

		steps.direcotryExists("src/main", true, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryExists_NoTimeout_WantedExists_DoesNotExist() {
		steps.direcotryExists("src/foo", true, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryExists_NoTimeout_WantedDoesNotExist_Exists() {
		steps.direcotryExists("src/main", false, 0, null);
	}

	@Test
	public void testDirectoryExists_NoTimeout_WantedDoesNotExist_DoesNotExist() {
		steps.direcotryExists("src/foo", false, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryExists_TimesOut_WantedExists() {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.direcotryExists("src/foo", true, 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryExists_TimesOut_WantedDoesNotExist() {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.direcotryExists("src/main", false, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testDirectoryContainsFiles_NoNameMatch_Success() throws Exception {

		steps.directoryContainsFiles("src", CountQuantifier.AT_MOST, 5000, null);
	}

	@Test
	public void testDirectoryContainsFiles_WithNameMatch_Success() throws Exception {

		steps.directoryContainsFiles("src", CountQuantifier.EXACTLY, 2, "(?:test|main)");
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryContainsFiles_NoName_Fail() throws Exception {

		steps.directoryContainsFiles("src", CountQuantifier.EXACTLY, 1, null);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryContainsFiles_WithName_Fail() throws Exception {

		steps.directoryContainsFiles("src", CountQuantifier.EXACTLY, 1, "crapola");
	}

	@Test
	public void testDirectoryContainsFilesWithinTimeLimit_NoName_Success() throws Exception {

		steps.directoryContainsFilesWithinTimeLimit("src", 2, null, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testDirectoryContainsFilesWithinTimeLimit_WithName_Success() throws Exception {

		steps.directoryContainsFilesWithinTimeLimit("src", 2, "(?:test|main)", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryContainsFilesWithinTimeLimit_WithName_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.directoryContainsFilesWithinTimeLimit("src", 1, "crapola", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testDirectoryContainsFilesWithinTimeLimit_NoName_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.directoryContainsFilesWithinTimeLimit("src", 1000, null, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testFileExists_WantedExists_Exists() throws Exception {

		steps.fileExists("pom.xml", true, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testFileExists_WantedExists_DoesNotExist() throws Exception {

		steps.fileExists("crapola", true, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testFileExists_WantedDoesNotExist_Exists() throws Exception {

		steps.fileExists("pom.xml", false, 0, null);
	}

	@Test
	public void testFileExists_WantedDoesNotExist_DoesNotExist() throws Exception {

		steps.fileExists("crapola", false, 0, null);
	}

	@Test(expected = AssertionError.class)
	public void testFileExists_TimesOut_WantedExists() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.fileExists("crapola", true, 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testFileExists_TimesOut_WantedDoesNotExist() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.fileExists("pom.xml", false, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testFileContainsLines_WithLineRegex_Success() throws Exception {

		steps.fileContainsLines("pom.xml", CountQuantifier.EXACTLY, 1, "\\s*<artifactId>relish</artifactId>\\s*");
	}

	@Test
	public void testFileContainsLines_NoLineRegex_Success() throws Exception {

		steps.fileContainsLines("pom.xml", CountQuantifier.AT_MOST, 10000, null);
	}

	@Test(expected = AssertionError.class)
	public void testFileContainsLines_WithLineRegex_Fail() throws Exception {

		steps.fileContainsLines("pom.xml", CountQuantifier.EXACTLY, 1, "crapola");
	}

	@Test(expected = AssertionError.class)
	public void testFileContainsLines_NoLineRegex_Fail() throws Exception {

		steps.fileContainsLines("pom.xml", CountQuantifier.EXACTLY, 1, null);
	}

	@Test
	public void testFileContainsLinesWithinTimeLimit_WithLineRegex_Success() throws Exception {

		steps.fileContainsLinesWithinTimeLimit("pom.xml", 1, "\\s*<artifactId>relish</artifactId>\\s*", 1, TimeUnit.SECONDS);
	}

	@Test
	public void testFileContainsLinesWithinTimeLimit_NoLineRegex_Success() throws Exception {

		steps.fileContainsLinesWithinTimeLimit("pom.xml", 1, null, 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testFileContainsLinesWithinTimeLimit_WithLineRegex_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.fileContainsLinesWithinTimeLimit("pom.xml", 1, "crapola", 1, TimeUnit.SECONDS);
	}

	@Test(expected = AssertionError.class)
	public void testFileContainsLinesWithinTimeLimit_NoLineRegex_TimesOut() throws Exception {

		elapsedTime.expectMinMillis(500);
		elapsedTime.expectMaxMillis(1500);
		steps.fileContainsLinesWithinTimeLimit("pom.xml", 10000, null, 1, TimeUnit.SECONDS);
	}

	@Test
	public void testFileContainsThatMatches_Success() throws Exception {

		steps.fileContainsThatMatches("pom.xml", DataFormat.TEXT, "(?:.*\\n?)*");
	}

	@Test(expected = AssertionError.class)
	public void testFileContainsThatMatches_Fail() throws Exception {

		steps.fileContainsThatMatches("pom.xml", DataFormat.TEXT, "abc");
	}

	@Test
	public void testFileIsCreatedWith() throws Exception {

		steps.fileIsCreatedWith("foo.txt", DataFormat.TEXT, "abcdef");
		String contents = RelishUtil.getFileContentsAsString(new File("foo.txt"));
		assertEquals("abcdef", contents);
	}

	@Test
	public void testFileIsDeleted_Success() throws Exception {

		File file = new File("foo.txt");
		file.createNewFile();
		assertTrue(file.exists());
		steps.fileIsDeleted("foo.txt");
		assertFalse(file.exists());
	}

	@Test
	public void testFileIsDeleted_FileDoesNotExist() throws Exception {

		steps.fileIsDeleted("foo.txt");
	}

	@Test(expected = AssertionError.class)
	public void testFileIsDeleted_FileCannotBeDeleted() throws Exception {

		steps.fileIsDeleted("src");
	}
}
