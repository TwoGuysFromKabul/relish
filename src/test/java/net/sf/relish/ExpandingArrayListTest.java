package net.sf.relish;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExpandingArrayListTest {

	List<Integer> list = new ExpandingArrayList<Integer>();

	@Test
	public void testSet_NoExpansionNeeded() {

		list.add(1);
		assertEquals(1, list.size());
		assertEquals(1, (int) list.get(0));

		list.set(0, 2);
		assertEquals(1, list.size());
		assertEquals(2, (int) list.get(0));
	}

	@Test
	public void testSet_ExpansionNeeded() {

		list.set(0, 2);
		assertEquals(1, list.size());
		assertEquals(2, (int) list.get(0));

		list.set(3, 5);
		assertEquals(4, list.size());
		assertEquals(2, (int) list.get(0));
		assertNull(list.get(1));
		assertNull(list.get(2));
		assertEquals(5, (int) list.get(3));
	}

	@Test
	public void testAdd_NoExpansionNeeded() {

		list.add(0, 5);
		list.add(1, 9);

		assertEquals(2, list.size());
		assertEquals(5, (int) list.get(0));
		assertEquals(9, (int) list.get(1));
	}

	@Test
	public void testAdd_ExpansionNeeded() {

		list.add(1, 5);
		list.add(4, 9);

		assertEquals(5, list.size());
		assertNull(list.get(0));
		assertEquals(5, (int) list.get(1));
		assertNull(list.get(2));
		assertNull(list.get(3));
		assertEquals(9, (int) list.get(4));
	}

	@Test
	public void testAddAll_NoExpansionNeeded() {

		Collection<Integer> extra = new ArrayList<Integer>();
		extra.add(5);
		extra.add(9);
		list.addAll(0, extra);

		assertEquals(2, list.size());
		assertEquals(5, (int) list.get(0));
		assertEquals(9, (int) list.get(1));
	}

	@Test
	public void testAddAll_ExpansionNeeded() {

		Collection<Integer> extra = new ArrayList<Integer>();
		extra.add(5);
		extra.add(9);
		list.addAll(2, extra);

		assertEquals(4, list.size());
		assertNull(list.get(0));
		assertNull(list.get(1));
		assertEquals(5, (int) list.get(2));
		assertEquals(9, (int) list.get(3));
	}

	@Test
	public void testGet_IndexOutOfBounds() {

		assertNull(list.get(0));
	}
}
