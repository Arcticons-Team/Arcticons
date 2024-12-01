package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
        String sourceDir = rootDir + "/icons/white";
        String resDir;
        String destDir;
        String xmlDir = rootDir+"/app/src/main/res/xml";
        String valuesDir = rootDir+"/app/src/main/res/values";
        String newXML = rootDir+"/generated/newdrawables.xml";
        String categoryGamesXml;
        String assetsDir;
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String drawableXml = xmlDir +"/drawable.xml";
        String changelogXml = valuesDir +"/changelog.xml";

        int countTotal = countAll(drawableXml);
        int countNew = countAll(newXML);
        int countFilterTotal = countAll(appFilter);
        int countFilterOld = 19762; //tag11.4.6(21744)
        int countReused = countFilterTotal-countFilterOld-countNew;

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
}
