package com.donnnno.arcticons.helper;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class XMLCreator {
    private static final Pattern DRAWABLE_PATTERN = Pattern.compile("drawable=\"([\\w_]+)\"");
    private static final Locale LOCALE = Locale.ROOT;

    public static void mergeNewDrawables(String valuesDir, String generatedDir, String assetPath, String iconsDir,
                                         String xmlDir, String appFilterPath) throws IOException {

        // 1. Load all available icon names from the directory first
        Set<String> availableIcons = new HashSet<>();
        Path iconsPath = Paths.get(iconsDir);
        if (Files.exists(iconsPath)) {
            try (Stream<Path> stream = Files.list(iconsPath)) {
                stream.map(p -> p.getFileName().toString())
                        .filter(name -> name.contains("."))
                        .map(name -> name.substring(0, name.lastIndexOf('.')))
                        .forEach(availableIcons::add);
            }
        }

        Set<String> newDrawables = new TreeSet<>();
        Set<String> games = new TreeSet<>();
        Set<String> system = new TreeSet<>();

        Map<String, Set<String>> categories = new LinkedHashMap<>();
        categories.put("New", newDrawables);
        categories.put("Folders", new TreeSet<>());
        categories.put("Calendar", new TreeSet<>());
        categories.put("Google", new TreeSet<>());
        categories.put("Microsoft", new TreeSet<>());
        categories.put("Games", games);
        categories.put("System", system);
        categories.put("Emoji", new TreeSet<>());
        categories.put("Symbols", new TreeSet<>());
        categories.put("Numbers", new TreeSet<>());
        categories.put("Letters", new TreeSet<>());
        categories.put("0-9", new TreeSet<>());
        categories.put("A-Z", new TreeSet<>());

        // 2. Load existing data, strictly filtering against availableIcons
        loadDrawablesFromXml(Paths.get(generatedDir, "newdrawables.xml"), newDrawables, availableIcons);

        Path pathGames = Paths.get(generatedDir, "games.xml");
        loadLinesToSet(pathGames, games, availableIcons);

        Path pathSystem = Paths.get(generatedDir, "system.xml");
        loadLinesToSet(pathSystem, system, availableIcons);

        // 3. Classify all icons (using the already loaded set)
        availableIcons.forEach(name -> classify(name, categories));

        // Save total count
        int totalIcons = categories.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
                .size();

        createCustomIconCountFile(Paths.get(valuesDir, "custom_icon_count.xml"), totalIcons);

        // Note: We write back the filtered lists. This cleans up the source files if items were deleted.
        Files.write(pathGames, games);
        Files.write(pathSystem, system);

        // Build XML Output
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n<version>1</version>\n");
        categories.forEach((title, items) -> {
            if (!items.isEmpty()) {
                xml.append(String.format(LOCALE, "\n\t<category title=\"%s\" />\n", title));
                for (String item : items) {
                    xml.append(String.format(LOCALE, "\t<item drawable=\"%s\" />\n", item));
                }
            }
        });
        xml.append("</resources>");

        // Write and sync files
        Path drawableXml = Paths.get(xmlDir, "drawable.xml");
        Files.writeString(drawableXml, xml.toString());

        syncFiles(drawableXml, Path.of(assetPath, "drawable.xml"));
        syncFiles(Path.of(appFilterPath), Path.of(assetPath, "appfilter.xml"),
                Path.of(xmlDir, "appfilter.xml"), Path.of(assetPath, "icon_config.xml"),
                Path.of(xmlDir, "icon_config.xml"));
    }

    private static void classify(String name, Map<String, Set<String>> categories) {
        if (name.startsWith("folder_")) categories.get("Folders").add(name);
        else if (name.startsWith("calendar_")) categories.get("Calendar").add(name);
        else if (name.startsWith("google_")) categories.get("Google").add(name);
        else if (name.startsWith("microsoft_") || name.startsWith("xbox")) categories.get("Microsoft").add(name);
        else if (name.startsWith("emoji_")) categories.get("Emoji").add(name);
        else if (name.startsWith("letter_")) categories.get("Letters").add(name);
        else if (name.startsWith("currency_") || name.startsWith("symbol_")) categories.get("Symbols").add(name);
        else if (name.startsWith("number_")) categories.get("Numbers").add(name);
        else if (name.startsWith("_")) categories.get("0-9").add(name);
        else categories.get("A-Z").add(name);
    }

    private static void loadDrawablesFromXml(Path path, Set<String> target, Set<String> validIcons) {
        if (!Files.exists(path)) return;
        try {
            String content = Files.readString(path);
            Matcher m = DRAWABLE_PATTERN.matcher(content);
            while (m.find()) {
                String drawableName = m.group(1);
                // Only add if it actually exists in the validIcons set
                if (validIcons.contains(drawableName)) {
                    target.add(drawableName);
                }
            }
        } catch (IOException ignored) {}
    }

    private static void loadLinesToSet(Path path, Set<String> target, Set<String> validIcons) {
        if (!Files.exists(path)) return;
        try (Stream<String> lines = Files.lines(path)) {
            lines.filter(l -> !l.isBlank())
                    .filter(validIcons::contains) // Only add if it exists
                    .forEach(target::add);
        } catch (IOException ignored) {}
    }

    private static void createCustomIconCountFile(Path path, int count) throws IOException {
        String xml = String.format(LOCALE, """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                   <integer name="custom_icons_count">%d</integer>
                </resources>""", count);
        Files.writeString(path, xml);
    }

    private static void syncFiles(Path source, Path... destinations) throws IOException {
        for (Path dest : destinations) {
            Files.createDirectories(dest.getParent());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}