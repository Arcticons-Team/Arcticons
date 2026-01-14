package com.donnnno.arcticons.helper;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NewDrawableXmlCreator {

    private static final Pattern DRAWABLE_PATTERN = Pattern.compile("drawable=\"([\\w_]+)\"");
    private static final Locale LOCALE = Locale.ROOT;

    public static void main(String[] args) {
        String rootDir = System.getProperty("user.dir");
        if (Paths.get(rootDir).getFileName().toString().equals("preparehelper")) {
            rootDir = "..";
        }

        String generatedDir = rootDir + "/generated";
        String newIconsDir = rootDir + "/newicons";

        try {
            createNewDrawables(newIconsDir, generatedDir + "/newdrawables.xml", true);
        } catch (IOException e) {
            System.err.println("Failed to create new drawables XML: " + e.getMessage());
        }
    }

    public static void createNewDrawables(String svgDir, String outputPath, boolean isNewRelease) throws IOException {
        // 1. Use a TreeSet to keep names unique and automatically sorted (A-Z)
        Set<String> newDrawables = new TreeSet<>();

        Path outPath = Paths.get(outputPath);

        // 2. If not a fresh release, load current "new" icons first
        if (!isNewRelease && Files.exists(outPath)) {
            try {
                String existingContent = Files.readString(outPath);
                Matcher matcher = DRAWABLE_PATTERN.matcher(existingContent);
                while (matcher.find()) {
                    newDrawables.add(matcher.group(1));
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not read existing newDrawables.xml");
            }
        }

        // 3. Scan the newicons directory for SVG files
        Path svgPath = Paths.get(svgDir);
        if (Files.exists(svgPath)) {
            try (Stream<Path> paths = Files.walk(svgPath)) {
                paths.filter(path -> path.toString().toLowerCase(LOCALE).endsWith(".svg"))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            // Better extension removal
                            String name = fileName.substring(0, fileName.lastIndexOf('.'));
                            newDrawables.add(name);
                        });
            }
        }

        // 4. Generate the XML output
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<resources>\n\t<version>1</version>\n\t<category title=\"New\" />\n");

        for (String drawable : newDrawables) {
            xml.append(String.format(LOCALE, "\t<item drawable=\"%s\" />\n", drawable));
        }
        xml.append("</resources>\n");

        // 5. Ensure parent directories exist and write file
        Files.createDirectories(outPath.getParent());
        Files.writeString(outPath, xml.toString());

        System.out.printf(LOCALE, "Successfully processed %d new icons.%n", newDrawables.size());
    }
}