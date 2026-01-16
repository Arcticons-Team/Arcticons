package com.donnnno.arcticons.helper;

import com.android.ide.common.vectordrawable.Svg2Vector;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class SvgConverter {

    public static void process(String sourceDir, String destDir, String flavor) {
        Path sourcePath = Path.of(sourceDir);
        Path destPath = Path.of(destDir);

        try {
            List<Path> svgFiles;
            try (Stream<Path> stream = Files.walk(sourcePath)) {
                svgFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".svg"))
                        .toList();
            }

            int totalFiles = svgFiles.size();
            if (totalFiles == 0) return;

            System.out.printf("Converting %d icons for flavor [%s]...%n", totalFiles, flavor);
            AtomicInteger processedCount = new AtomicInteger(0);

            // Parallel stream is efficient here for CPU-bound XML parsing
            svgFiles.parallelStream().forEach(svgFile -> {
                try {
                    Path relative = sourcePath.relativize(svgFile);
                    Path targetFile = destPath.resolve(relative);
                    Files.createDirectories(targetFile.getParent());
                    convertToVector(svgFile, targetFile, flavor);

                    int current = processedCount.incrementAndGet();
                    printProgressBar(current, totalFiles);
                } catch (IOException e) {
                    System.err.println("\nI/O Error on " + svgFile + ": " + e.getMessage());
                }
            });
            System.out.println("\nDone.");
        } catch (IOException e) {
            System.err.println("Fatal Error: " + e.getMessage());
        }
    }

    private static void convertToVector(Path svgSource, Path vectorTargetPath, String flavor) {
        Path targetFile = Path.of(getFileWithExtension(vectorTargetPath));

        try (var os = new ByteArrayOutputStream()) {
            synchronized (Svg2Vector.class) {
                Svg2Vector.parseSvgToXml(svgSource, os);
            }

            String xmlOutput = os.toString(StandardCharsets.UTF_8);

            switch (flavor) {
                case "you"      -> createAdaptive(xmlOutput, targetFile.toString());
                case "black"    -> createDrawable(xmlOutput, targetFile.toString(), "#000000");
                case "normal"   -> createDrawable(xmlOutput, targetFile.toString(), "#ffffff");
                case "dayNight" -> createDrawable(xmlOutput, targetFile.toString(), "@color/icon_color");
                default         -> throw new IllegalArgumentException("Unknown flavor: " + flavor);
            }
        } catch (Exception e) {
            System.err.println("\n Error converting " + svgSource.getFileName() + ": " + e.getMessage());
        }
    }

    private static void createDrawable(String xml, String resPath, String color) throws Exception {
        Document doc = DocumentHelper.parseText(xml);
        applyIconStyles(doc, color, "1");
        writeDocumentToFile(doc, resPath);
    }

    private static void createAdaptive(String xml, String resPath) throws Exception {
        Element svgRoot = DocumentHelper.parseText(xml).getRootElement();

        Document adaptiveDoc = DocumentHelper.createDocument();
        Element root = adaptiveDoc.addElement("adaptive-icon")
                .addAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");

        root.addElement("background").addAttribute("android:drawable", "@color/icon_background_color");

        Element inset = root.addElement("foreground").addElement("inset")
                .addAttribute("android:inset", "25%");

        inset.add(svgRoot.createCopy());

        applyIconStyles(adaptiveDoc, "@color/icon_color", "1.2");
        writeDocumentToFile(adaptiveDoc, resPath);
    }

    private static void applyIconStyles(Document doc, String color, String strokeWidth) {
        Element root = doc.getRootElement();
        // If it's a vector root, we can apply a global tint
        if (root.getName().equals("vector")) {
            updateAttribute(root, "android:tint", color);
        }
        updateAttributesRecursive(root, color, strokeWidth);
    }

    private static void updateAttributesRecursive(Element parent, String color, String strokeWidth) {
        for (Element el : parent.elements()) {
            if ("path".equals(el.getName())) {
                // Check for fillColor vs strokeColor
                // If a path has a stroke, we update it. If it has a fill, we update that.
                if (el.attribute("strokeColor") != null) {
                    updateAttribute(el, "android:strokeColor", color);
                }
                if (el.attribute("fillColor") != null) {
                    // Only update fill if it's not transparent
                    String currentFill = el.attributeValue("fillColor");
                    if (!"#00000000".equals(currentFill)) {
                        updateAttribute(el, "android:fillColor", color);
                    }
                }
                updateAttribute(el, "android:strokeWidth", strokeWidth);
            }
            updateAttributesRecursive(el, color, strokeWidth);
        }
    }

    private static void updateAttribute(Element el, String name, String value) {
        // dom4j handling of namespaces:
        // Use the attribute name without the prefix to find it, but add with prefix
        String baseName = name.contains(":") ? name.split(":")[1] : name;
        var attr = el.attribute(baseName);
        if (attr != null) {
            attr.setValue(value);
        } else {
            el.addAttribute(name, value);
        }
    }

    private static void printProgressBar(int current, int total) {
        int percent = (int) ((double) current / total * 100);
        String bar = "#".repeat(percent / 4) + "_".repeat(25 - (percent / 4));
        System.out.print("\r" + bar + " " + percent + "% (" + current + "/" + total + ")");
    }

    private static String getFileWithExtension(Path target) {
        String fileName = target.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');

        String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        return target.resolveSibling(baseName + ".xml").toAbsolutePath().toString();
    }

    private static void writeDocumentToFile(Document outDocument, String outputConfigPath) throws IOException {
        Path outputPath = Path.of(outputConfigPath);

        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding(StandardCharsets.UTF_8.name());
        format.setIndentSize(4); // Consistent indentation

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            XMLWriter xmlWriter = new XMLWriter(writer, format);
            xmlWriter.write(outDocument);
            xmlWriter.flush();
        }
    }
}