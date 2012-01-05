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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test class for XPathGen.
 * 
 * @author Adrian Mouat
 *
 */
public class XPathGenTest {

    /**
     * Helper method to create an XML Document from a string of XML.
     * 
     * Calls fail if any exception is thrown.
     * 
     * @param xml The XML to turn into a document.
     * @return A DOM document representing the string.
     */
    public static Document createDocument(final String xml) {
        
        Document ret = null;
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    
        //Turn off DTD stuff - if DTD support changes reconsider
        fac.setValidating(false);
        fac.setNamespaceAware(true);
        try {
            fac.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", 
                    false);
        } catch (ParserConfigurationException e) {
            //Should never happen, but probably won't matter
            fail("Failed to configure non-loading of DTDs");
        }
        
        fac.setIgnoringComments(false);
                
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(
                    xml.getBytes("utf-8"));
            ret = fac.newDocumentBuilder().parse(is);
        } catch (UnsupportedEncodingException e) {
            fail("No utf-8 encoder!");
        } catch (ParserConfigurationException e) {
            fail("Error configuring parser: " + e.getMessage());
        } catch (IOException e) {
            fail("Caught IOException: " + e.getMessage());
        } catch (SAXException e) {
            fail("Caught SAXexception: " + e.getMessage());
        }
        
        return ret;

    }
    
    /**
     * Usage example.
     */
    @Test
    public final void testUsageExample() {
        Document testDoc = createDocument(
                "<a>aa<b attr='test'>b<!-- comment -->c<c/></b>d</a>");

        //Grab text node "aa"
        Node aa = testDoc.getDocumentElement().getFirstChild(); 
        assertEquals("/node()[1]/node()[1]", XPathGen.getXPath(aa));

        //Element b
        Node b = aa.getNextSibling();
        assertEquals("/node()[1]/node()[2]", XPathGen.getXPath(b));
        
        //Attribute "attr"
        Node attr = b.getAttributes().getNamedItem("attr");
        assertEquals("/node()[1]/node()[2]/@attr", XPathGen.getXPath(attr));
        
        //Comments are just the same as other nodes
        Node comment = b.getFirstChild().getNextSibling();
        assertEquals("/node()[1]/node()[2]/node()[2]",
                XPathGen.getXPath(comment));
    }

    /**
     * Test getting the unique XPath for nodes.
     */
    @Test
    public final void testGetXPath() {
        //Create an XML doc, loop through nodes, confirming that doing a
        //getXPath then a select returns the node
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpath = xPathFac.newXPath();
        
        Document testDoc = createDocument(
                "<a>aa<b attr='test'>b<!-- comment -->c<c/></b>d</a>");
        
        Node b = testDoc.getDocumentElement().getFirstChild().getNextSibling();
        
        //Old test to ensure comment nodes are processed
        assertEquals(b.getFirstChild().getNextSibling().getNodeType(),
                Node.COMMENT_NODE);
        assertEquals(b.getChildNodes().item(1).getNodeType(), 
                Node.COMMENT_NODE); 
        
        testXPathForNode(testDoc.getDocumentElement(), xpath);
    }
    
    /**
     * Helper method for testGetXPath.
     * 
     * Gets the XPath for the node and evaluates it, checking if the same node
     * is returned. 
     * 
     * DocumentType nodes are ignored as they cannot be identified by an XPath
     * 
     * @param n The node to test
     * @param xp XPath expression (reused for efficiency only)
     */
    private void testXPathForNode(final Node n, final XPath xp) {
        
        if (n.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
            String xpath = XPathGen.getXPath(n);
            compareXPathResult(n, xpath, xp);
        }
    }
    
    /**
     * Compares the result of the XPath expression to the expected Node n.
     * 
     * Also tests children.
     *
     * @param n The expected result node
     * @param xpath The expression to evaluate
     * @param xp XPath expression (for efficiency)
     */
    private void compareXPathResult(final Node n, final String xpath, 
            final XPath xp) {

        Document doc;
        if (n.getNodeType() == Node.DOCUMENT_NODE) {
            doc = (Document) n;
        } else {
            doc = n.getOwnerDocument();
        }
        
        try {
            Node ret = (Node) xp.evaluate(
                    xpath, doc, XPathConstants.NODE);
            assertNotNull(ret);

            if (XPathGen.isText(n)) {
                Node textNode = ret;
                String text = "";
                while (XPathGen.isText(textNode)) {
                    text = text + textNode.getNodeValue();
                    textNode = textNode.getNextSibling();
                }
                
                assertTrue(text + " does not contain " + n.getTextContent(), 
                        text.contains(n.getTextContent()));
            } else {
                assertTrue(
                        ret.getNodeName() + ":" + ret.getNodeValue() 
                        + " is not " + n.getNodeName() + ":" + n.getNodeValue(),
                        n.isSameNode((Node) ret));
            }
        } catch (XPathExpressionException e) {
            fail("Caught exception: " + e.getMessage());
        }

        //Test children
        if (!(n.getNodeType() == Node.ATTRIBUTE_NODE)) {
            NodeList list = n.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                testXPathForNode(list.item(i), xp);
            }
        }
    }

    /**
     * Test for the horrible coalesced text nodes issue.
     * 
     */
    @Test
    public final void testGetXPathWithTextNodes() {
        
        Document testDoc = createDocument("<a>b</a>");
        Element docEl = testDoc.getDocumentElement();
        Node b = docEl.getFirstChild();
        Node c = testDoc.createTextNode("c\n");
        docEl.appendChild(c);
        Node d = testDoc.createElement("d");
        docEl.appendChild(d);
        Node e = testDoc.createTextNode("e");
        docEl.appendChild(e);
        String bxpath = XPathGen.getXPath(b);
        String cxpath = XPathGen.getXPath(c);
        String dxpath = XPathGen.getXPath(d);
        String expath = XPathGen.getXPath(e);

        //Have to normalize the doc for the XPath context to be correct.
        testDoc.normalize();
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xp = xPathFac.newXPath();
 
        compareXPathResult(b, bxpath, xp);       
        compareXPathResult(c, cxpath, xp);       
        compareXPathResult(d, dxpath, xp);       
        compareXPathResult(e, expath, xp);       
    }
    
    /**
     * Test getting XPath for attributes.
     */
    @Test
    public final void testGetXPathForAttributes() {
        
        Document testDoc = createDocument(
                "<a><b attr=\"test\"/></a>");
        Element docEl = testDoc.getDocumentElement();
        NamedNodeMap attrs = docEl.getFirstChild().getAttributes();
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(attrs.item(0), xpathExpr);
    }   
 
    /**
     * Test getting XPath with namespaced element.
     */
    @Test
    public final void testGetXPathWithNamespace() {
        
        Document testDoc = createDocument(
                "<d:a xmlns:d=\"http://test.com\"><b/></d:a>");
        Element docEl = testDoc.getDocumentElement();
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(docEl, xpathExpr);
        testXPathForNode(docEl.getFirstChild(), xpathExpr);
    }   

    /**
     * Test check for blank text nodes.
     */
    @Test
    public final void testCheckForBlankText() {
        Document testDoc = createDocument("<a></a>");

        Node nonBlank = testDoc.createTextNode("a");
        assertFalse(XPathGen.nodeIsEmptyText(nonBlank));

        Node blank = testDoc.createTextNode("");
        assertTrue(XPathGen.nodeIsEmptyText(blank));
    }
    
    /**
     * Test getxPath with DTD thing in prolog.
     */
    @Test
    public final void testGetXPathWithDTDProlog() {
        
        Document testDoc = createDocument(
                "<!DOCTYPE a [ <!ELEMENT a (#PCDATA)>]><a>text</a>");
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(testDoc, xpathExpr);
    }
    
    /**
     * Test getXPath with comment in prolog.
     */
    @Test
    public final void testGetXPathWithCommentProlog() {
        
        Document testDoc = createDocument("<!-- comment --><a>text</a>");
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(testDoc, xpathExpr);
 
    }
    
    /**
     * Test handling of newlines in text nodes.
     */
    @Test
    public final void testNewlineIsNotEmpty() {
        Document testDoc = createDocument("<a>text</a>");
        
        Node text1 = testDoc.createTextNode("\r");
        Node text2 = testDoc.createTextNode("\r\n");
        Node text3 = testDoc.createTextNode("\n");
        
        assertFalse(XPathGen.nodeIsEmptyText(text1));
        assertEquals(1, text1.getNodeValue().length());
        assertFalse(XPathGen.nodeIsEmptyText(text2));
        assertEquals(2, text2.getNodeValue().length());
        assertFalse(XPathGen.nodeIsEmptyText(text3));
        assertEquals(1, text3.getNodeValue().length());
    }
    
    /**
     * Test getting XPath with spaced text nodes.
     */
    @Test
    public final void testGetXPathWithSpacedText() {
        
        Document testDoc = createDocument(
                "<a>x<b>4</b>y</a>");
        Element docEl = testDoc.getDocumentElement();
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(docEl, xpathExpr);
        testXPathForNode(docEl.getFirstChild(), xpathExpr);
    }   
 
    /**
     * Regression test for old bug.
     */
    @Test
    public final void testBug() {
        
        Document testDoc = createDocument(
                "<p><br/>yyy</p>");
        Element docEl = testDoc.getDocumentElement();
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(docEl, xpathExpr);
        testXPathForNode(docEl.getFirstChild(), xpathExpr);
    }
    
    /**
     * Test getting XPath with CDATA and text.
     */
    @Test
    public final void testCDATAandText() {
        
        Document testDoc = createDocument(
                "<p>xxx<![CDATA[yyy]]>zzz</p>");
        Element docEl = testDoc.getDocumentElement();
        
        //Move to beforeclass method
        XPathFactory xPathFac = XPathFactory.newInstance();
        XPath xpathExpr = xPathFac.newXPath();
 
        testXPathForNode(docEl, xpathExpr);
        testXPathForNode(docEl.getFirstChild(), xpathExpr);
    }
}
