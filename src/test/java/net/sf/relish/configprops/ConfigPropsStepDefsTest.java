package net.sf.relish.configprops;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sf.relish.NameValuePair;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigPropsStepDefsTest {

	ConfigPropsStepDefs defs = new ConfigPropsStepDefs();

	@Test
	public void testConfigPropertiesAre() {

		defs.configPropertiesAre(Arrays.asList(new NameValuePair[] { new NameValuePair("n1", "v1"), new NameValuePair("n2", "v2") }));
		assertEquals("v1", System.getProperty("n1"));
		assertEquals("v2", System.getProperty("n2"));
	}

	@Test
	public void testConfigPropertyIs() throws Exception {

		defs.configPropertyis("n1", "v3");
		assertEquals("v3", System.getProperty("n1"));
	}
}
