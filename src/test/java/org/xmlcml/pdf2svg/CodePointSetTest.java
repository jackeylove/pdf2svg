/**
 * Copyright (C) 2012 pm286 <peter.murray.rust@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmlcml.pdf2svg;

import java.util.List;

import nu.xom.Builder;
import nu.xom.Element;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.euclid.Util;

public class CodePointSetTest {

	private final static Logger LOG = Logger.getLogger(CodePointSet.class);
	
	
	@Test
	public void testCreateFromElementHighCodePoints() throws Exception {
		Element fontFamilyElementSet = new Builder().build(
				Util.getResourceUsingContextClassLoader(
						CodePointSet.UNICODE_POINT_SET_XML, this.getClass())).getRootElement();
		CodePointSet nonStandardSet = CodePointSet.createFromElement(fontFamilyElementSet); 
		Assert.assertNotNull(nonStandardSet);
	}
	
	@Test
	public void testGetCodePointByUnicode() throws Exception {
		Element fontFamilyElementSet = new Builder().build(
				Util.getResourceUsingContextClassLoader(
						CodePointSet.UNICODE_POINT_SET_XML, this.getClass())).getRootElement();
		CodePointSet nonStandardSet = CodePointSet.createFromElement(fontFamilyElementSet); 
		CodePoint codePoint = nonStandardSet.getByUnicodeValue("U+039F");
		Assert.assertNotNull(codePoint);
		Assert.assertEquals("GREEK CAPITAL LETTER OMICRON", codePoint.getUnicodeName());
	}
	
	@Test
	public void testGetCodePointByDecimal() throws Exception {
		Element fontFamilyElementSet = new Builder().build(
				Util.getResourceUsingContextClassLoader(
						CodePointSet.UNICODE_POINT_SET_XML, this.getClass())).getRootElement();
		CodePointSet nonStandardSet = CodePointSet.createFromElement(fontFamilyElementSet); 
		CodePoint codePoint = nonStandardSet.getByDecimal((Integer)927);
		Assert.assertNotNull(codePoint);
		Assert.assertEquals("GREEK CAPITAL LETTER OMICRON", codePoint.getUnicodeName());
	}
	
	@Test
	public void testConvertCharnameToUnicode() throws Exception {
		Element fontFamilyElementSet = new Builder().build(
				Util.getResourceUsingContextClassLoader(
						CodePointSet.UNICODE_POINT_SET_XML, this.getClass())).getRootElement();
		CodePointSet nonStandardSet = CodePointSet.createFromElement(fontFamilyElementSet); 
		CodePoint codePoint = nonStandardSet.getByUnicodeName("GREEK CAPITAL LETTER OMICRON");
		Assert.assertEquals("unicode", "U+039F", codePoint.getUnicodeValue());
	}

	@Test
	public void testConvertIntegerToUnicode() throws Exception {
		Element fontFamilyElementSet = new Builder().build(
				Util.getResourceUsingContextClassLoader(
						CodePointSet.UNICODE_POINT_SET_XML, this.getClass())).getRootElement();
		CodePointSet nonStandardSet = CodePointSet.createFromElement(fontFamilyElementSet); 
		CodePoint codePoint = nonStandardSet.getByDecimal((int)927);
		Assert.assertEquals("unicode", "U+039F", codePoint.getUnicodeValue());
	}
	
	@Test
	public void testInclude() throws Exception {
		CodePointSet codePointSet = CodePointSet.readCodePointSet("org/xmlcml/pdf2svg/codepoints/test/mtsyn.xml");
		Assert.assertNotNull("codePointSet", codePointSet);
		List<CodePoint> codePoints = codePointSet.getCodePoints();
		Assert.assertEquals("codePoints", 97, codePoints.size());
	}

	@Test
	public void testIncludeSearch1() throws Exception {
		CodePointSet codePointSet = CodePointSet.readCodePointSet("org/xmlcml/pdf2svg/codepoints/test/mtsyn.xml");
		CodePoint codePoint = codePointSet.getByDecimal(65);
		Assert.assertNotNull("A", codePoint);
		Assert.assertNotNull("A", codePoint.getUnicodePoint());
		Assert.assertEquals("A", "LATIN CAPITAL LETTER A", codePoint.getUnicodePoint().getUnicodeName());
	}

	@Test
	public void testIncludeSearchNonAnsi() throws Exception {
		CodePointSet codePointSet = CodePointSet.readCodePointSet("org/xmlcml/pdf2svg/codepoints/test/mtsyn.xml");
		CodePoint codePoint = codePointSet.getByDecimal(5); 
		Assert.assertNotNull("SOLIDUS", codePoint);
		Assert.assertNotNull("SOLIDUS", codePoint.getUnicodePoint());
		Assert.assertEquals("SOLIDUS", "SOLIDUS", codePoint.getUnicodePoint().getUnicodeName());
		Assert.assertEquals("SOLIDUS", 47, (int) codePoint.getUnicodePoint().getDecimalValue());
	}

	@Test
	public void testMainResources() throws Exception {
		CodePointSet codePointSet = CodePointSet.readCodePointSet("org/xmlcml/pdf2svg/codepoints/defacto/mtsyn.xml");
		Assert.assertTrue(""+codePointSet.size(), codePointSet.size() > 100);
		CodePoint codePoint = codePointSet.getByDecimal(5); 
		Assert.assertNotNull("SOLIDUS", codePoint);
		Assert.assertNotNull("SOLIDUS", codePoint.getUnicodePoint());
		Assert.assertEquals("SOLIDUS", "SOLIDUS", codePoint.getUnicodePoint().getUnicodeName());
		Assert.assertEquals("SOLIDUS", 47, (int) codePoint.getUnicodePoint().getDecimalValue());
		
		codePoint = codePointSet.getByDecimal(183); 
		Assert.assertNotNull("183", codePoint);
		Assert.assertNotNull("183", codePoint.getUnicodePoint());
		Assert.assertEquals("183", "MIDDLE DOT", codePoint.getUnicodePoint().getUnicodeName());
		Assert.assertEquals("MIDDLE DOT", 183, (int) codePoint.getUnicodePoint().getDecimalValue());
	}


}
