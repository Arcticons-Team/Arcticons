package com.donnnno.arcticons.helper;

import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.lang.System.out;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


public class Checks {

    private static MatchResult matchResult;

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
        String valuesDir = rootDir + "/app/src/main/res/values";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String changelogXml = valuesDir + "/changelog.xml";
        String generatedDir = rootDir + "/generated";
        String sourceDir = rootDir + "/icons/white";
        String newIconsDir = rootDir + "/newicons";

        int check = 0;
        check = check + (checkXml(appFilter) ? 1 : 0);
        check = check + (missingDrawable(appFilter, sourceDir, newIconsDir) ? 1 : 0);
        check = check + (duplicateEntry(appFilter) ? 1 : 0);
        check = check + (checkSVG(sourceDir) ? 1 : 0);
        // Check if check is not 0, then exit
        if (check != 0) {
            System.out.printf("Exiting program because %d checks failed.%n", check);
            exit(0);  // Exit the program with status 0 (normal termination)
        }
    }

    public static void startChecks(String appFilter, String sourceDir, String newIconsDir) {
        int check = 0;
        check = check + (checkXml(appFilter) ? 1 : 0);
        check = check + (missingDrawable(appFilter, sourceDir, newIconsDir) ? 1 : 0);
        check = check + (duplicateEntry(appFilter) ? 1 : 0);
        check = check + (checkSVG(newIconsDir) ? 1 : 0);
        // Check if check is not 0, then exit
        if (check != 0) {
            System.out.printf("Exiting program because %d checks failed.%n", check);
            exit(0);  // Exit the program with status 0 (normal termination)
        }
    }

    public static boolean checkXml(String path) {
        List<String> defect = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            Pattern pattern = Pattern.compile("((<!--.*-->)|(<(item|calendar) component=\"(ComponentInfo\\{.*/.*}|:[A-Z_]*)\" (drawable|prefix)=\".*\"\\s?/>)|(^\\s*$)|(</?resources>)|(<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>))");
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
            out.println("Error reading file: " + e.getMessage());
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
                out.println("\n\n______ Found duplicate appfilter entries ______\n\n");
                for (String duplicate : duplicates) {
                    out.println("\t" + duplicate);
                }
                out.println("\n\n____ Please check these first before proceeding ____\n\n");
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
                out.println("\n\n______ Found non existent drawables ______\n");
                out.println("Possible causes are typos or completely different naming of the icon\n\n");
                for (Element item : missingDrawables) {
                    // Convert the element to a string and print it
                    String itemString = convertElementToString(item);
                    out.println(itemString);
                }
                out.println("\n\n____ Please check these first before proceeding ____\n\n");
                return true;
            }

        } catch (Exception e) {
            out.println("Error occurred: " + e.getMessage());
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
            out.println("Error occurred: " + e.getMessage());
            return "";
        }
    }

    public static boolean checkSVG(String dir) {
        Map<String, List<String>> strokeAttr = new HashMap<>();

        try {
            // Get all SVG files in the specified directory
            File folder = new File(dir);
            File[] files = folder.listFiles((dir1, name) -> name.endsWith(".svg"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String name = fileName.substring(0, fileName.length() - 4); // Remove .svg extension
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    Pattern pattern = Pattern.compile("(?<strokestr>stroke-width(?:=\"|: ?))(?<number>\\d*(?:.\\d+)?)(?=[p\"; }/])");
                    Matcher matcher = pattern.matcher(content);

                    // StringBuffer to accumulate the modified content
                    StringBuilder result = new StringBuilder();

                    while (matcher.find()) {
                        // Get the stroke width value from the match
                        String matchedStrokeWidth = matcher.group("number");
                        // Process the match through replaceStroke
                        String replacement = replaceStroke(matcher);
                        // Append the replacement to the result
                        matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                    }

                    // Append the remaining part of the string
                    matcher.appendTail(result);

                    // Convert result to string
                    content = result.toString();
                    // Perform regex checks on the SVG content
                    checkAttributes(fileName, content, strokeAttr);
                    // Write the updated content back to the file
                    FileUtils.write(file, content, "UTF-8");

                }
            }

            // Print any findings
            if (!strokeAttr.isEmpty()) {
                out.println("\n\n______ Found SVG with wrong line attributes ______\n");
                for (String svg : strokeAttr.keySet()) {
                    out.println("\n" + svg + ":");
                    for (String attr : strokeAttr.get(svg)) {
                        out.println("\t" + attr);
                    }
                }
                out.println("\n\n____ Please check these first before proceeding ____\n\n");
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void checkAttributes(String file, String content, Map<String, List<String>> strokeAttr) {
        // Regex patterns for various attributes
        Pattern strokeColorPattern = Pattern.compile("stroke(?:=\"|:)(?:rgb[^a]|#).*?(?=[\"; ])");
        Pattern fillColorPattern = Pattern.compile("fill(?:=\"|:)(?:rgb[^a]|#).*?(?=[\"; ])");
        Pattern strokeOpacityPattern = Pattern.compile("stroke-opacity(?:=\"|:).*?(?=[\"; ])");
        Pattern fillOpacityPattern = Pattern.compile("fill-opacity(?:=\"|:).*?(?=[\"; ])");
        Pattern strokeRGBAPattern = Pattern.compile("stroke(?:=\"|:)rgba.*?(?=[\"; ])");
        Pattern fillRGBAPattern = Pattern.compile("fill(?:=\"|:)rgba.*?(?=[\"; ])");
        Pattern strokeWidthPattern = Pattern.compile("stroke-width(?:=\"|:) ?.*?(?=[\"; ])");
        Pattern lineCapPattern = Pattern.compile("stroke-linecap(?:=\"|:).*?(?=[\";}])");
        Pattern lineJoinPattern = Pattern.compile("stroke-linejoin(?:=\"|:).*?(?=[\";}])");

        // Find matching attributes
        Matcher strokeColorMatcher = strokeColorPattern.matcher(content);
        Matcher fillColorMatcher = fillColorPattern.matcher(content);
        Matcher strokeOpacityMatcher = strokeOpacityPattern.matcher(content);
        Matcher fillOpacityMatcher = fillOpacityPattern.matcher(content);
        Matcher strokeRGBAMatcher = strokeRGBAPattern.matcher(content);
        Matcher fillRGBAMatcher = fillRGBAPattern.matcher(content);
        Matcher strokeWidthMatcher = strokeWidthPattern.matcher(content);
        Matcher lineCapMatcher = lineCapPattern.matcher(content);
        Matcher lineJoinMatcher = lineJoinPattern.matcher(content);

        List<String> validColors = Arrays.asList(
                "stroke:#ffffff", "stroke:#fff", "stroke:#FFFFFF",
                "stroke=\"#ffffff", "stroke=\"#fff", "stroke=\"#FFFFFF",
                "stroke=\"white",
                "fill:#ffffff", "fill:#fff", "fill:#FFFFFF",
                "fill=\"#ffffff", "fill=\"#fff", "fill=\"#FFFFFF"
        );
        List<String> validOpacities = Arrays.asList(
                "stroke-opacity=\"0", "stroke-opacity=\"0%", "stroke-opacity=\"1",
                "stroke-opacity=\"100%", "stroke-opacity:1", "stroke-opacity:0",
                "fill-opacity=\"0", "fill-opacity=\"0%", "fill-opacity=\"1",
                "fill-opacity=\"100%", "fill-opacity:1", "fill-opacity:0"
        );
        List<String> validStrokeWidth = Arrays.asList(
                "stroke-width:1","stroke-width:1px","stroke-width:0px",
                "stroke-width:0","stroke-width=\"1","stroke-width=\"0",
                "stroke-width: 0px", "stroke-width: 1px"
        );
        List<String> validLineJoinCap = Arrays.asList(
                "stroke-linejoin:round","stroke-linejoin=\"round","stroke-linejoin: round",
                "stroke-linecap:round","stroke-linecap=\"round","stroke-linecap: round"
        );

        // Check for stroke and fill colors
        checkAttributes(file, strokeColorMatcher, strokeAttr,validColors);
        checkAttributes(file, fillColorMatcher, strokeAttr,validColors);
        checkAttributes(file, strokeOpacityMatcher, strokeAttr,validOpacities);
        checkAttributes(file, fillOpacityMatcher, strokeAttr,validOpacities);

        checkAttributes(file, strokeWidthMatcher, strokeAttr,validStrokeWidth);
        checkAttributes(file, lineCapMatcher, strokeAttr,validLineJoinCap);
        checkAttributes(file, lineJoinMatcher, strokeAttr,validLineJoinCap);

        checkRGBAAttributes(file, strokeRGBAMatcher, strokeAttr);
        checkRGBAAttributes(file, fillRGBAMatcher, strokeAttr);

    }

    // Helper method to check color attributes
    private static void checkAttributes(String file, Matcher matcher, Map<String, List<String>> strokeAttr,List <String> validAttributes) {
        while (matcher.find()) {
            String attr = matcher.group();
            if (!validAttributes.contains(attr.trim())) {
                addToStrokeAttr(file, attr, strokeAttr);
            }
        }
    }

    // Helper method to check rgba attributes
    private static void checkRGBAAttributes(String file, Matcher matcher, Map<String, List<String>> strokeAttr) {
        while (matcher.find()) {
            String rgba = matcher.group();
            if (!isValidRGBA(rgba)) {
                addToStrokeAttr(file, rgba, strokeAttr);
            }
        }
    }

    // Check if the RGBA is valid (specific checks for rgba(255,255,255) or opacity of 0 or 1)
    private static boolean isValidRGBA(String rgba) {
        return !(rgba.contains("rgba(255,255,255") || rgba.endsWith(",0)") || rgba.endsWith(",1)"));
    }

    // Check if the attribute is valid (stroke-width, linecap, etc.)
    private static boolean isValidAttribute(String attribute, String attributeName) {
        if ("stroke-width".equals(attributeName)) {
            return !attribute.equals("stroke-width:1") && !attribute.equals("stroke-width=0");
        } else if ("stroke-linecap".equals(attributeName) || "stroke-linejoin".equals(attributeName)) {
            return attribute.equals("stroke-linecap: round") || attribute.equals("stroke-linejoin: round") ||attribute.equals("stroke-linecap:round") || attribute.equals("stroke-linejoin:round");
        }
        return true;
    }

    // Add attribute to strokeAttr map
    private static void addToStrokeAttr(String file, String attribute, Map<String, List<String>> strokeAttr) {
        strokeAttr.computeIfAbsent(file, k -> new ArrayList<>()).add(attribute);
    }

    // Helper method to replace stroke-width values
    private static String replaceStroke(Matcher matcher) {
        String strokeStr = matcher.group("strokestr");
        double strokeWidth = Double.parseDouble(matcher.group("number"));


        if (strokeWidth > 0.9 && strokeWidth < 1.2) {
            return strokeStr + "1";  // Append '1' to the stroke width
        } else if (strokeWidth >= 0 && strokeWidth < 0.3) {
            return strokeStr +"0";  // Append '0' to the stroke width
        } else {
            return strokeStr + matcher.group("number");  // No change to the stroke width
        }
    }

}
