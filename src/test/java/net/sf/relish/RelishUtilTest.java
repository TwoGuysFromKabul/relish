package net.sf.relish;

import static net.sf.relish.RelishUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class RelishUtilTest {

	private final Closeable closeable = mock(Closeable.class);

	private FileOutputStream stream;

	private final File testRoot = new File("testRoot");

	@Before
	public void before() throws Exception {

		closeQuietly(stream);
		doDelete(testRoot);
	}

	@After
	public void after() throws Exception {

		closeQuietly(stream);
		doDelete(testRoot);
	}

	@Test
	public void testGetFileContentsAsString() throws Exception {

		testRoot.mkdirs();
		final File file = createFile(testRoot, "testFile", "I am the very model of a modern Major General.".getBytes());
		final String fileContents = getFileContentsAsString(file);

		assertEquals("I am the very model of a modern Major General.", fileContents);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFileContentsAsStringNullFile() throws Exception {

		getFileContentsAsString(null);
	}

	@Test(expected = RuntimeException.class)
	public void testGetFileContentsAsStringNotAFile() throws Exception {

		testRoot.mkdirs();
		getFileContentsAsString(testRoot);
	}

	@Test
	public void testGetFileContents() throws Exception {

		testRoot.mkdirs();
		final byte[] contents = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a };
		final File file = createFile(testRoot, "testFile", contents);
		final byte[] fileContents = getFileContents(file);

		assertArrayEquals(contents, fileContents);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFileContentsNullFile() throws Exception {

		getFileContents(null);
	}

	@Test(expected = RuntimeException.class)
	public void testGetFileContentsNotAFile() throws Exception {

		testRoot.mkdirs();
		getFileContents(testRoot);
	}

	@Test
	public void testWriteToFile() throws Exception {
		File file = File.createTempFile("testWriteToFile", "tmp");
		file.deleteOnExit();
		ByteArrayInputStream is = new ByteArrayInputStream("hello".getBytes());
		writeToFile(file, is);
		assertEquals("hello", getFileContentsAsString(file));
	}

	@Test
	public void testWriteToFile_LargeFile() throws Exception {
		File file = File.createTempFile("testWriteToFile", "tmp");
		file.deleteOnExit();
		byte[] bytes = new byte[300];
		new Random().nextBytes(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		writeToFile(file, is);
		assertTrue(Arrays.equals(bytes, getFileContents(file)));
	}

	private void doDelete(File file) {

		if (!file.exists()) {
			return;
		}

		file.setWritable(true);
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				doDelete(f);
			}
		}
		assertTrue("Failed to delete file " + file, file.delete());
	}

	private File createFile(final File parentDirectory, final String fileName, final byte[] fileContents) throws Exception {

		final File file = new File(parentDirectory, fileName);

		assertTrue(file.createNewFile());

		final FileOutputStream stream = new FileOutputStream(file);
		final FileChannel channel = stream.getChannel();
		final ByteBuffer buffer = ByteBuffer.wrap(fileContents);
		channel.write(buffer);
		closeQuietly(channel);
		closeQuietly(stream);

		return file;
	}

	@Test
	public void testCloseQuietly_ThrowsException() throws Exception {

		doThrow(new IOException()).when(closeable).close();
		closeQuietly(closeable);
		verify(closeable).close();
	}

	@Test
	public void testCloseQuietly_Success() throws Exception {

		closeQuietly(closeable);
		verify(closeable).close();
	}

	@Test
	public void testCloseQuietly_NullCloseable() {

		closeQuietly((Closeable) null);
		// nothing to assert or verify, just wanted to make sure it didn't throw a NullPointerException
	}

	@Test
	public void testReadFromInputStream_SmallStream() throws Exception {
		InputStream in = mock(InputStream.class);
		final byte[] smallStream = new byte[] { 0x01, 0x02, 0x03 };
		final AtomicInteger readsToDo = new AtomicInteger(1);
		when(in.read(any(byte[].class))).thenAnswer(new Answer<Integer>() {

			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				if (readsToDo.getAndDecrement() > 0) {
					byte[] buffer = (byte[]) invocation.getArguments()[0];
					System.arraycopy(smallStream, 0, buffer, 0, smallStream.length);

					return smallStream.length;
				}

				return -1;
			}

		});

		assertArrayEquals(smallStream, readFromInputStream(in));
		verify(in, never()).close();
	}

	@Test
	public void testReadFromInputStream_LargeStream() throws Exception {
		InputStream in = mock(InputStream.class);
		final byte[] largeStream = new byte[65535];
		for (int i = 0; i < 65535; i++) {
			largeStream[i] = (byte) (i % 256);
		}
		final AtomicInteger readsToDo = new AtomicInteger(8);
		when(in.read(any(byte[].class))).thenAnswer(new Answer<Integer>() {

			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				if (readsToDo.getAndDecrement() > 0) {
					byte[] buffer = (byte[]) invocation.getArguments()[0];
					int startPos = 7 - readsToDo.get();
					int sizeFactor = startPos * 8192;
					int bytesRead = largeStream.length - sizeFactor >= 8192 ? 8192 : largeStream.length - sizeFactor;
					System.arraycopy(largeStream, sizeFactor, buffer, 0, bytesRead);

					return bytesRead;
				}

				return -1;
			}

		});

		assertArrayEquals(largeStream, readFromInputStream(in));
		verify(in, never()).close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testReadFromInputStream_NullStream() throws Exception {
		readFromInputStream(null);
	}

	@Test(expected = RuntimeException.class)
	public void testReadFromInputStream_InputStreamThrowsExceptionDuringRead() throws Exception {
		InputStream in = mock(InputStream.class);
		when(in.read(any(byte[].class))).thenThrow(new IOException("No es bueno."));

		readFromInputStream(in);
		verify(in, never()).close();
	}

	@Test
	public void testWriteToOutputStream_SmallStream() throws Exception {
		byte[] smallStream = new byte[] { 0x01, 0x02, 0x03 };
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		writeToOutputStream(out, smallStream);
		assertArrayEquals(smallStream, out.toByteArray());
	}

	@Test
	public void testWriteToOutputStream_LargeStream() throws Exception {
		final byte[] largeStream = new byte[65535];
		for (int i = 0; i < 65535; i++) {
			largeStream[i] = (byte) (i % 256);
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		writeToOutputStream(out, largeStream);
		assertArrayEquals(largeStream, out.toByteArray());
	}

	@Test
	public void testWriteToOutputStream_EmptyStream() throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		writeToOutputStream(out, new byte[0]);
		assertArrayEquals(new byte[0], out.toByteArray());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWriteToOutputStream_NullStream() throws Exception {
		writeToOutputStream(null, new byte[0]);
	}

	@Test
	public void testWriteToOutputStream_NullBytes() throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		writeToOutputStream(out, null);
		assertArrayEquals(new byte[0], out.toByteArray());
	}

	@Test(expected = RuntimeException.class)
	public void testWriteToOutputStream_OutputStreamThrowsExceptionDuringWrite() throws Exception {
		OutputStream out = mock(OutputStream.class);
		doThrow(new IOException("No es bueno.")).when(out).write(any(byte[].class));

		writeToOutputStream(out, new byte[] { 1, 2, 3, 4 });
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateNotEmpty_String_ValueNull() throws Exception {

		validateNotEmpty("arg1", (String) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateNotEmpty_String_ValueEmpty() throws Exception {

		validateNotEmpty("arg1", "");
	}

	@Test
	public void testValidateNotEmpty_String_Success() throws Exception {

		assertEquals("123", validateNotEmpty("arg1", "123"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateNotNull_ValueNull() throws Exception {

		validateNotNull("arg1", null);
	}

	@Test
	public void testValidateNotNull_Succees() throws Exception {

		Object o = new Object();
		assertSame(o, validateNotNull("arg1", o));
	}

	@Test
	public void testValidateInRange_int_Success() throws Exception {

		assertEquals(3, (int) validateInRange("arg1", 3, 3, 5));
		assertEquals(4, (int) validateInRange("arg1", 4, 3, 5));
		assertEquals(5, (int) validateInRange("arg1", 5, 3, 5));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateInRange_int_BelowMin() throws Exception {

		validateInRange("arg1", 2, 3, 5);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateInRange_int_AboveMax() throws Exception {

		validateInRange("arg1", 6, 3, 5);
	}

	@Test
	public void testValidateInRange_long_Success() throws Exception {

		assertEquals(3l, (long) validateInRange("arg1", 3l, 3l, 5l));
		assertEquals(4l, (long) validateInRange("arg1", 4l, 3l, 5l));
		assertEquals(5l, (long) validateInRange("arg1", 5l, 3l, 5l));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateInRange_long_BelowMin() throws Exception {

		validateInRange("arg1", 2, 3l, 5l);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateInRange_long_AboveMax() throws Exception {

		validateInRange("arg1", 6, 3l, 5l);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testValidateGreaterThan_int_EqualsMin() throws Exception {

		validateGreaterThan("arg1", 3, 3);
	}

	@Test
	public void testValidateGreaterThan_int_Success() throws Exception {

		assertEquals(4, (int) validateGreaterThan("arg1", 4, 3));
	}

	@Test
	public void testQuickSplit() {

		assertArrayEquals(new String[] {}, quickSplit(null, '-'));
		assertArrayEquals(new String[] {}, quickSplit("", '-'));
		assertArrayEquals(new String[] {}, quickSplit("-", '-'));
		assertArrayEquals(new String[] {}, quickSplit("--", '-'));
		assertArrayEquals(new String[] {}, quickSplit("---", '-'));
		assertArrayEquals(new String[] { "abc" }, quickSplit("abc", '-'));
		assertArrayEquals(new String[] { "abc" }, quickSplit("-abc", '-'));
		assertArrayEquals(new String[] { "abc" }, quickSplit("abc-", '-'));
		assertArrayEquals(new String[] { "abc" }, quickSplit("-abc-", '-'));
		assertArrayEquals(new String[] { "abc", "123" }, quickSplit("abc-123", '-'));
		assertArrayEquals(new String[] { "abc", "123" }, quickSplit("abc--123", '-'));
		assertArrayEquals(new String[] { "abc", "123" }, quickSplit("abc---123", '-'));
		assertArrayEquals(new String[] { "abc", "123" }, quickSplit("--abc--123--", '-'));
		assertArrayEquals(new String[] { "abc", "123" }, quickSplit("---abc---123---", '-'));
	}
}
