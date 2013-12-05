package net.sf.relish.file;

import static net.sf.relish.RelishUtil.*;
import static net.sf.relish.matcher.RelishMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import net.sf.relish.CountQuantifier;
import net.sf.relish.DataFormat;
import net.sf.relish.RelishUtil;
import net.sf.relish.transformer.CountQuantifierTransformer;
import net.sf.relish.transformer.IsNullTransformer;
import net.sf.relish.transformer.NullSafeIntegerTransformer;
import cucumber.api.Transform;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Cucumber step defs for file operations like matching content, existence, etc. All text files are assumed to be UTF-8 encoded.
 */
public final class FileStepDefs {

	/**
	 * Validates that a directory does or does not exist within a specified time
	 * 
	 * @param directory
	 *            The directory to validate
	 * @param exists
	 *            If true then then the validation succeeds if the directory exists, otherwise if the directory does not exist
	 * @param timeout
	 *            Max time to wait for the condition to be true
	 * @param timeUnit
	 *            The unit of measure for timeout
	 */
	@Then("^directory \"(.+)\"(?: (does not))? exists?(?: within ([\\d]+) (seconds|milliseconds))?$")
	public void direcotryExists(String directory, @Transform(IsNullTransformer.class) boolean exists, @Transform(NullSafeIntegerTransformer.class) int timeout,
			TimeUnit timeUnit) {

		final File dir = new File(directory);

		Callable<Boolean> callable = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				return dir.isDirectory();
			}
		};

		String msgFormat = exists ? "Directory %s did not exist within the specified time" : "Directory %s did not get removed within the specified time";
		assertThatWithin(timeout, timeUnit, callable, equalTo(exists), msgFormat, directory);
	}

	/**
	 * Validates that the specified directory contains the specified number of files with names that match the regex
	 * 
	 * @param directory
	 *            The directory to validate
	 * @param countQuantifier
	 *            How to interpret the file count
	 * @param fileCount
	 *            The number of files that must match
	 * @param nameRegex
	 *            The regular expression the file names must match. Null to match any file name.
	 */
	@Then("^directory \"(.+)\" contains (at least|at most|exactly) ([\\d]+) files?(?: with (?:names that match|a name that matches) \"(.+)\")?$")
	public void directoryContainsFiles(String directory, @Transform(CountQuantifierTransformer.class) CountQuantifier countQuantifier, int fileCount,
			String nameRegex) {

		Pattern pattern = nameRegex == null ? null : Pattern.compile(nameRegex);
		File dir = new File(directory);

		int count = countMatchingFiles(dir, pattern);
		assertThat(count, countQuantifier.newMatcher(fileCount), "Directory '%s' matching file count does not match", directory);
	}

	/**
	 * Validates that the specified directory contains at least the specified number of files with names that match the regex within the specified timeout
	 * 
	 * @param directory
	 *            The directory to validate
	 * @param fileCount
	 *            The number of files that must match
	 * @param nameRegex
	 *            The regular expression the file names must match. Null to match any file name.
	 * @param timeout
	 *            Maximum time to wait for the matching files
	 * @param timeUnit
	 *            Unit of measure for the timeout
	 */
	@Then("^directory \"(.+)\" contains at least ([\\d]+) files?(?: with (names that match|a name that matches) \"(.+)\")? within ([\\d]+) (seconds|milliseconds)$")
	public void directoryContainsFilesWithinTimeLimit(String directory, int fileCount, String nameRegex, int timeout, TimeUnit timeUnit) {

		final Pattern pattern = nameRegex == null ? null : Pattern.compile(nameRegex);
		final File dir = new File(directory);

		Callable<Integer> callable = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return countMatchingFiles(dir, pattern);
			}
		};

		assertThatWithin(timeout, timeUnit, callable, gte(fileCount), "Directory '%s' matching file count does not match", directory);
	}

	/**
	 * Validate that the specified file exists or not within an optional timeout
	 * 
	 * @param file
	 *            The name of the file to validate
	 * @param exists
	 *            True to validate that the file exists. False to validate that the file does not exist
	 * @param timeout
	 *            Maximum to wait for the validation to succeed
	 * @param timeUnit
	 *            Unit of measure for the timeout
	 */
	@Then("^file \"(.+)\"(?: (does not))? exists?(?: within ([\\d]+) (seconds|milliseconds))?$")
	public void fileExists(String file, @Transform(IsNullTransformer.class) boolean exists, Integer timeout, TimeUnit timeUnit) {

		final File f = new File(file);

		Callable<Boolean> callable = new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {

				return f.isFile();
			}
		};

		String msgFormat = exists ? "File %s did not exist within the specified time" : "File %s did not get removed within the specified time";
		assertThatWithin(timeout, timeUnit, callable, equalTo(exists), msgFormat, file);
	}

	/**
	 * Validates that the specified text file contains the specified number of lines that match the regular expression.
	 * 
	 * @param file
	 *            The file to validate
	 * @param countQuantifier
	 *            How to interpret the lineCount
	 * @param lineCount
	 *            The number of lines that must match
	 * @param lineRegex
	 *            The regular expression a line must match. Null to match any line.
	 */
	@Then("^file \"(.+)\" contains (at least|exactly|at most) ([\\d]+) lines?(?: that match(?:es)? \"(.+)\")?$")
	public void fileContainsLines(String file, @Transform(CountQuantifierTransformer.class) CountQuantifier countQuantifier, int lineCount, String lineRegex)
			throws IOException {

		Pattern pattern = lineRegex == null ? null : Pattern.compile(lineRegex);
		int count = countMatchingLines(file, pattern);

		assertThat(count, countQuantifier.newMatcher(lineCount), "File %s number of matching lines did not match", file);
	}

	/**
	 * Validates that the specified text file contains at least the specified number of lines that match the regular expression within a timeout.
	 * 
	 * @param file
	 *            The file to validate
	 * @param lineCount
	 *            The number of lines that must match
	 * @param lineRegex
	 *            The regular expression a line must match. Null to match any line.
	 * @param timeout
	 *            Maximum time to wait for the validation to succeed
	 * @param timeUnit
	 *            Unit of measure for timeout
	 */
	@Then("^file \"(.+)\" contains at least ([\\d]+) lines?(?: that match(?:es)? \"(.+)\")? within ([\\d]+) (seconds|milliseconds)$")
	public void fileContainsLinesWithinTimeLimit(final String file, int lineCount, String lineRegex, int timeout, TimeUnit timeUnit) {

		final Pattern pattern = lineRegex == null ? null : Pattern.compile(lineRegex);
		Callable<Integer> callable = new Callable<Integer>() {

			@Override
			public Integer call() throws Exception {
				return countMatchingLines(file, pattern);
			}
		};

		assertThatWithin(timeout, timeUnit, callable, gte(lineCount), "File %s number of matching lines did not match in the specified time", file);
	}

	/**
	 * Validates that the specified file has the specified content
	 * 
	 * @param file
	 *            The file to validate
	 * @param format
	 *            The format used to interpret the content of the file
	 * @param contentRegex
	 *            The regular expression that the file content must match
	 */
	@Then("^file \"(.+)\" contains (text|JSON|XML|binary) that matches:$")
	public void fileContainsThatMatches(String file, DataFormat format, String contentRegex) {

		byte[] bytes = RelishUtil.getFileContents(new File(file));

		String fileContent = format.bytesToText(bytes);
		contentRegex = format.normalizeRegex(contentRegex);
		assertThat(fileContent, matches(contentRegex), "File %s content did not match", file);
	}

	/**
	 * Creates a file with the specified content. If the file exists it will be overwritten.
	 * 
	 * @param file
	 *            The file to create
	 * @param format
	 *            The format of the file
	 * @param content
	 *            The content of the file
	 */
	@Given("^file \"(.+)\" is created with this (text|XML|JSON|binary):$")
	public void fileIsCreatedWith(String file, DataFormat format, String content) throws IOException {

		byte[] bytes = format.textToBytes(content);
		RelishUtil.writeToFile(new File(file), new ByteArrayInputStream(bytes));
	}

	/**
	 * Deletes the specified file
	 * 
	 * @param file
	 *            The file to delete
	 */
	@When("^file \"(.+)\" is deleted$")
	public void fileIsDeleted(String file) {

		File f = new File(file);
		if (!f.exists()) {
			return;
		}

		assertThat(f.delete(), equalTo(true), "Failed to delete file '%s'", file);
	}

	private int countMatchingFiles(File dir, Pattern pattern) {

		int count = 0;
		String[] files = dir.list();
		for (String file : files) {
			if (pattern == null || pattern.matcher(file).matches()) {
				count++;
			}
		}

		return count;
	}

	private int countMatchingLines(String file, Pattern pattern) throws IOException {

		int count = 0;
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), DataFormat.UTF8));
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (pattern == null || pattern.matcher(line).matches()) {
					count++;
				}
			}
		} finally {
			RelishUtil.closeQuietly(reader);
		}

		return count;
	}
}
