package com.donnnno.arcticons.helper;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.regex.*;
import javax.imageio.ImageIO;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class WebpCreator {

    public static void createWebpIcons(String newIconsDir, String whiteDir, String blackDir, String exportWhiteDir, String exportBlackDir){
        try {
            final String ORIGINAL_STROKE = "stroke\\s*:\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))";
            final String ORIGINAL_STROKE_ALT = "stroke\\s*=\"\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))\"";
            final String ORIGINAL_FILL = "fill\\s*:\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))";
            final String ORIGINAL_FILL_ALT = "fill\\s*=\"\\s*(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))\"";
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

        Pattern strokePattern = Pattern.compile(stroke, Pattern.CASE_INSENSITIVE);
        Pattern fillPattern = Pattern.compile(fill, Pattern.CASE_INSENSITIVE);
        Pattern strokeAltPattern = Pattern.compile(strokeAlt, Pattern.CASE_INSENSITIVE);
        Pattern fillAltPattern = Pattern.compile(fillAlt, Pattern.CASE_INSENSITIVE);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir), "*.svg")) {
            for (Path path : stream) {
                String content = Files.readString(path);

                content = strokePattern.matcher(content).replaceAll(replaceStroke);
                content = fillPattern.matcher(content).replaceAll(replaceFill);
                content = strokeAltPattern.matcher(content).replaceAll(replaceStrokeAlt);
                content = fillAltPattern.matcher(content).replaceAll(replaceFillAlt);

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
                        String name = fileName.substring(0, fileName.length() - 4);

                        Path destinationPath = Paths.get(iconDir, fileName);
                        Files.copy(file, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                        for (Integer size : sizes) {
                            try {
                                File svgFile = file.toFile();
                                File webpFile = new File(exportDir + "/" + name + ".webp");

                                BufferedImage image = convertSvgToImage(svgFile, size, size);

                                File tmpPng = File.createTempFile("temp_icon", ".png");
                                ImageIO.write(image, "png", tmpPng);

                                try {
                                    convertPngToWebp(tmpPng, webpFile);
                                } catch (Exception e) {
                                    System.err.println("Error converting PNG to WebP: " + e.getMessage());
                                } finally {
                                    tmpPng.delete();
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
        PNGTranscoder transcoder = new PNGTranscoder();
        TranscoderInput input = new TranscoderInput(svgFile.toURI().toString());
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(baos);
        transcoder.transcode(input, output);

        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }

    private static void convertPngToWebp(File pngFile, File webpFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("cwebp", pngFile.getAbsolutePath(), "-o", webpFile.getAbsolutePath());
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("cwebp conversion failed with exit code " + exitCode);
        }
    }

    public static void removeSvg(String dir) {
        File directory = new File(dir);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir1, name) -> name.endsWith(".svg"));
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
