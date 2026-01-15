package com.donnnno.arcticons.helper;

import com.android.ide.common.vectordrawable.Svg2Vector;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class SvgConverter {
    private static Path sourceSvgPath;
    private static Path destinationVectorPath;
    private static String flavor;

    public static void process(String sourceDir, String destDir, String getFlavor) {
        sourceSvgPath = Path.of(sourceDir);
        destinationVectorPath = Path.of(destDir);
        flavor = getFlavor;

        try {
            // 1. Collect files first to get a total count for the progress bar
            List<Path> svgFiles;
            try (Stream<Path> stream = Files.walk(sourceSvgPath)) {
                svgFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".svg"))
                        .toList();
            }

            int totalFiles = svgFiles.size();
            if (totalFiles == 0) {
                System.out.println("No SVG files found.");
                return;
            }

            System.out.printf("Starting conversion of %d files using %d cores...%n",
                    totalFiles, Runtime.getRuntime().availableProcessors());

            AtomicInteger processedCount = new AtomicInteger(0);

            // 2. Parallel Processing
            svgFiles.parallelStream().forEach(svgFile -> {
                Path relative = sourceSvgPath.relativize(svgFile);
                Path targetFile = destinationVectorPath.resolve(relative);

                try {
                    Files.createDirectories(targetFile.getParent());
                    convertToVector(svgFile, targetFile);

                    // 3. Update progress
                    int current = processedCount.incrementAndGet();
                    printProgressBar(current, totalFiles);

                } catch (IOException e) {
                    System.err.println("\nFailed to create directory: " + targetFile.getParent());
                }
            });

            System.out.println("\nAll icons processed successfully.");

        } catch (IOException e) {
            System.err.println("Fatal I/O Error: " + e.getMessage());
        }
    }

    private static void printProgressBar(int current, int total) {
        int percent = (int) ((double) current / total * 100);
        String bar = "#".repeat(percent / 2) + "-".repeat(50 - (percent / 2));
        System.out.print("\r[" + bar + "] " + percent + "% (" + current + "/" + total + ")");
    }

    private static void convertToVector(Path svgSource, Path vectorTargetPath) {
        Path targetFile = Path.of(XMLhelper.getFileWithExtension(vectorTargetPath));

        try (var os = new ByteArrayOutputStream()) {
            synchronized (Svg2Vector.class) {
                Svg2Vector.parseSvgToXml(svgSource, os);
            }

            switch (flavor) {
                case "you"      -> createAdaptive(os, targetFile.toString());
                case "black"    -> createDrawable(os, targetFile.toString(), "#000000");
                case "normal"   -> createDrawable(os, targetFile.toString(), "#ffffff");
                case "dayNight" -> createDrawable(os, targetFile.toString(), "@color/icon_color");
                default         -> throw new IllegalArgumentException("Unknown flavor: " + flavor);
            }
        } catch (Exception e) {
            System.err.println("\n Error in " + svgSource.getFileName() + ": " + e.getMessage());
        }
    }

    private static void createDrawable(ByteArrayOutputStream os, String resPath, String color) throws Exception {
        Document doc = DocumentHelper.parseText(os.toString(StandardCharsets.UTF_8));

        applyIconStyles(doc, color, "1"); // Centralized styling logic
        XMLhelper.writeDocumentToFile(doc, resPath);
    }

    private static void createAdaptive(ByteArrayOutputStream os, String resPath) throws Exception {
        String fg = "@color/icon_color";
        String bg = "@color/icon_background_color";

        Element svgRoot = DocumentHelper.parseText(os.toString(StandardCharsets.UTF_8)).getRootElement();

        Document adaptiveDoc = DocumentHelper.createDocument();
        Element root = adaptiveDoc.addElement("adaptive-icon")
                .addAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");

        root.addElement("background").addAttribute("android:drawable", bg);
        root.addElement("foreground").addElement("inset")
                .addAttribute("android:inset", "25%")
                .add(svgRoot.createCopy()); // createCopy() ensures DOM integrity

        applyIconStyles(adaptiveDoc, fg, "1.2");
        XMLhelper.writeDocumentToFile(adaptiveDoc, resPath);
    }

    private static void applyIconStyles(Document doc, String color, String strokeWidth) {
        updateAttributes(doc.getRootElement(), color, strokeWidth);
        updateAttributeIfPresent(doc.getRootElement(), "android:tint", color);
    }

    private static void updateAttributes(Element parent, String color, String strokeWidth) {
        for (Object obj : parent.elements()) {
            if (obj instanceof Element el) {
                if ("path".equals(el.getName())) {
                    updateAttributeIfPresent(el, "android:strokeColor", color);
                    updateAttributeIfPresent(el, "android:fillColor", color);
                    updateAttributeIfPresent(el, "android:strokeWidth", strokeWidth);
                }
                updateAttributes(el, color, strokeWidth);
            }
        }
    }

    private static void updateAttributeIfPresent(Element el, String name, String value) {
        String localName = name.contains(":") ? name.split(":")[1] : name;
        var attr = el.attribute(localName);
        // Don't overwrite transparent/null colors
        if (attr == null) {
            el.addAttribute(name, value);
        } else if (!attr.getValue().equals("#00000000")) {
            attr.setValue(value);
        }
    }
}