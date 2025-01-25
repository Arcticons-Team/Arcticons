package com.donnnno.arcticons.helper;

import static java.lang.System.getProperty;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderException;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.imageio.ImageIO;


public class WebpCreator {

    public static void createWebpIcons(String newIconsDir, String whiteDir, String blackDir, String exportWhiteDir, String exportBlackDir){
        try {
        // Define original color matching patterns
        final String ORIGINAL_STROKE = "stroke\\s*:\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))";
        final String ORIGINAL_STROKE_ALT = "stroke\\s*=\"\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))\"";
        final String ORIGINAL_FILL = "fill\\s*:\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))";
        final String ORIGINAL_FILL_ALT = "fill\\s*=\"\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))\"";

        // Define replacement strings
        final String REPLACE_STROKE_WHITE = "stroke:#fff";
        final String REPLACE_STROKE_WHITE_ALT = "stroke=\"#fff\"";
        final String REPLACE_FILL_WHITE = "fill:#fff";
        final String REPLACE_FILL_WHITE_ALT = "fill=\"#fff\"";
        final String REPLACE_STROKE_BLACK = "stroke:#000";
        final String REPLACE_STROKE_BLACK_ALT = "stroke=\"#000\"";
        final String REPLACE_FILL_BLACK = "fill:#000";
        final String REPLACE_FILL_BLACK_ALT = "fill=\"#000\"";

        final List<Integer> SIZES = List.of(256);

        svgColors(newIconsDir, ORIGINAL_STROKE, ORIGINAL_FILL, ORIGINAL_STROKE_ALT, ORIGINAL_FILL_ALT, REPLACE_STROKE_WHITE, REPLACE_FILL_WHITE, REPLACE_STROKE_WHITE_ALT, REPLACE_FILL_WHITE_ALT);
        createIcons(SIZES, newIconsDir, exportWhiteDir, whiteDir, "White");
        svgColors(newIconsDir, ORIGINAL_STROKE, ORIGINAL_FILL, ORIGINAL_STROKE_ALT, ORIGINAL_FILL_ALT, REPLACE_STROKE_BLACK, REPLACE_FILL_BLACK, REPLACE_STROKE_BLACK_ALT, REPLACE_FILL_BLACK_ALT);
        createIcons(SIZES, newIconsDir, exportBlackDir, blackDir, "Black");
        removeSvg(newIconsDir);
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }



    public static void svgColors(String dir, String stroke, String fill, String strokeAlt, String fillAlt,
                                 String replaceStroke, String replaceFill, String replaceStrokeAlt, String replaceFillAlt) throws IOException {

        // Define regex patterns to match the colors (stroke and fill)
        Pattern strokePattern = Pattern.compile(stroke, Pattern.CASE_INSENSITIVE);
        Pattern fillPattern = Pattern.compile(fill, Pattern.CASE_INSENSITIVE);
        Pattern strokeAltPattern = Pattern.compile(strokeAlt, Pattern.CASE_INSENSITIVE);
        Pattern fillAltPattern = Pattern.compile(fillAlt, Pattern.CASE_INSENSITIVE);

        // Iterate through all SVG files in the directory
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir), "*.svg")) {
            for (Path path : stream) {
                String content = Files.readString(path);

                // Replace colors based on the provided rules
                content = strokePattern.matcher(content).replaceAll(replaceStroke);
                content = fillPattern.matcher(content).replaceAll(replaceFill);
                content = strokeAltPattern.matcher(content).replaceAll(replaceStrokeAlt);
                content = fillAltPattern.matcher(content).replaceAll(replaceFillAlt);

                // Write the updated content back to the file
                Files.writeString(path, content);
            }
        }
    }

    public static void createIcons(List<Integer> sizes, String dir, String exportDir, String iconDir, String mode) {
        System.out.println("Working on " + mode);

        try {
            Files.walkFileTree(Paths.get(dir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".svg")) {
                        String fileName = file.getFileName().toString();
                        String name = fileName.substring(0, fileName.length() - 4);  // Remove '.svg'

                        // Copy the file to iconDir
                        Path destinationPath = Paths.get(iconDir, fileName);
                        Files.copy(file, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                        for (Integer size : sizes) {
                            try {
                                // Convert the SVG directly to WebP
                                File svgFile = file.toFile();
                                File webpFile = new File(exportDir + "/" + name + ".webp");

                                // Transcode SVG to BufferedImage (with specified size)
                                BufferedImage image = convertSvgToImage(svgFile, size, size);

                                // Save as WebP using WebPImageIO
                                try {
                                    ImageIO.write(image, "webp",webpFile);
                                } catch (IOException e) {
                                    System.out.println("Error occurred: " + e.getMessage());
                                }

                            } catch (Exception e) {
                                System.err.println("Error processing " + file + ": " + e.getMessage());
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }

    private static BufferedImage convertSvgToImage(File svgFile, int width, int height) throws IOException, TranscoderException {
        // Initialize Batik's PNGTranscoder to handle SVG to PNG conversion
        PNGTranscoder transcoder = new PNGTranscoder();

        // Set the transcoder input (SVG file)
        TranscoderInput input = new TranscoderInput(svgFile.toURI().toString());
        // Set the output size (width and height)
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Use the image's graphics context to draw the resulting image
        TranscoderOutput output = new TranscoderOutput(baos);

        // Convert SVG to BufferedImage
        transcoder.transcode(input,output);
        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }

    public static void removeSvg(String dir) {
        File directory = new File(dir);

        // Check if the directory exists and is actually a directory
        if (directory.exists() && directory.isDirectory()) {
            // Get all files in the directory
            File[] files = directory.listFiles((dir1, name) -> name.endsWith(".svg"));

            // Iterate through the files and delete each one
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        System.out.println("Deleted: " + file.getName());
                    } else {
                        System.out.println("Failed to delete: " + file.getName());
                    }
                }
            }
        } else {
            System.out.println("Directory not found or not a directory.");
        }
    }

}
