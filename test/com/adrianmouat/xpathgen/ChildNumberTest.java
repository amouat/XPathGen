/*
 * Utility class for generating unique XPaths based on child number.
 * 
 * Copyright 2012 Adrian Mouat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.adrianmouat.xpathgen;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test class for ChildNumber.
 * 
 * @author Adrian Mouat
 *
 */
public class ChildNumberTest {

    /** Test XML document. */
    private Document testDoc;
    
    /** Test XML Element. */
    private Element parent;
    
    /** Factory for docs. */
    private DocumentBuilderFactory mFac;
    
    /** Factory for XPath Expressions. */
    private XPathFactory mXPathFac;
    
    /**
     * Prepares commonly used test elements etc.
     * 
     * @throws Exception
     */
    @Before
    public final void setUp() throws Exception {

        mFac = DocumentBuilderFactory.newInstance();
        mXPathFac = XPathFactory.newInstance();
        testDoc = mFac.newDocumentBuilder().newDocument();
        parent = testDoc.createElement("parent");
        testDoc.appendChild(parent); 
    }
    
    /**
     * Check straightforward case.
     */
    @Test
    public final void testSimpleChildNo() {

        Element a = testDoc.createElement("a");
        Element b = testDoc.createElement("b");
        Element c = testDoc.createElement("c");
        
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        
        
        XPath xpath = mXPathFac.newXPath();
        try {
            String pre = "//parent/node()[";
            Object ret = xpath.evaluate(
                    pre + aChildNo.getXPath() + "]", testDoc, 
                    XPathConstants.NODE);
            assertTrue(a.isSameNode(((Node) ret)));
            
            ret = xpath.evaluate(pre + bChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue(b.isSameNode(((Node) ret)));
            
            ret = xpath.evaluate(pre + cChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue(c.isSameNode(((Node) ret)));
            
        } catch (XPathExpressionException e) {
            fail("Caught XPathExpressionException: " + e.getMessage());
        }
        
    }
    
    /**
     * Test handling of text nodes.
     */
    @Test
    public final void testTextNodeChildNo() {
        
        //<parent><a/>12<!--d-->3</parent>
        Node blank = testDoc.createTextNode("");
        Element a = testDoc.createElement("a");
        Node b = testDoc.createTextNode("1");
        Node c = testDoc.createTextNode("2");
        Node d = testDoc.createComment("d");
        Node e = testDoc.createTextNode("3");
        
        parent.appendChild(blank); //Should be ignored in XPaths
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        parent.appendChild(d);
        parent.appendChild(e);
        
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        ChildNumber dChildNo = new ChildNumber(d);
        ChildNumber eChildNo = new ChildNumber(e);
        
        
        //Force evaluation of xpaths before normalize
        aChildNo.getXPath();
        bChildNo.getXPath();
        cChildNo.getXPath();
        dChildNo.getXPath();
        eChildNo.getXPath();

        testDoc.normalize();

        XPath xpath = mXPathFac.newXPath();
        try {
            String pre = "//parent/node()[";
            Object ret = xpath.evaluate(
                    pre + aChildNo.getXPath() + "]", testDoc, 
                    XPathConstants.NODE);
            assertTrue(a.isSameNode(((Node) ret)));
            
            ret = xpath.evaluate(
                    "substring(" + pre + bChildNo.getXPath()
                    + "]," + bChildNo.getXPathCharPos() + ",1)",
                    testDoc, XPathConstants.STRING);
            assertEquals("1", ret.toString());
            
            ret = xpath.evaluate(
                    "substring(" + pre + cChildNo.getXPath() + "],"
                    + cChildNo.getXPathCharPos() + ",1)", testDoc, 
                    XPathConstants.STRING);
            assertEquals("2", ret.toString());
            
            ret = xpath.evaluate(pre + dChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue("Got: " + ret.toString(), d.isSameNode(((Node) ret)));

            ret = xpath.evaluate(
                    "substring(" + pre + eChildNo.getXPath() + "],"
                    + eChildNo.getXPathCharPos() + ",1)", testDoc, 
                    XPathConstants.STRING);
            assertEquals(e.getTextContent(), ret.toString());

        } catch (XPathExpressionException ex) {
            fail("Caught XPathExpressionException: " + ex.getMessage());
        }

    }

    /**
     * Test two initial text nodes are counted properly.
     */
    @Test
    public final void testTwoInitialTextNodes() {
        
        Node a = testDoc.createTextNode("1234");
        Node b = testDoc.createTextNode("5");
        Element c = testDoc.createElement("a");
        
        parent.appendChild(a);
        parent.appendChild(b);
        parent.appendChild(c);
        
        ChildNumber aChildNo = new ChildNumber(a);
        ChildNumber bChildNo = new ChildNumber(b);
        ChildNumber cChildNo = new ChildNumber(c);
        
        XPath xpath = mXPathFac.newXPath();
        try {
            String pre = "//parent/node()[";
            Object ret = xpath.evaluate(
                    "substring(" + pre + aChildNo.getXPath()
                    + "]," + aChildNo.getXPathCharPos() + ",4)",
                    testDoc, XPathConstants.STRING);
            assertEquals(a.getTextContent(), ret.toString());
            
            ret = xpath.evaluate(
                    "substring(" + pre + bChildNo.getXPath() + "],"
                    + bChildNo.getXPathCharPos() + ",1)", testDoc, 
                    XPathConstants.STRING);
            assertEquals(b.getTextContent(), ret.toString());
            
            ret = xpath.evaluate(pre + cChildNo.getXPath() + "]",
                    testDoc, XPathConstants.NODE);
            assertTrue("Got: " + ret.toString(), c.isSameNode(((Node) ret)));
            
        } catch (XPathExpressionException e) {
            fail("Caught XPathExpressionException: " + e.getMessage());
        }
    }

    /**
     * Check exception thrown if given null.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testNull() {
        
        new ChildNumber(null);
    }
    
    /**
     * Test exception thrown if no parent.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void testChildWithNoParent() {
        
        Node child = testDoc.createElement("noparent");
        new ChildNumber(child);
    }
}
