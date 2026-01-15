package com.donnnno.arcticons.helper;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class WebpCreator {

    // 1. Capture the opening quote (if any) into Group 1 using ("?)
    // 2. Use \\1 at the end to ensure we only match a closing quote if we found an opening one.
    private static final String COLOR_REGEX = "(#FFFFFF|#ffffff|#fff|white|rgb\\(255,255,255\\)|rgba\\(255,255,255,1\\.\\d*\\))";

    // Pattern:  attribute  separator  (quote?)   color    (matching quote)
    private static final Pattern STROKE_PATTERN = Pattern.compile("stroke\\s*[:=]\\s*(\"?)\\s*" + COLOR_REGEX + "\\1", Pattern.CASE_INSENSITIVE);
    private static final Pattern FILL_PATTERN   = Pattern.compile("fill\\s*[:=]\\s*(\"?)\\s*" + COLOR_REGEX + "\\1", Pattern.CASE_INSENSITIVE);

    public static void createWebpIcons(String newIconsDir, String whiteDir, String blackDir, String exportWhiteDir, String exportBlackDir) {
        Path sourceDir = Path.of(newIconsDir);

        try (Stream<Path> files = Files.list(sourceDir)) {
            List<Path> svgFiles = files
                    .filter(p -> p.toString().endsWith(".svg"))
                    .toList();

            if (svgFiles.isEmpty()) {
                System.out.println("No SVG files found in " + newIconsDir);
                return;
            }

            System.out.println("Processing " + svgFiles.size() + " icons in Parallel...");
            AtomicInteger counter = new AtomicInteger(0);

            // Parallel stream handles both White and Black conversions simultaneously
            svgFiles.parallelStream().forEach(path -> {
                try {
                    String originalXml = Files.readString(path);
                    String fileName = path.getFileName().toString();
                    String nameWithoutExt = fileName.replace(".svg", "");

                    // 1. Process White Variant
                    processVariant(originalXml, "White",
                            Path.of(whiteDir, fileName),
                            Path.of(exportWhiteDir, nameWithoutExt + ".webp"));

                    // 2. Process Black Variant
                    processVariant(originalXml, "Black",
                            Path.of(blackDir, fileName),
                            Path.of(exportBlackDir, nameWithoutExt + ".webp"));

                    // Progress update
                    int c = counter.incrementAndGet();
                    if (c % 10 == 0) System.out.print("\rProcessed: " + c + "/" + svgFiles.size());

                    // Cleanup original SVG
                    Files.deleteIfExists(path);

                } catch (IOException e) {
                    System.err.println("Failed to process " + path + ": " + e.getMessage());
                }
            });

            System.out.println("\nCompleted.");

        } catch (IOException e) {
            System.err.println("Error listing files: " + e.getMessage());
        }
    }

    private static void processVariant(String xmlContent, String mode, Path svgDest, Path webpDest) {
        try {
            // Apply color transformation in memory
            String modifiedXml = switch (mode) {
                case "White" -> applyColors(xmlContent, "#fff");
                case "Black" -> applyColors(xmlContent, "#000");
                default -> xmlContent;
            };

            // Save modified SVG to its specific directory (e.g. /white/icon.svg)
            Files.writeString(svgDest, modifiedXml, StandardCharsets.UTF_8);

            // Convert in-memory XML directly to WebP (no need to read the file we just wrote)
            convertSvgStringToWebp(modifiedXml, webpDest);

        } catch (Exception e) {
            System.err.println("Error processing " + mode + " variant: " + e.getMessage());
        }
    }

    private static String applyColors(String content, String hexColor) {
        // Prepare replacement strings
        String cssStyle = "fill:" + hexColor;      // for style="fill:..."
        String xmlAttr  = "fill=\"" + hexColor + "\""; // for fill="..."

        // 1. Replace Fill
        content = FILL_PATTERN.matcher(content).replaceAll(matchResult -> {
            // If the match contains '=', it was likely an XML attribute (fill="#fff")
            // Otherwise, it was CSS inside a style tag (fill:#fff)
            return matchResult.group().contains("=") ? xmlAttr : cssStyle;
        });

        // 2. Replace Stroke (Update strings for stroke)
        String cssStroke = "stroke:" + hexColor;
        String xmlStroke = "stroke=\"" + hexColor + "\"";

        content = STROKE_PATTERN.matcher(content).replaceAll(matchResult -> {
            return matchResult.group().contains("=") ? xmlStroke : cssStroke;
        });

        return content;
    }

    private static void convertSvgStringToWebp(String svgXml, Path outputPath) throws IOException, TranscoderException {
        // Use Batik Transcoder
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 256f);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 256f);

        // Feed XML string directly to Transcoder (avoiding disk I/O)
        try (Reader reader = new StringReader(svgXml);
             ByteArrayOutputStream pngStream = new ByteArrayOutputStream()) {

            TranscoderInput input = new TranscoderInput(reader);
            TranscoderOutput output = new TranscoderOutput(pngStream);
            transcoder.transcode(input, output);

            // Convert PNG stream to BufferedImage
            try (ByteArrayInputStream bais = new ByteArrayInputStream(pngStream.toByteArray())) {
                BufferedImage image = ImageIO.read(bais);
                if (image != null) {
                    ImageIO.write(image, "webp", outputPath.toFile());
                }
            }
        }
    }
}