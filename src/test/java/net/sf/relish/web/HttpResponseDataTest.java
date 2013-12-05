package net.sf.relish.web;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HttpResponseDataTest {

	@Test
	public void assertAllPublicMethodsSynchronized() {

		for (Method method : HttpResponseData.class.getMethods()) {
			if (method.getDeclaringClass() != Object.class) {
				assertTrue("Method is not synchronized: " + method, Modifier.isSynchronized(method.getModifiers()));
			}
		}
	}
}
