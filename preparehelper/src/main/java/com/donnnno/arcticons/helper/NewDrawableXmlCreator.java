package com.donnnno.arcticons.helper;

import static java.lang.System.getProperty;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

public class NewDrawableXmlCreator {

    public static void main(String[] args) {
        try {
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

            createNewDrawables(newIconsDir, generatedDir+"/newDrawables.xml", true);
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    public static void createNewDrawables(String svgDir, String newDrawablesPath, Boolean newRelease) throws IOException {
        // Regex for matching drawable="..." in XML files
        Pattern drawablePattern = Pattern.compile("drawable=\"([\\w_]+)\"");

        // Set to hold new drawables
        Set<String> newDrawables = new HashSet<>();

        if (!newRelease) {
            // Read existing drawables from the newDrawables file if it exists
            File newDrawablesFile = new File(newDrawablesPath);
            if (newDrawablesFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(newDrawablesPath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Matcher matcher = drawablePattern.matcher(line);
                        if (matcher.find()) {
                            newDrawables.add(matcher.group(1));
                        }
                    }
                }
            }
        }


        // Add drawables from SVG files in the specified directory
        try (Stream<Path> paths = Files.walk(Paths.get(svgDir))) {
            paths.filter(path -> path.toString().endsWith(".svg"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String nameWithoutExtension = fileName.substring(0, fileName.length() - 4);
                        newDrawables.add(nameWithoutExtension);
                    });
        }


        // Sort the drawables
        List<String> sortedNewDrawables = new ArrayList<>(newDrawables);
        Collections.sort(sortedNewDrawables);

        // Write the new drawables to the XML file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newDrawablesPath))) {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n\t<version>1</version>\n\t<category title=\"New\" />\n");
            String drawablePre = "\t<item drawable=\"";
            String drawableSuf = "\" />\n";
            for (String drawable : sortedNewDrawables) {
                writer.write(drawablePre + drawable + drawableSuf);
            }
            writer.write("</resources>\n");
        }

        // Print the number of new icons
        System.out.println("There are " + newDrawables.size() + " new icons");
    }

}
