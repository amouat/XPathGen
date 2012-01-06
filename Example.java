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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.adrianmouat.xpathgen.XPathGen;

/**
 * Example usage of XPathGen.
 * 
 * @author Adrian Mouat
 *
 */
public class Example {

    public static void main (String[] args) {
        
        String docStr = "<a>aa<b attr='test'>b<!-- comment -->c<c/></b>d</a>";
        
        Document testDoc = createDocument(docStr);

        System.out.println("Using Test Doc:");
        System.out.println(docStr);

        //Grab text node "aa"
        Node aa = testDoc.getDocumentElement().getFirstChild();
        System.out.println("\nText Node 'aa' has XPath: "
                + XPathGen.getXPath(aa));

        //Element b
        Node b = aa.getNextSibling();
        System.out.println("\nElement 'b' has XPath: " + XPathGen.getXPath(b));

        //Attribute "attr"
        Node attr = b.getAttributes().getNamedItem("attr");
        System.out.println("\nAttribute 'attr' has XPath: "
                + XPathGen.getXPath(attr));

        //Comments are just the same as other nodes
        Node comment = b.getFirstChild().getNextSibling();
        System.out.println("\nComment node has XPath: " 
                + XPathGen.getXPath(comment));

    }
    
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
            throw new IllegalArgumentException(
                    "Failed to configure non-loading of DTDs");
        }
        
        fac.setIgnoringComments(false);
                
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(
                    xml.getBytes("utf-8"));
            ret = fac.newDocumentBuilder().parse(is);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("No utf-8 encoder!");
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(
                    "Error configuring parser: " + e.getMessage());
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Caught IOException: " + e.getMessage());
        } catch (SAXException e) {
            throw new IllegalArgumentException(
                    "Caught SAXexception: " + e.getMessage());
        }
        
        return ret;

    }
}
