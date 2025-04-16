package com.donnnno.arcticons.helper;

import static java.lang.System.getProperty;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;

public class SortAppfilter {
    public static void main(String[] args) throws Exception {
        //String
        String rootDir = getProperty("user.dir");
        // Get the path of the root directory
        Path rootPath = Paths.get(rootDir);
        // Get the name of the root directory
        String rootDirName = rootPath.getFileName().toString();
        if (rootDirName.equals("preparehelper")) {
            rootDir = "..";
        }
        String appFilter = rootDir + "/newicons/appfilter.xml";
        sortXML(appFilter);
    }

    public static void sortXML(String path) throws Exception {
        // Parse the XML file
        File xmlFile = new File(path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        // Normalize the document
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getDocumentElement().getChildNodes();

        // Store elements and comments
        List<ElementGroup> elements = new ArrayList<>();
        String commentStr = null;
        List<Node> items = new ArrayList<>();

        // Iterate through nodes and group elements with their preceding comments
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.COMMENT_NODE) {
                if (commentStr != null) {
                    elements.add(new ElementGroup(commentStr, items));
                    items = new ArrayList<>();
                }
                commentStr = node.getTextContent();
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                items.add(node);
            }
        }

        // Add the last set of elements
        if (commentStr != null) {
            elements.add(new ElementGroup(commentStr, items));
        }

        // Sort the elements by the comment value (case-insensitive)
        elements.sort(Comparator.comparing(element -> element.comment.toLowerCase()));

        // Rebuild the document with sorted elements and comments
        Element root = doc.getDocumentElement();
        root.setTextContent("");  // Clear current content

        for (ElementGroup elementGroup : elements) {
            // Add the comment back
            Comment comment = doc.createComment(elementGroup.comment);
            root.appendChild(comment);

            // Add the elements
            for (Node item : elementGroup.items) {
                root.appendChild(item);
            }
        }
        // Convert Document to string with formatting
        String xmlString = convertDocumentToString(doc);

        // Add newlines and tabs as required
        xmlString = addNewlineBeforeOccurrences(xmlString, "(<!--|</res)");
        xmlString = addTab(xmlString, "(<!|<i|<c)");

        // Write the sorted XML back to the file
        writeXMLToFile(xmlString, path);
    }

    // Helper method to convert Document to string
    public static String convertDocumentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }


    // Helper method to write XML string to a file
    public static void writeXMLToFile(String xmlString, String path) throws Exception {
        xmlString = xmlString.replace(" standalone=\"no\"", "");  // Remove standalone="no"
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(xmlString);
        }
    }

    // Utility class to store comment and its associated elements
    static class ElementGroup {
        String comment;
        List<Node> items;

        ElementGroup(String comment, List<Node> items) {
            this.comment = comment;
            this.items = items;
        }
    }

    // Utility method to add new lines before occurrences of a pattern
    public static String addNewlineBeforeOccurrences(String input, String pattern) {
        return input.replaceAll(pattern, "\r\n$0");
    }

    // Utility method to add tabs before occurrences of a pattern
    public static String addTab(String input, String pattern) {
        return input.replaceAll(pattern, "\t$0");
    }
}
