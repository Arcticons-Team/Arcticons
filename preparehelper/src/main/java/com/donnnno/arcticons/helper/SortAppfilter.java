package com.donnnno.arcticons.helper;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SortAppfilter {

    public static void main(String[] args) {
        Path root = Path.of(System.getProperty("user.dir"));
        if (root.getFileName().toString().equals("preparehelper")) {
            root = root.getParent();
        }

        Path appFilterPath = root.resolve("newicons/appfilter.xml");

        try {
            if (Files.exists(appFilterPath)) {
                sortXML(appFilterPath);
                System.out.println("Sorted appfilter.xml successfully.");
            } else {
                System.err.println("File not found: " + appFilterPath);
            }
        } catch (Exception e) {
            System.err.println("Critical Error: " + e.getMessage());
        }
    }

    public static void sortXML(Path path) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(path.toFile());
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();

        Map<String, List<Node>> sortedGroups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        NodeList nodeList = rootElement.getChildNodes();
        String currentComment = "Uncategorized";
        List<Node> currentItems = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            switch (node) {
                case Comment c -> {
                    if (!currentItems.isEmpty()) {
                        appendToMap(sortedGroups, currentComment, currentItems);
                        currentItems.clear();
                    }
                    currentComment = c.getTextContent().trim();
                }
                case Element e -> currentItems.add(e);
                default -> {}
            }
        }
        if (!currentItems.isEmpty()) {
            appendToMap(sortedGroups, currentComment, currentItems);
        }
        rootElement.setTextContent("");

        sortedGroups.forEach((comment, items) -> {
            rootElement.appendChild(doc.createTextNode("\n\n\t"));
            rootElement.appendChild(doc.createComment(" " + comment + " "));
            for (Node item : items) {
                rootElement.appendChild(doc.createTextNode("\n\t"));
                rootElement.appendChild(item);
            }
        });

        rootElement.appendChild(doc.createTextNode("\n"));
        saveDocument(doc, path);
    }

    private static void appendToMap(Map<String, List<Node>> map, String comment, List<Node> items) {
        map.computeIfAbsent(comment, k -> new ArrayList<>())
                .addAll(List.copyOf(items));
    }

    private static void saveDocument(Document doc, Path path) throws Exception {
        var tf = TransformerFactory.newInstance();
        var transformer = tf.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        var writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        String result = writer.toString()
                .replace(" standalone=\"no\"", "")
                .replace("<resources>", "\n<resources>")
                .replace("</resources>", "\n</resources>")
                .stripTrailing();

        Files.writeString(path, result, StandardCharsets.UTF_8);
    }
}