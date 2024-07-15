package com.donnnno.arcticons.helper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
    private static List<String> newDrawables = new ArrayList<>();
    private static List<String> games = new ArrayList<>();
    private static List<String> drawables = new ArrayList<>();
    private static List<String> folder = new ArrayList<>();
    private static List<String> calendar = new ArrayList<>();
    private static List<String> google = new ArrayList<>();
    private static List<String> microsoft = new ArrayList<>();
    private static List<String> emoji = new ArrayList<>();
    private static List<String> numbers = new ArrayList<>();
    private static List<String> letters = new ArrayList<>();
    private static List<String> symbols = new ArrayList<>();
    private static List<String> number = new ArrayList<>();

    private static final Pattern drawablePattern = Pattern.compile("drawable=\"([\\w_]+)\"");

    public static void mergeNewDrawables(String pathXml, String pathNewXml,String CatGamePath, String assetPath, String iconsDir,
                                         String xmlDir, String appFilterPath) throws IOException {
        //Read new drawables from File and add to list
        try (BufferedReader reader = new BufferedReader(new FileReader(pathNewXml))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = drawablePattern.matcher(line);
                if (matcher.find()) {
                    newDrawables.add(matcher.group(1));
                }
            }
        }catch(FileNotFoundException e){
            System.out.println("XML file: games.xml not found");
        }
        //Read games from File and add to list
        try (BufferedReader reader = new BufferedReader(new FileReader(CatGamePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                games.add(line);
            }
        }catch(FileNotFoundException e){
            System.out.println("XML file: newdrawables.xml not found");
        }

        // Collect existing drawables
        File iconsDirectory = new File(iconsDir);
        File[] files = iconsDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String IconDrawable = fileName.substring(0, fileName.lastIndexOf('.'));

                if (!newDrawables.contains(IconDrawable) && !games.contains(IconDrawable)) {
                    classifyDrawable(IconDrawable);
                }
            }
        }
        // Remove duplicates and sort
        newDrawables = new ArrayList<>(new HashSet<>(newDrawables));
        Collections.sort(newDrawables);
        games = new ArrayList<>(new HashSet<>(games));
        Collections.sort(games);
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
        symbols = new ArrayList<>(new HashSet<>(symbols));
        Collections.sort(symbols);
        numbers = new ArrayList<>(new HashSet<>(numbers));
        Collections.sort(numbers);
        letters = new ArrayList<>(new HashSet<>(letters));
        Collections.sort(letters);
        number = new ArrayList<>(new HashSet<>(number));
        Collections.sort(number);
        emoji = new ArrayList<>(new HashSet<>(emoji));
        Collections.sort(emoji);


        // Build output
        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n<version>1</version>\n");

        appendCategory(output, "New", newDrawables);
        appendCategory(output, "Folders", folder);
        appendCategory(output, "Calendar", calendar);
        appendCategory(output, "Google", google);
        appendCategory(output, "Microsoft", microsoft);
        appendCategory(output, "Games", games);
        appendCategory(output, "Emoji", emoji);
        appendCategory(output, "Symbols", symbols);
        appendCategory(output, "Numbers", numbers);
        appendCategory(output, "Letters", letters);
        appendCategory(output, "0-9", number);
        appendCategory(output, "A-Z", drawables);

        output.append("\n</resources>");
        
        // Write to drawable.xml in res directory
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pathXml))) {
            writer.write(output.toString());
        }

        // Copy files
        copyFile(pathXml, assetPath+"/drawable.xml");
        copyFile(appFilterPath, assetPath+"/appfilter.xml");
        copyFile(appFilterPath, xmlDir+"/appfilter.xml");
        copyFile(appFilterPath, assetPath+"/icon_config.xml");
        copyFile(appFilterPath, xmlDir+"/icon_config.xml");
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
        } else if (newDrawable.startsWith("letter_")){
            letters.add(newDrawable);
        } else if (newDrawable.startsWith("currency_") || newDrawable.startsWith("symbol_")) {
            symbols.add(newDrawable);
        } else if (newDrawable.startsWith("number_")){
            numbers.add(newDrawable);
        } else if (newDrawable.startsWith("_")) {
            number.add(newDrawable);
        } else {
            drawables.add(newDrawable);
        }
    }
}
