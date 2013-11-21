XPathGen
========

Simple Java utility class for generating a unique XPath for a given DOM node.

E.g.
```Java
        Document testDoc = createDocument(
                "<a>aa<b attr='test'>b<!-- comment -->c<c/></b>d</a>");
                
        //Grab text node "aa"
        Node aa = testDoc.getDocumentElement().getFirstChild(); 
        
        System.out.println(XPathGen.getXPath(aa));
        
        //Should print "/node()[1]/node()[1]"
```
This code has been taken from diffxml (http://diffxml.sourceforge.net),
slightly simplified and relicensed under the Apache licence V2. (Note that
diffxml was released under a GPL license which I've relicensed here to allow
for commercial use.)
 
There are also a few extra methods you might find useful:

    - copyNodeToDoc   which copies an element with any attributes but no children
                      from one document to another
    - getLocalName    which just returns the local name of the node, falling back
                      to the node name if this is null
    - isNamespaceAttr which returns true if the node is a namespace declaration
    - isText          which returns true if the node is a CDATA or text node
     
The code has been simplified slightly by removing support for ignoring nodes
and calculating text offsets (in DOM it is possible to have two adjacent text
nodes but these are seen as a single node in XPath, which requires the use of
offsets and length attributes to work around). The ChildNumber class is still
more complicated than it needs to be however.

For more usage details see "Example.java" and the JUnit tests.

XPathGen has been tested with Java SE 6. If you want to run the tests you will
need JUnit 4.

Possible improvements:

  - Using node names rather than numbers
  - Simplify ChildNumber class (subsume into XPathGen?)
  - Add an interface class to hide details
  
Adrian Mouat
