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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility class for generating unique XPath for a given DOM node.
 * 
 * Also contains some other helpful methods for determining if a node is a
 * namespace declaration, copying nodes between documents and checking if node
 * is a text node.
 * 
 * Extracted and relicensed from diffxml code: http://diffxml.sourceforge.net/
 *
 */
public final class XPathGen {
    
    /**
     * XML Namespace URI. Probably a better place to get this from.
     */
    public static final String XMLNS = "http://www.w3.org/2000/xmlns/";
    
    /**
     * Disallow instantiation.
     */
    private XPathGen() {
    }
    
    /**
     * Calculates an XPath that uniquely identifies the given node.
     * For text nodes note that the given node may only be part of the returned
     * node due to coalescing issues; use an offset and length to identify it
     * unambiguously.
     * 
     * @param n The node to calculate the XPath for.
     * @return The XPath to the node as a String
     */
    public static String getXPath(final Node n) {
 
        String xpath;
        
        if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
            //Slightly special case for attributes as they are considered to
            //have no parent
            ((Attr) n).getOwnerElement();
            xpath = getXPath(((Attr) n).getOwnerElement())
                 + "/@" + n.getNodeName();
            
        } else if (n.getNodeType() == Node.DOCUMENT_NODE) {
            
            xpath = "/";
        } else if (n.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            
            throw new IllegalArgumentException(
                    "DocumentType nodes cannot be identified with XPath");
            
        } else if (n.getParentNode().getNodeType() == Node.DOCUMENT_NODE) {
            
            ChildNumber cn = new ChildNumber(n);
            xpath = "/node()[" + cn.getXPath() + "]"; 
            
        } else {

            ChildNumber cn = new ChildNumber(n);

            xpath = getXPath(n.getParentNode()) 
                + "/node()[" + cn.getXPath() + "]";
        }
        
        return xpath;
    }
    
    /**
     * Check if node is an empty text node.
     * 
     * @param n The Node to test.
     * @return True if it is a 0 sized text node
     */
    public static boolean nodeIsEmptyText(final Node n) {
        return (n.getNodeType() == Node.TEXT_NODE 
            && n.getNodeValue().length() == 0);
    }

    /**
     * Copies a node from one Document to another, including attributes but
     * no children.
     * 
     * Required as importNode does not handle namespaces well for element nodes
     * 
     * @param mDoc1 Document that x is to be copied to
     * @param x The node to copy
     * @return A copy of the node in mDoc1
     */
    public static Node copyNodeToDoc(Document doc, Node x) {
        
        Node copy;
        if (x.getNodeType() == Node.ELEMENT_NODE) {
            Element copyEl = doc.createElementNS(
                    x.getNamespaceURI(), x.getNodeName());
            NamedNodeMap attrs = ((Element) x).getAttributes();
            
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr a = (Attr) attrs.item(i);
                copyEl.setAttributeNS(
                        a.getNamespaceURI(), a.getNodeName(), a.getNodeValue());    
            }
            
            copy = copyEl;
            
        } else {
            copy = doc.importNode(x, false);
        }
        
        return copy;
    }

    /**
     * Gets the local name of the node if not null, else just the node name.
     * 
     * Avoids issues with getLocalName returning null.
     * 
     * @param n Node to get the name of
     * @return The local name of the node
     */
    public static String getLocalName(Node a) {
        String ret = a.getLocalName();
        if (ret == null) {
            ret = a.getNodeName();
        }
        
        return ret;
    }

    /**
     * Returns true if the attribute is namespace declaration.
     * 
     * Takes a node to avoid casts.
     * 
     * @param n Attribute to check if namespace declaration
     * @return True if namespace declaration
     */
    public static boolean isNamespaceAttr(Node n) {
        
        boolean ret = false;
        
        if (n.getNamespaceURI() != null) {
            if (n.getNamespaceURI().equals(XPathGen.XMLNS)) {
                ret = true;
            } else if (n.getLocalName().equals("xmlns")) {
                ret = true;
            }
        } else if (n.getNodeName().equals("xmlns")) {
            ret = true;
        }
        
        return ret;
    }
    
    /**
     * Tests if the given node is a text or CDATA node.
     * 
     * @param node The node to test
     * @return True if the type is TEXT_NODE or CDATA_SECTION_NODE 
     */
    public static boolean isText(final Node node) {
     
        boolean ret = false;
        if (node != null 
                && (node.getNodeType() == Node.TEXT_NODE
                || node.getNodeType() == Node.CDATA_SECTION_NODE)) {
            ret = true;
        }
        return ret;
    }
}
