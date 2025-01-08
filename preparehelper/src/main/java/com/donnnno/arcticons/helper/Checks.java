package com.donnnno.arcticons.helper;

import static java.lang.System.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.nio.file.*;


public class Checks {
    public static void main(String[] args) {
        //String
        String rootDir = getProperty("user.dir");
        // Get the path of the root directory
        Path rootPath = Paths.get(rootDir);
        // Get the name of the root directory
        String rootDirName = rootPath.getFileName().toString();
        if (rootDirName.equals("preparehelper")) {
            rootDir = "..";
        }
        String valuesDir = rootDir+"/app/src/main/res/values";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String changelogXml = valuesDir +"/changelog.xml";
        String generatedDir = rootDir +"/generated";
        String sourceDir = rootDir + "/icons/white";
        String newIconsDir = rootDir + "/newicons";
        int check = 0;
        check = check + (checkXml(appFilter) ? 1 : 0);
        check = check + (missingDrawable(appFilter, sourceDir, newIconsDir) ? 1 : 0);
        check = check + (duplicateEntry(appFilter)  ? 1 : 0);
        // Check if check is not 0, then exit
        if (check != 0) {
            System.out.println("Exiting program because check is not 0.");
            System.exit(0);  // Exit the program with status 0 (normal termination)
        }


    }

    public static boolean checkXml(String path) {
        List<String> defect = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"))) {
            String line;
            Pattern pattern = Pattern.compile("((<!--.*-->)|(<(item|calendar) component=\"(ComponentInfo\\{.*/.*}|:[A-Z_]*)\" (drawable|prefix)=\".*\"s?/>)|(^s*$)|(</?resources>))");
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) {
                    defect.add(line);
                }
            }
            if (!defect.isEmpty()) {
                out.println("\n\n______ Found defect appfilter entries ______\n\n");
                for (String defectLine : defect) {
                    out.println(defectLine);
                }
                out.println("\n\n____ Please check these first before proceeding ____\n\n");
                return true;
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return false;
    }

    public static boolean duplicateEntry(String path) {
        List<String> components = new ArrayList<>();

        try {
            // Set up XML parsing
            File inputFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Iterate through all <item> elements
            NodeList nodeList = doc.getElementsByTagName("item");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element item = (Element) node;
                    // Check if 'prefix' attribute exists
                    if (item.getAttribute("prefix").isEmpty()) {
                        String component = item.getAttribute("component");
                        components.add(component); // Add component to the list
                    }
                }
            }

            // Check for duplicates in the components list
            Set<String> duplicates = new HashSet<>();
            Set<String> seen = new HashSet<>();
            for (String component : components) {
                if (!seen.add(component)) { // If the component was already seen, it's a duplicate
                    duplicates.add(component);
                }
            }

            // Print the duplicates if any
            if (!duplicates.isEmpty()) {
                System.out.println("\n\n______ Found duplicate appfilter entries ______\n\n");
                for (String duplicate : duplicates) {
                    System.out.println("\t" + duplicate);
                }
                System.out.println("\n\n____ Please check these first before proceeding ____\n\n");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean missingDrawable(String appfilterPath, String whiteDir, String otherDir) {
        List<Element> missingDrawables = new ArrayList<>();
        try {
            // Set up XML parsing
            File inputFile = new File(appfilterPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            // Iterate through all <item> elements
            NodeList nodeList = doc.getElementsByTagName("item");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element item = (Element) node;
                    // Check if 'prefix' attribute exists
                    if (item.getAttribute("prefix").isEmpty()) {
                        String drawable = item.getAttribute("drawable");
                        // Check if the drawable file exists in the specified directories
                        Path whitePath = Paths.get(whiteDir, drawable + ".svg");
                        Path otherPath = Paths.get(otherDir, drawable + ".svg");
                        if (!Files.exists(whitePath) && !Files.exists(otherPath)) {
                            missingDrawables.add(item); // Add item to the list if missing
                        }
                    }
                }
            }

            // Print missing drawables if any
            if (!missingDrawables.isEmpty()) {
                System.out.println("\n\n______ Found non existent drawables ______\n");
                System.out.println("Possible causes are typos or completely different naming of the icon\n\n");
                for (Element item : missingDrawables) {
                    // Convert the element to a string and print it
                    String itemString = convertElementToString(item);
                    System.out.println(itemString);
                }
                System.out.println("\n\n____ Please check these first before proceeding ____\n\n");
                return true;
            }

        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return false;
    }

    // Helper method to convert Element to String
    private static String convertElementToString(Element element) {
        try {
            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            return "";
        }
    }

}
