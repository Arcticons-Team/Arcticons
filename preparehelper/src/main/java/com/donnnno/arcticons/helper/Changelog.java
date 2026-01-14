package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class Changelog {

    public record ChangelogData(int total, int newIcons, int reused, String notes, String date) {}

    public static void main(String[] args) {
        String rootDir = System.getProperty("user.dir");
        if (Paths.get(rootDir).getFileName().toString().equals("preparehelper")) rootDir = "..";

        String valuesDir = rootDir + "/app/src/main/res/values";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String changelogXml = valuesDir + "/changelog.xml";
        String generatedDir = rootDir + "/generated";

        generateChangelogs(generatedDir, valuesDir + "/custom_icon_count.xml", appFilter, changelogXml, rootDir, false);
    }

    public static void generateChangelogs(String generatedDir, String customIconCountXml, String appFilter, String changelogXml, String rootDir, boolean newRelease) {
        int countTotal = getIntegerValue(customIconCountXml);
        int countNew = countTags(generatedDir + "/newdrawables.xml", "item");
        int countFilterTotal = countTags(appFilter, "item");
        int countFilterOld = readStoredCount(generatedDir + "/countFilterTotal.txt");
        int countReused = countFilterTotal - countFilterOld - countNew;

        ChangelogData data = new ChangelogData(
                countTotal, countNew, countReused,
                getReleaseNotes(generatedDir),
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        );

        // Generate all formats
        saveMarkdown(data, generatedDir + "/changelog.md");
        savePlayStoreNotes(data, rootDir);
        saveXml(data, changelogXml);

        if (newRelease) {
            safeWrite(String.valueOf(countFilterTotal), generatedDir + "/countFilterTotal.txt");
        }
    }

    private static void saveMarkdown(ChangelogData d, String path) {
        StringBuilder content = new StringBuilder();

        // Header bullet points
        content.append(String.format(Locale.ROOT,"* 🎉 **%d** new and updated icons!\n", d.newIcons));
        content.append(String.format(Locale.ROOT,"* 💡 Added support for **%d** apps using existing icons.\n", d.reused));
        content.append(String.format(Locale.ROOT,"* 🔥 **%d** icons in total!", d.total));

        // Process notes into bullet points
        if (!d.notes.isEmpty()) {
            for (String line : d.notes.split("\n")) {
                if (!line.isBlank()) {
                    content.append("\n* ").append(line.trim());
                }
            }
        }

        safeWrite(content.toString(), path);
    }

    private static void savePlayStoreNotes(ChangelogData d, String rootDir) {
        String content = String.format(Locale.ROOT,
                "🎉 %d new and updated icons!\n💡 Added support for %d apps using existing icons.\n🔥 %d icons in total!%s\n\n🔗 Detailed changes: https://github.com/Arcticons-Team/Arcticons/releases 📄",
                d.newIcons, d.reused, d.total, d.notes.isEmpty() ? "" : "\n\n" + d.notes);

        List<String> flavors = List.of("you", "normal", "black", "dayNight");
        for (String flavor : flavors) {
            String path = String.format(Locale.ROOT,"%s/app/src/%s/play/release-notes/en-US/default.txt", rootDir, flavor);
            safeWrite(content, path);
        }
    }

    private static void saveXml(ChangelogData d, String path) {
        StringBuilder items = new StringBuilder();
        items.append(String.format(Locale.ROOT,"        <item>🎉 <b>%d</b> new and updated icons!</item>\n", d.newIcons));
        items.append(String.format(Locale.ROOT,"        <item>💡 Added support for <b>%d</b> apps using existing icons.</item>\n", d.reused));
        items.append(String.format(Locale.ROOT,"        <item>🔥 <b>%d</b> icons in total!</item>\n", d.total));

        if (!d.notes.isEmpty()) {
            for (String line : d.notes.split("\n")) {
                if (!line.isBlank()) items.append("        <item>").append(line.trim()).append("</item>\n");
            }
        }

        String xml = String.format(Locale.ROOT,"""
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="changelog_date">%s</string>
                    <string-array name="changelog">
                %s    </string-array>
                </resources>""", d.date, items);
        safeWrite(xml, path);
    }

    // --- Helper Logic ---

    private static String getReleaseNotes(String generatedDir) {
        try {
            return Files.readString(Paths.get(generatedDir, "additionalReleaseNotes.txt")).strip();
        } catch (IOException e) { return ""; }
    }

    private static int readStoredCount(String path) {
        try {
            return Integer.parseInt(Files.readString(Paths.get(path)).strip());
        } catch (Exception e) { return 0; }
    }

    private static void safeWrite(String content, String pathStr) {
        try {
            Path path = Paths.get(pathStr);
            Files.createDirectories(path.getParent()); // Ensure folders exist
            Files.writeString(path, content);
            System.out.println("Saved: " + pathStr);
        } catch (IOException e) {
            System.err.println("Error writing " + pathStr + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static int countTags(String path, String tagName) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
            return doc.getElementsByTagName(tagName).getLength();
        } catch (Exception e) { return 0; }
    }

    private static int getIntegerValue(String path) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(path));
            return Integer.parseInt(doc.getElementsByTagName("integer").item(0).getTextContent());
        } catch (Exception e) { return 0; }
    }
}