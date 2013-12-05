package net.sf.relish;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sf.relish.rule.ElapsedTime;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UtilStepDefsTest {

	@Rule public final ElapsedTime elapsedTime = new ElapsedTime();

	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	PrintStream ps = new PrintStream(baos);

	UtilStepDefs steps = new UtilStepDefs();

	@Before
	public void before() {
		System.setOut(ps);
	}

	@Test
	public void testPrintTable() {

		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map = new HashMap<String, String>();
		map.put("name", "foo");
		map.put("value", "bar");
		list.add(map);
		map = new HashMap<String, String>();
		map.put("name", "abc");
		map.put("value", "123");
		list.add(map);
		steps.printTable(list);
		assertEquals("{name=foo, value=bar}\n{name=abc, value=123}\n", new String(baos.toByteArray()));
	}

	@Test
	public void testPrintDocText() throws Exception {

		steps.printDocText("line1\nline2");
		assertEquals("line1\nline2\n", new String(baos.toByteArray()));
	}

	@Test
	public void testPrint() throws Exception {

		steps.print("some data");
		assertEquals("some data\n", new String(baos.toByteArray()));
	}

	@Test
	public void testSleep() throws Exception {

		elapsedTime.expectMinMillis(900);
		elapsedTime.expectMaxMillis(1500);

		steps.sleep(1, TimeUnit.SECONDS);
	}
}
