package net.sf.relish;

import static net.sf.relish.DataFormat.*;
import static org.junit.Assert.*;

import java.nio.charset.Charset;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataFormatTest {

	@Test
	public void testTextToBytes_JSON_NullText() {

		assertNull(JSON.textToBytes(null));
	}

	@Test
	public void testTextToBytes_JSON_EmptyText() {

		assertNull(JSON.textToBytes(""));
	}

	@Test
	public void testTextToBytes_JSON_SingleLine() {

		assertArrayEquals(" { \"foo\" : 123 } ".getBytes(Charset.forName("UTF8")), JSON.textToBytes(" { \"foo\" : 123 } "));
	}

	@Test
	public void testTextToBytes_JSON_MultiLine() {

		assertArrayEquals(" {\n\"foo\" : 123\n} ".getBytes(Charset.forName("UTF8")), JSON.textToBytes(" {\n\"foo\" : 123\n} "));
	}

	@Test(expected = RelishException.class)
	public void testTextToBytes_JSON_TextNotJSON() {

		JSON.textToBytes("foo");
	}

	@Test
	public void testTextToBytes_XML_NullText() {
		assertNull(XML.textToBytes(null));
	}

	@Test
	public void testTextToBytes_XML_EmptyText() {
		assertNull(XML.textToBytes(""));
	}

	@Test
	public void testTextToBytes_XML_SingleLine() {
		assertArrayEquals(" <home> <foo> bar </foo> </home> ".getBytes(Charset.forName("UTF8")), XML.textToBytes(" <home> <foo> bar </foo> </home> "));
	}

	@Test
	public void testTextToBytes_XML_MultiLine() {
		assertArrayEquals("<home>\n\t<foo> bar\n </foo> </home> ".getBytes(Charset.forName("UTF8")), XML.textToBytes("<home>\n\t<foo> bar\n </foo> </home> "));
	}

	@Ignore
	@Test(expected = IllegalArgumentException.class)
	public void testTextToBytes_XML_TextNotXML() {
		XML.textToBytes("foo");
	}

	@Test
	public void testTextToBytes_TEXT_NullText() {
		assertNull(TEXT.textToBytes(null));
	}

	@Test
	public void testTextToBytes_TEXT_EmptyText() {
		assertNull(TEXT.textToBytes(""));
	}

	@Test
	public void testTextToBytes_TEXT_SingleLine() {
		assertArrayEquals(" foo bar ".getBytes(Charset.forName("UTF8")), TEXT.textToBytes(" foo bar "));
	}

	@Test
	public void testTextToBytes_TEXT_MultiLine() {
		assertArrayEquals(" \n foo\nbar\n ".getBytes(Charset.forName("UTF8")), TEXT.textToBytes(" \n foo\nbar\n "));
	}

	@Test
	public void testTextToBytes_BINARY_NullText() {
		assertNull(BINARY.textToBytes(null));
	}

	@Test
	public void testTextToBytes_BINARY_EmptyText() {
		assertNull(BINARY.textToBytes(""));
	}

	@Test
	public void testTextToBytes_BINARY_SingleLine() {
		assertArrayEquals(new byte[] { 1, 2, 3, 12, 15, 32 }, BINARY.textToBytes(" 01 02 03  0c 0f20   "));
	}

	@Test
	public void testTextToBytes_BINARY_MultiLine() {
		assertArrayEquals(new byte[] { 1, 2, 3, 12, 15, 32 }, BINARY.textToBytes(" 01 \n 02 03 \n\t 0c 0f20 \n "));
	}

	@Test
	public void testBytesToText_JSON_NullBytes() {

		assertNull(JSON.bytesToText(null));
	}

	@Test
	public void testBytesToText_JSON_EmptyBytes() {

		assertNull(JSON.bytesToText(new byte[0]));
	}

	@Test
	public void testBytesToText_JSON_SingleLine() {

		assertEquals("{\"foo\":123}", JSON.bytesToText(" { \"foo\" : 123 } ".getBytes(Charset.forName("UTF8"))));
	}

	@Test
	public void testBytesToText_JSON_MultiLine() {

		assertEquals("{\"foo\":123}", JSON.bytesToText(" {\n\"foo\" : 123\n} ".getBytes(Charset.forName("UTF8"))));
	}

	@Test(expected = RelishException.class)
	public void testBytesToText_JSON_BytesNotJSON() {

		JSON.bytesToText("foo".getBytes(Charset.forName("UTF8")));
	}

	@Test
	public void testBytesToText_XML_NullBytes() {
		assertNull(XML.bytesToText(null));
	}

	@Test
	public void testBytesToText_XML_EmptyBytes() {
		assertNull(XML.bytesToText(new byte[0]));
	}

	@Ignore
	@Test
	public void testBytesToText_XML_SingleLine() {
		assertEquals("<home><foo> bar </foo></home>", XML.bytesToText(" <home> <foo> bar </foo> </home> ".getBytes(Charset.forName("UTF8"))));
	}

	@Ignore
	@Test
	public void testBytesToText_XML_MultiLine() {
		assertEquals("<home><foo> bar\n </foo></home>", XML.bytesToText("<home>\n\t<foo> bar\n </foo> </home> ".getBytes(Charset.forName("UTF8"))));
	}

	@Ignore
	@Test(expected = IllegalArgumentException.class)
	public void testBytesToText_XML_BytesNotXML() {
		XML.bytesToText("foo".getBytes(Charset.forName("UTF8")));
	}

	@Test
	public void testBytesToText_TEXT_NullBytes() {
		assertNull(TEXT.bytesToText(null));
	}

	@Test
	public void testBytesToText_TEXT_EmptyBytes() {
		assertNull(TEXT.bytesToText(new byte[0]));
	}

	@Test
	public void testBytesToText_TEXT_SingleLine() {
		assertEquals(" foo bar ", TEXT.bytesToText(" foo bar ".getBytes(Charset.forName("UTF8"))));
	}

	@Test
	public void testBytesToText_TEXT_MultiLine() {
		assertEquals(" \n foo\nbar\n ", TEXT.bytesToText(" \n foo\nbar\n ".getBytes(Charset.forName("UTF8"))));
	}

	@Test
	public void testBytesToText_BINARY_NullBytes() {
		assertNull(BINARY.bytesToText(null));
	}

	@Test
	public void testBytesToText_BINARY_EmptyBytes() {
		assertNull(BINARY.bytesToText(new byte[0]));
	}

	@Test
	public void testBytesToText_BINARY_SingleLine() {
		assertEquals("0102030c0f20", BINARY.bytesToText(new byte[] { 1, 2, 3, 12, 15, 32 }));
	}

	@Test
	public void testNormalizeText_JSON_NullText() {

		assertNull(JSON.normalizeText(null));
	}

	@Test(expected = RelishException.class)
	public void testNormalizeText_JSON_EmptyText() {

		JSON.normalizeText("");
	}

	@Test
	public void testNormalizeText_JSON_SingleLine() {

		assertEquals("{\"foo\":123}", JSON.normalizeText(" { \"foo\" : 123 } "));
	}

	@Test
	public void testNormalizeText_JSON_MultiLine() {

		assertEquals("{\"foo\":123}", JSON.normalizeText(" {\n\"foo\" : 123\n} "));
	}

	@Test(expected = RelishException.class)
	public void testNormalizeText_JSON_TextNotJSON() {

		JSON.normalizeText("foo");
	}

	@Test
	public void testNormalizeText_XML_NullText() {
		assertNull(XML.normalizeText(null));
	}

	@Ignore
	@Test(expected = IllegalArgumentException.class)
	public void testNormalizeText_XML_EmptyText() {
		XML.normalizeText("");
	}

	@Ignore
	@Test
	public void testNormalizeText_XML_SingleLine() {
		assertEquals("<home><foo> bar </foo></home>", XML.normalizeText(" <home> <foo> bar </foo> </home> "));
	}

	@Ignore
	@Test
	public void testNormalizeText_XML_MultiLine() {
		assertEquals("<home><foo> bar\n </foo></home>", XML.normalizeText("<home>\n\t<foo> bar\n </foo> </home> "));
	}

	@Ignore
	@Test(expected = IllegalArgumentException.class)
	public void testNormalizeText_XML_TextNotXML() {
		XML.normalizeText("foo");
	}

	@Test
	public void testNormalizeText_TEXT_NullText() {
		assertNull(TEXT.normalizeText(null));
	}

	@Test
	public void testNormalizeText_TEXT_EmptyText() {
		assertEquals("", TEXT.normalizeText(""));
	}

	@Test
	public void testNormalizeText_TEXT_SingleLine() {
		assertEquals(" foo bar ", TEXT.normalizeText(" foo bar "));
	}

	@Test
	public void testNormalizeText_TEXT_MultiLine() {
		assertEquals(" \n foo\nbar\n ", TEXT.normalizeText(" \n foo\nbar\n "));
	}

	@Test
	public void testNormalizeText_BINARY_NullText() {
		assertNull(BINARY.normalizeText(null));
	}

	@Test
	public void testNormalizeText_BINARY_EmptyText() {
		assertEquals("", BINARY.normalizeText(""));
	}

	@Test
	public void testNormalizeText_BINARY_SingleLine() {
		assertEquals("010203121532", BINARY.normalizeText(" 01 02 03  12 1532   "));
	}

	@Test
	public void testNormalizeText_BINARY_MultiLine() {
		assertEquals("010203121532", BINARY.normalizeText(" 01 \n 02 03\t\n  12 1532 \n  "));
	}

	@Test
	public void testNormalizeRegex_JSON_NullText() {

		assertNull(JSON.normalizeRegex(null));
	}

	@Test
	public void testNormalizeRegex_JSON_EmptyText() {

		assertEquals("", JSON.normalizeRegex(""));
	}

	@Test
	public void testNormalizeRegex_JSON_SingleLine() {

		assertEquals("{ \"foo\" : 123 }", JSON.normalizeRegex(" { \"foo\" : 123 } "));
	}

	@Test
	public void testNormalizeRegex_JSON_MultiLine() {

		assertEquals("{\"foo\" : 123}", JSON.normalizeRegex(" {  \n  \"foo\" : 123 \n} "));
	}

	@Test
	public void testNormalizeRegex_JSON_TextNotJSON() {

		assertEquals("\"foo\" : 123 } }", JSON.normalizeRegex(" \"foo\" : 123 } } "));
	}

	@Test
	public void testNormalizeRegex_XML_NullText() {
		assertNull(XML.normalizeRegex(null));
	}

	@Test
	public void testNormalizeRegex_XML_EmptyText() {
		assertEquals("", XML.normalizeRegex(""));
	}

	@Test
	public void testNormalizeRegex_XML_SingleLine() {
		assertEquals("<home> <foo> bar </foo> </home>", XML.normalizeRegex(" <home> <foo> bar </foo> </home> "));
	}

	@Test
	public void testNormalizeRegex_XML_MultiLine() {
		assertEquals("<home><foo> bar</foo> </home>", XML.normalizeRegex(" <home>\n\t<foo> bar\n </foo> </home> "));
	}

	@Test
	public void testNormalizeRegex_XML_TextNotXML() {
		assertEquals("ome><foo> bar</fo </home>", XML.normalizeRegex("ome>\n\t<foo> bar\n </fo </home>"));
	}

	@Test
	public void testNormalizeRegex_TEXT_NullText() {
		assertNull(TEXT.normalizeRegex(null));
	}

	@Test
	public void testNormalizeRegex_TEXT_EmptyText() {
		assertEquals("", TEXT.normalizeRegex(""));
	}

	@Test
	public void testNormalizeRegex_TEXT_SingleLine() {
		assertEquals(" foo bar ", TEXT.normalizeRegex(" foo bar "));
	}

	@Test
	public void testNormalizeRegex_TEXT_MultiLine() {
		assertEquals(" \n foo\nbar\n ", TEXT.normalizeRegex(" \n foo\nbar\n "));
	}
}
