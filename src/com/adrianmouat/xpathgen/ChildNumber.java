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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to hold and calculate XPath child numbers of node.
 * 
 */
public final class ChildNumber {
    

    /** XPath child number. */
    private int mXPathChildNo = -1;

    /** XPath char position. */
    private int mXPathCharPos = -1;
    
    /** The node we are doing the calcs on. */
    private final Node mNode;
    
    /** The siblings of the node and the node itself. */
    private NodeList mSiblings;
    
    
    /**
     * Default constructor.
     * 
     * @param n Node to find the child numbers of
     */
    public ChildNumber(final Node n) {
        
        if (n == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        if (n.getParentNode() == null) {
            throw new IllegalArgumentException("Node must have parent");
        }
        
        mNode = n;
        mSiblings = mNode.getParentNode().getChildNodes();
    }


    /**
     * Get the XPath child number.
     * 
     * @return XPath child number of associated node.
     */
    public int getXPathCharPos() {
        
        if (mXPathCharPos == -1) {
            calculateXPathChildNumberAndPosition();
        }
        return mXPathCharPos;
    }

    
    /**
     * Get the XPath child number.
     * 
     * @return XPath child number of associated node.
     */
    public int getXPath() {
        
        if (mXPathChildNo == -1) {
            calculateXPathChildNumberAndPosition();
        }
        return mXPathChildNo;
    }

    
    /**
     * Determines whether XPath index should be incremented.
     * 
     * Handles differences between DOM index and XPath index
     * 
     * @param i The current position in siblings
     * @return true If index should be incremented
     */
    private boolean incIndex(final int i) {

        boolean inc = true;
        Node curr = mSiblings.item(i);
 
        // Handle non-coalescing of text nodes
        if ((i > 0 && nodesAreTextNodes(curr, mSiblings.item(i - 1))) 
                || XPathGen.nodeIsEmptyText(curr)
                || curr.getNodeType() == Node.DOCUMENT_TYPE_NODE) {
            inc = false;
        }

        return inc;
    }
    
    /**
     * Determines whether the given Nodes are all text nodes or not.
     * 
     * @param nodes The Nodes to checks.
     * @return true if all the given Nodes are text nodes
     */
    private static boolean nodesAreTextNodes(final Node... nodes) {

        boolean areText = true;

        for (Node n : nodes) {            
            if (!XPathGen.isText(n)) {
                areText = false;
                break;
            }

        }
        return areText;
    }

 
    /**
     * Sets the XPath child number and text position.
     */
    private void calculateXPathChildNumberAndPosition() {

        calculateXPathTextPosition(calculateXPathChildNumber());   
    }

    
    /**
     * Calculate the character position of the node.
     * 
     * @param domIndex The DOM index of the node in its siblings.
     */
    private void calculateXPathTextPosition(final int domIndex) {
        
        mXPathCharPos = 1;
        for (int i = (domIndex - 1); i >= 0; i--) {
            if (XPathGen.isText(mSiblings.item(i))) {
                mXPathCharPos = mXPathCharPos 
                    + mSiblings.item(i).getTextContent().length();
            } else {
                break;
            }
        }
    }

    /**
     * Set the XPath child number of the node.
     * 
     * @return The DOM index of the node in its siblings
     */
    private int calculateXPathChildNumber() {
        
        int childNo = 1;

        int domIndex;
        for (domIndex = 0; domIndex < mSiblings.getLength(); domIndex++) {
            
            if (mSiblings.item(domIndex).isSameNode(mNode)) {
                
                if (!incIndex(domIndex)) {
                    childNo--;
                }
                break;
            }
            if (incIndex(domIndex)) {
                childNo++;
            }
        }
        
        mXPathChildNo = childNo;
        return domIndex;
    }


}
