package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Changelog {
    public static void main(String[] args) {
        //String
        String rootDir = System.getProperty("user.dir");
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


       generateChangelogs(generatedDir, valuesDir+"/custom_icon_count.xml", appFilter, changelogXml,rootDir, false);
    }





    public static void generateChangelogs(String generatedDir, String customIconCountXml, String appFilter, String changelogXml,String rootDir, boolean newRelease) {
        String newXML = generatedDir + "/newdrawables.xml";
        int countTotal = getCustomIconsCount(customIconCountXml);
        int countNew = countAll(newXML);
        int countFilterTotal = countAll(appFilter);
        int countFilterOld = readCountFilterOld(generatedDir);//19762; //tag11.4.6(21744)
        int countReused = countFilterTotal - countFilterOld - countNew;

        createChangelogXML(countTotal, countNew, countReused, changelogXml,generatedDir);
        createChangelogMd(countTotal, countNew, countReused, generatedDir,generatedDir);
        createChangelogTXT(countTotal, countNew, countReused, generatedDir,generatedDir,rootDir);

        if (newRelease) {
            //save countFilterTotal to file
            try {
                writeToFile(String.valueOf(countFilterTotal), generatedDir + "/countFilterTotal.txt");
                System.out.println("countFilterTotal saved to: " + generatedDir + "/countFilterTotal.txt");
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            }
        }
    }



    public static int readCountFilterOld(String generatedDir) {
        //read count from File
        try {
            Path path = Paths.get(generatedDir + "/countFilterTotal.txt");
            String content = new String(Files.readAllBytes(path)).strip();
            return Integer.parseInt(content);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

      return  0 ;
    }

    public static void readReleaseNotes(String generatedDir,StringBuilder output) {
        //read count from File
        try {
            Path path = Paths.get(generatedDir + "/additionalReleaseNotes.txt");
            String content = new String(Files.readAllBytes(path)).strip();
            if (!content.isEmpty()) {
                output.append("\n\n");
                output.append(content);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public static void readReleaseNotesLines(String generatedDir, StringBuilder output) {
        // Read content from the file
        try {
            Path path = Paths.get(generatedDir + "/additionalReleaseNotes.txt");
            String content = new String(Files.readAllBytes(path)).strip();

            // Split the content into lines
            String[] lines = content.split("\n");

            for (String line : lines) {
                if (!line.isEmpty()) {
                    output.append("        <item>");
                    output.append(line);
                    output.append("</item>");
                    output.append("\n"); // Add a new line after each item
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }


    public static void createChangelogMd(int countTotal, int countNew, int countReused, String changelogMd,String generatedDir) {
        StringBuilder output = new StringBuilder("* \uD83C\uDF89 **");
                output.append(countNew);
                output.append("** new and updated icons!\n");
                output.append("* \uD83D\uDCA1 Added support for **");
                output.append(countReused);
                output.append("** apps using existing icons.\n");
                output.append("* \uD83D\uDD25 **");
                output.append(countTotal);
                output.append("** icons in total!");
                readReleaseNotes(generatedDir,output);

        try {
            writeToFile(output.toString(), changelogMd + "/changelog.md");
            System.out.println("Changelog saved to: " + changelogMd + "/changelog.md");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void createChangelogTXT(int countTotal, int countNew, int countReused, String changelogMd,String generatedDir,String rootDir) {
        String TripleTYouNotes = rootDir +"/app/src/you/play/release-notes/en-US/default.txt";
        String TripleTNormalNotes = rootDir +"/app/src/normal/play/release-notes/en-US/default.txt";
        String TripleTBlackNotes = rootDir +"/app/src/black/play/release-notes/en-US/default.txt";
        String TripleTDayNightNotes = rootDir +"/app/src/dayNight/play/release-notes/en-US/default.txt";
        StringBuilder output = new StringBuilder("\uD83C\uDF89 ");
        output.append(countNew);
        output.append(" new and updated icons!\n");
        output.append("\uD83D\uDCA1 Added support for ");
        output.append(countReused);
        output.append(" apps using existing icons.\n");
        output.append("\uD83D\uDD25 ");
        output.append(countTotal);
        output.append(" icons in total!");
        readReleaseNotes(generatedDir,output);
        output.append("\n\n\uD83D\uDD17 You can find a detailed list of changes on our Github: https://github.com/Donnnno/Arcticons/releases  \uD83D\uDCC4");

        try {
            writeToFile(output.toString(), TripleTYouNotes);
            System.out.println("Changelog saved to: " + TripleTYouNotes);
            writeToFile(output.toString(), TripleTNormalNotes);
            System.out.println("Changelog saved to: " + TripleTNormalNotes);
            writeToFile(output.toString(), TripleTBlackNotes);
            System.out.println("Changelog saved to: " + TripleTBlackNotes);
            writeToFile(output.toString(), TripleTDayNightNotes);
            System.out.println("Changelog saved to: " + TripleTDayNightNotes);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void createChangelogXML(int countTotal, int countNew, int countReused, String changelogXml,String generatedDir){
        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "\n" +
                "    <!-- Leave empty if you don't want to show changelog date -->\n" +
                "    <string name=\"changelog_date\">");
        output.append(currentDate());
        output.append("</string>\n\n");
        output.append("    <!-- Changelog support html formatting\n");
        output.append("    * <b> for Bold\n");
        output.append("    * <i> for Italic\n");
        output.append("    * <u> for Underline\n");
        output.append("    * <a href=\"linkUrl\">Link Text</a> for links -->\n");
        output.append("    <string-array name=\"changelog\">\n");
        output.append("        <item>🎉 <b>");
        output.append(countNew);
        output.append("</b> new and updated icons!</item>\n");
        output.append("        <item>💡 Added support for <b>");
        output.append(countReused);
        output.append("</b> apps using existing icons.</item>\n");
        output.append("        <item>🔥 <b>");
        output.append(countTotal);
        output.append("</b> icons in total!</item>\n");
        readReleaseNotesLines(generatedDir,output);
        output.append("    </string-array>\n");
        output.append("</resources>");

        // Write the output to the file
        try {
            writeToFile(output.toString(), changelogXml);
            System.out.println("Changelog saved to: " + changelogXml);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

    }

    // Method to write a string to a file
    public static void writeToFile(String content, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes());
    }
    // Method to get the current date in "MMM dd, yyyy" format
    public static String currentDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return date.format(formatter);
    }

    public static int countAll(String drawableXml){
        try {
            // Path to the XML file
            Path xmlPath = Paths.get(drawableXml);

            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Create a DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(xmlPath.toFile());

            // Normalize the document (optional, but recommended)
            document.getDocumentElement().normalize();

            // Get all <item> nodes
            NodeList itemList = document.getElementsByTagName("item");

            // Count the <item> nodes
            int itemCount = itemList.getLength();

            // Output the count
            System.out.println("Number of <item> entries: " + itemCount);
            return itemCount;
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return 0;
    }

    public static int getCustomIconsCount(String XmlIconCount)  {
        try {
        // Path to the XML file
        Path xmlPath = Paths.get(XmlIconCount);
        // Create a DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Create a DocumentBuilder
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Parse the XML file
        Document document = builder.parse(xmlPath.toFile());
        // Normalize the document (optional, but recommended)
        document.getDocumentElement().normalize();
        // Get all <item> nodes
        NodeList itemList = document.getElementsByTagName("integer");
        // Iterate through the NodeList and retrieve the value of each <integer> element
            for (int i = 0; i < itemList.getLength(); i++) {
                // Get the individual node at index i
                Node node = itemList.item(i);

                // Ensure the node is an element (in case there are other types of nodes)
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // Cast to an Element
                    Element element = (Element) node;

                    // Retrieve the text content of the <integer> element
                    return Integer.parseInt(element.getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
        return 0;
    }

}
