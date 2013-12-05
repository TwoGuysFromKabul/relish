package net.sf.relish.web;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.relish.NameValuePair;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractHttpRequestResponseDataTest {

	TestData data = new TestData();

	@Test
	public void testCtor_WithBody() throws Exception {

		byte[] body = new byte[] { 1, 2, 3 };
		data = new TestData(body);
		assertArrayEquals(new byte[] { 1, 2, 3 }, data.getBody());
		body[1] = 5;
		assertArrayEquals(new byte[] { 1, 2, 3 }, data.getBody());
	}

	@Test
	public void testGetSetBody() {

		byte[] body1 = new byte[] { 1, 2, 3 };
		assertNull(data.getBody());
		data.setBody(body1);
		assertArrayEquals(new byte[] { 1, 2, 3 }, data.getBody());
		body1[1] = 5;
		byte[] body2 = data.getBody();
		assertArrayEquals(new byte[] { 1, 2, 3 }, body2);
		body2[1] = 6;
		assertArrayEquals(new byte[] { 1, 2, 3 }, data.getBody());
	}

	@Test
	public void testGetSetHeaders() {

		data.setHeader(new NameValuePair("foo", "bar"));
		assertEquals("bar", data.getHeaderValue("foo"));
		assertEquals(new NameValuePair("foo", "bar"), data.getHeader("foo"));

		data.setHeader("abc", "def");
		assertEquals("def", data.getHeaderValue("abc"));
		assertEquals(new NameValuePair("abc", "def"), data.getHeader("abc"));

		List<NameValuePair> headers = data.getHeaders();
		assertCollectionEquals(headers, new NameValuePair("foo", "bar"), new NameValuePair("abc", "def"));
		headers.add(new NameValuePair("ghi", "jkl"));
		assertCollectionEquals(headers, new NameValuePair("foo", "bar"), new NameValuePair("abc", "def"), new NameValuePair("ghi", "jkl"));
		assertCollectionEquals(data.getHeaders(), new NameValuePair("foo", "bar"), new NameValuePair("abc", "def"));
	}

	@Test
	public void assertAllPublicMethodsSynchronized() {

		for (Method method : AbstractHttpRequestResponseData.class.getMethods()) {
			if (method.getDeclaringClass() != Object.class) {
				assertTrue("Method is not synchronized: " + method, Modifier.isSynchronized(method.getModifiers()));
			}
		}
	}

	private void assertCollectionEquals(Collection<NameValuePair> c, NameValuePair... values) {

		assertEquals("Collection has wrong number of entries", values.length, c.size());
		Collection<NameValuePair> copy = new ArrayList<NameValuePair>(c);
		for (NameValuePair value : values) {
			boolean found = false;
			Iterator<NameValuePair> iter = copy.iterator();
			while (iter.hasNext()) {
				NameValuePair copyValue = iter.next();
				if ((value == copyValue) || value != null && value.equals(copyValue)) {
					iter.remove();
					found = true;
					break;
				}
			}
			assertTrue("Value not found in collection: " + value, found);
		}

		assertTrue("Unexpected values in collection: " + copy, copy.isEmpty());
	}

	private final class TestData extends AbstractHttpRequestResponseData {

		public TestData() {
			super();
		}

		public TestData(byte[] body) {
			super(body);
		}
	}
}
