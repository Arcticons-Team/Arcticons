package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Checks {

    // --- REGEX PATTERNS ---
    private static final Pattern XML_PATTERN = Pattern.compile("((<!--.*-->)|(<(item|calendar) component=\"(ComponentInfo\\{.*/.*}|:[A-Z_]*)\" (drawable|prefix)=\".*\"\\s?/>)|(^\\s*$)|(</?resources>)|(<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>))");
    private static final Pattern STROKE_STRING_PATTERN = Pattern.compile("(?<strokestr>stroke-width(?:=\"|: ?))(?<number>\\d*(?:.\\d+)?)(?=[p\"; }/])");
    private static final Pattern STROKE_COLOR_PATTERN = Pattern.compile("stroke(?:=\"|:)(?:rgb[^a]|#).*?(?=[\"; ])");
    private static final Pattern FILL_COLOR_PATTERN = Pattern.compile("fill(?:=\"|:)(?:rgb[^a]|#).*?(?=[\"; ])");
    private static final Pattern STROKE_OPACITY_PATTERN = Pattern.compile("stroke-opacity(?:=\"|:).*?(?=[\"; ])");
    private static final Pattern FILL_OPACITY_PATTERN = Pattern.compile("fill-opacity(?:=\"|:).*?(?=[\"; ])");
    private static final Pattern STROKE_RGBA_PATTERN = Pattern.compile("stroke(?:=\"|:)rgba.*?(?=[\"; ])");
    private static final Pattern FILL_RGBA_PATTERN = Pattern.compile("fill(?:=\"|:)rgba.*?(?=[\"; ])");
    private static final Pattern STROKE_WIDTH_PATTERN = Pattern.compile("stroke-width(?:=\"|:) ?.*?(?=[\"; ])");
    private static final Pattern LINE_CAP_PATTERN = Pattern.compile("stroke-linecap(?:=\"|:).*?(?=[\";}])");
    private static final Pattern LINE_JOIN_PATTERN = Pattern.compile("stroke-linejoin(?:=\"|:).*?(?=[\";}])");

    // --- VALIDATION SETS ---
    private static final Set<String> validColors = Set.of("stroke:#ffffff", "stroke:#fff", "stroke:#FFFFFF", "stroke=\"#ffffff", "stroke=\"#fff", "stroke=\"#FFFFFF", "stroke=\"white", "fill:#ffffff", "fill:#fff", "fill:#FFFFFF", "fill=\"#ffffff", "fill=\"#fff", "fill=\"#FFFFFF");
    private static final Set<String> validOpacities = Set.of("stroke-opacity=\"0", "stroke-opacity=\"0%", "stroke-opacity=\"1", "stroke-opacity=\"100%", "stroke-opacity:1", "stroke-opacity:0", "fill-opacity=\"0", "fill-opacity=\"0%", "fill-opacity=\"1", "fill-opacity=\"100%", "fill-opacity:1", "fill-opacity:0");
    private static final Set<String> validStrokeWidth = Set.of("stroke-width:1", "stroke-width:1px", "stroke-width:0px", "stroke-width:0", "stroke-width=\"1", "stroke-width=\"0", "stroke-width: 0px", "stroke-width: 1px");
    private static final Set<String> validLineJoinCap = Set.of("stroke-linejoin:round", "stroke-linejoin=\"round", "stroke-linejoin: round", "stroke-linecap:round", "stroke-linecap=\"round", "stroke-linecap: round");

    private static final Map<Pattern, Set<String>> ATTRIBUTE_VALIDATION_MAP = Map.of(
            STROKE_COLOR_PATTERN, validColors,
            FILL_COLOR_PATTERN, validColors,
            STROKE_OPACITY_PATTERN, validOpacities,
            FILL_OPACITY_PATTERN, validOpacities,
            STROKE_WIDTH_PATTERN, validStrokeWidth,
            LINE_CAP_PATTERN, validLineJoinCap,
            LINE_JOIN_PATTERN, validLineJoinCap
    );

    private static final List<Pattern> RGBA_PATTERNS = List.of(STROKE_RGBA_PATTERN, FILL_RGBA_PATTERN);

    // --- CENTRALIZED VIOLATION LOG ---
    private static final List<Violation> violations = Collections.synchronizedList(new ArrayList<>());

    public record Violation(String category, String source, String detail) {
    }

    public static void main(String[] args) {
        String rootDir = System.getProperty("user.dir");
        if (Paths.get(rootDir).getFileName().toString().equals("preparehelper")) rootDir = "..";

        String sourceDir = rootDir + "/icons/white";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String newIconsDir = rootDir + "/newicons";

        startChecks(appFilter, sourceDir, newIconsDir);
    }

    public static void startChecks(String appFilter, String sourceDir, String newIconsDir) {
        violations.clear();
        Document appFilterDoc = parseXml(appFilter);

        checkXml(appFilter);
        if (appFilterDoc != null) {
            checkDuplicateEntries(appFilterDoc);
            checkMissingDrawables(appFilterDoc, sourceDir, newIconsDir);
        }
        checkSVGFiles(newIconsDir);

        if (!violations.isEmpty()) {
            reportViolations();
            System.exit(1);
        } else {
            System.out.println("No violations found. All checks passed!");
        }
    }

    private static void reportViolations() {
        System.err.println("\n" + "=".repeat(60));
        System.err.println(" BUILD FAILED: VIOLATIONS DETECTED");
        System.err.println("=".repeat(60));

        violations.stream()
                .collect(Collectors.groupingBy(Violation::category))
                .forEach((category, list) -> {
                    System.err.println("\n[ " + category.toUpperCase() + " ]");
                    list.forEach(v -> System.err.println("  -> " + v.source() + ": " + v.detail()));
                });

        System.err.println("\n" + "=".repeat(60));
        System.err.printf("Total Failures: %d\n", violations.size());
    }

    // --- CHECK LOGIC ---

    private static void checkXml(String path) {
        try (var lines = Files.lines(Paths.get(path))) {
            lines.filter(line -> !line.isBlank() && !XML_PATTERN.matcher(line).find())
                    .forEach(line -> violations.add(new Violation("XML Syntax", path, "Invalid line format: " + line.trim())));
        } catch (IOException e) {
            violations.add(new Violation("System", path, "Could not read XML file"));
        }
    }

    private static void checkDuplicateEntries(Document doc) {
        Set<String> seen = new HashSet<>();
        NodeList nodeList = doc.getElementsByTagName("item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            if (item.getAttribute("prefix").isEmpty()) {
                String component = item.getAttribute("component");
                if (!seen.add(component)) {
                    violations.add(new Violation("Duplicate Entry", "appfilter.xml", component));
                }
            }
        }
    }

    private static void checkMissingDrawables(Document doc, String whiteDir, String otherDir) {
        NodeList nodeList = doc.getElementsByTagName("item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            if (item.getAttribute("prefix").isEmpty()) {
                String drawable = item.getAttribute("drawable");
                boolean existsInWhite = Files.exists(Paths.get(whiteDir, drawable + ".svg"));
                boolean existsInOther = Files.exists(Paths.get(otherDir, drawable + ".svg"));

                if (!existsInWhite && !existsInOther) {
                    violations.add(new Violation("Missing Drawable", item.getAttribute("component"), drawable + ".svg"));
                }
            }
        }
    }

    private static void checkSVGFiles(String dir) {
        Path folder = Paths.get(dir);
        try (var stream = Files.list(folder)) {
            stream.filter(path -> path.toString().endsWith(".svg"))
                    .parallel()
                    .forEach(entry -> {
                        try {
                            String fileName = entry.getFileName().toString();
                            String content = Files.readString(entry);

                            // Auto-fix stroke widths
                            String updatedContent = applyAutoFixes(content);

                            // Validate remaining attributes
                            validateAttributes(fileName, updatedContent);

                            if (!content.equals(updatedContent)) {
                                Files.writeString(entry, updatedContent);
                            }
                        } catch (IOException e) {
                            violations.add(new Violation("IO Error", entry.toString(), "Failed to process SVG"));
                        }
                    });
        } catch (IOException e) {
            violations.add(new Violation("System", dir, "Could not access SVG directory"));
        }
    }

    private static String applyAutoFixes(String content) {
        Matcher matcher = STROKE_STRING_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(replaceStroke(matcher)));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static void validateAttributes(String fileName, String content) {
        ATTRIBUTE_VALIDATION_MAP.forEach((pattern, validSet) -> {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                String attr = matcher.group().trim();
                if (!validSet.contains(attr)) {
                    violations.add(new Violation("SVG Attribute", fileName, "Invalid value: " + attr));
                }
            }
        });

        for (Pattern rgbaPattern : RGBA_PATTERNS) {
            Matcher matcher = rgbaPattern.matcher(content);
            while (matcher.find()) {
                String rgba = matcher.group();
                if (!isValidRGBA(rgba)) {
                    violations.add(new Violation("SVG RGBA", fileName, "Invalid color: " + rgba));
                }
            }
        }
    }

    private static boolean isValidRGBA(String rgba) {
        String clean = rgba.replaceAll("\\s", "");
        return clean.contains("rgba(255,255,255,1)") || clean.contains(",0)") || clean.contains(",0.0)");
    }

    private static String replaceStroke(Matcher matcher) {
        String strokeStr = matcher.group("strokestr");
        try {
            double strokeWidth = Double.parseDouble(matcher.group("number"));
            if (strokeWidth > 0.9 && strokeWidth < 1.2) return strokeStr + "1";
            if (strokeWidth >= 0 && strokeWidth < 0.3) return strokeStr + "0";
        } catch (NumberFormatException ignored) {
        }
        return strokeStr + matcher.group("number");
    }

    private static Document parseXml(String path) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(path));
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            violations.add(new Violation("XML Parse", path, "Malformed XML structure"));
            return null;
        }
    }
}