package com.donnnno.arcticons.helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLCreator {
    private static List<String> drawables = new ArrayList<>();
    private static List<String> folder = new ArrayList<>();
    private static List<String> calendar = new ArrayList<>();
    private static List<String> google = new ArrayList<>();
    private static List<String> microsoft = new ArrayList<>();
    private static List<String> emoji = new ArrayList<>();
    private static List<String> numbers = new ArrayList<>();
    private static List<String> symbols = new ArrayList<>();
    private static List<String> number = new ArrayList<>();

    private static final Pattern drawablePattern = Pattern.compile("drawable=\"([\\w_]+)\"");

    public static void mergeNewDrawables(String pathXml, String pathNewXml, String assetPath, String iconsDir,
                                         String xmlDir, String appFilterPath) throws IOException {

        List<String> newDrawables = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(pathNewXml))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = drawablePattern.matcher(line);
                if (matcher.find()) {
                    newDrawables.add(matcher.group(1));
                }
            }
        }
        Collections.sort(newDrawables);

        // Collect existing drawables
        File iconsDirectory = new File(iconsDir);
        File[] files = iconsDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String newDrawable = fileName.substring(0, fileName.lastIndexOf('.'));

                if (!newDrawables.contains(newDrawable)) {
                    classifyDrawable(newDrawable);
                }
            }
        }

        int newIcons = newDrawables.size();
        System.out.println("There are " + newIcons + " new icons");

        // Remove duplicates and sort
        drawables = new ArrayList<>(new HashSet<>(drawables));
        Collections.sort(drawables);
        folder = new ArrayList<>(new HashSet<>(folder));
        Collections.sort(folder);
        calendar = new ArrayList<>(new HashSet<>(calendar));
        Collections.sort(calendar);
        google = new ArrayList<>(new HashSet<>(google));
        Collections.sort(google);
        microsoft = new ArrayList<>(new HashSet<>(microsoft));
        Collections.sort(microsoft);
        emoji = new ArrayList<>(new HashSet<>(emoji));
        Collections.sort(emoji);


        // Build output
        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n<version>1</version>\n\n\t<category title=\"New\" />\n\t");
        for (String newDrawable : newDrawables) {
            output.append("<item drawable=\"").append(newDrawable).append("\" />\n\t");
        }

        appendCategory(output, "Folders", folder);
        appendCategory(output, "Calendar", calendar);
        appendCategory(output, "Google", google);
        appendCategory(output, "Microsoft", microsoft);
        appendCategory(output, "Symbols", symbols);
        appendCategory(output, "Numbers", numbers);
        appendCategory(output, "0-9", number);

        // Iterate alphabet
        char letter = 'a';
        for (String entry : drawables) {
            if (!entry.startsWith(String.valueOf(letter))) {
                letter++;
                output.append("\n\t<category title=\"").append(Character.toUpperCase(letter)).append("\" />\n\t");
            }
            output.append("<item drawable=\"").append(entry).append("\" />\n\t");
        }
        output.append("\n</resources>");

        // Write to new_'filename'.xml in working directory
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathXml))) {
            writer.write(output.toString());
        }

        // Copy files
        copyFile(pathXml, assetPath);
        copyFile(appFilterPath, assetPath);
        copyFile(appFilterPath, xmlDir);

        // Remove the new xml file
        File newXmlFile = new File(pathNewXml);
        if (newXmlFile.exists()) {
            newXmlFile.delete();
        }
    }

    private static void appendCategory(StringBuilder output, String title, List<String> entries) {
        output.append("\n\t<category title=\"").append(title).append("\" />\n\t");
        for (String entry : entries) {
            output.append("<item drawable=\"").append(entry).append("\" />\n\t");
        }
    }

    private static void copyFile(String sourcePath, String destinationPath) throws IOException {
        Path source = Path.of(sourcePath);
        Path destination = Path.of(destinationPath);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void classifyDrawable(String newDrawable) {
        if (newDrawable.startsWith("folder_")) {
            folder.add(newDrawable);
        } else if (newDrawable.startsWith("calendar_")) {
            calendar.add(newDrawable);
        } else if (newDrawable.startsWith("google_")) {
            google.add(newDrawable);
        } else if (newDrawable.startsWith("microsoft_") || newDrawable.startsWith("xbox")) {
            microsoft.add(newDrawable);
        } else if (newDrawable.startsWith("emoji_")) {
            emoji.add(newDrawable);
        } else if (newDrawable.startsWith("letter_") || newDrawable.startsWith("number_")
                || newDrawable.startsWith("currency_") || newDrawable.startsWith("symbol_")) {
            symbols.add(newDrawable);
        } else if (newDrawable.startsWith("_")) {
            number.add(newDrawable);
        } else {
            drawables.add(newDrawable);
        }
    }
}
