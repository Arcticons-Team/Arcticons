package com.donnnno.arcticons.helper;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageCollageGenerator {

    private static final int ICON_SIZE = 256;
    private static final int SPACING = 10;
    private static final int PADDING = 100;
    private static final Color BG_COLOR = Color.decode("#111111");

    public static void main(String[] args) {
        String rootDir = System.getProperty("user.dir");
        if (Paths.get(rootDir).getFileName().toString().equals("preparehelper")) rootDir = "..";

        String generatedDir = rootDir + "/generated";
        String iconsPath = rootDir + "/icons/white";

        generateReleaseImage(generatedDir + "/newdrawables.xml", iconsPath, generatedDir + "/releaseImage.webp");
    }

    public static void generateReleaseImage(String newIconsXml, String iconsPath, String outputPath) {
        try {
            // 1. Parse XML
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(newIconsXml));
            NodeList itemNodes = doc.getElementsByTagName("item");
            List<String> iconNames = new ArrayList<>();
            for (int i = 0; i < itemNodes.getLength(); i++) {
                iconNames.add(itemNodes.item(i).getAttributes().getNamedItem("drawable").getNodeValue());
            }

            if (iconNames.isEmpty()) {
                System.out.println("No new icons found for collage.");
                return;
            }

            // 2. Rasterize SVGs (Using a set size for high quality)
            List<BufferedImage> images = new ArrayList<>();
            for (String name : iconNames) {
                File file = new File(iconsPath, name + ".svg");
                if (file.exists()) {
                    images.add(rasterize(file, ICON_SIZE));
                }
            }

            // 3. Calculate Grid
            int count = images.size();
            int columns = (int) Math.ceil(Math.sqrt(count));
            int rows = (int) Math.ceil((double) count / columns);

            int width = (columns * ICON_SIZE) + ((columns - 1) * SPACING) + (PADDING * 2);
            int height = (rows * ICON_SIZE) + ((rows - 1) * SPACING) + (PADDING * 2);

            // 4. Draw Collage
            BufferedImage collage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = collage.createGraphics();

            // Quality hints
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            g2d.setColor(BG_COLOR);
            g2d.fillRect(0, 0, width, height);

            int x = PADDING;
            int y = PADDING;

            for (int i = 0; i < images.size(); i++) {
                g2d.drawImage(images.get(i), x, y, ICON_SIZE, ICON_SIZE, null);

                if ((i + 1) % columns == 0) {
                    x = PADDING;
                    y += ICON_SIZE + SPACING;
                } else {
                    x += ICON_SIZE + SPACING;
                }
            }

            g2d.dispose();

            // 5. Save
            ImageIO.write(collage, "webp", new File(outputPath));
            System.out.printf(Locale.ROOT, "Generated collage with %d icons at: %s%n", count, outputPath);

        } catch (Exception e) {
            System.err.println("Collage Error: " + e.getMessage());
        }
    }

    public static BufferedImage rasterize(File svgFile, int size) throws IOException {
        final BufferedImage[] imagePointer = new BufferedImage[1];

        TranscodingHints hints = new TranscodingHints();
        hints.put(ImageTranscoder.KEY_WIDTH, (float) size);
        hints.put(ImageTranscoder.KEY_HEIGHT, (float) size);
        hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVGDOMImplementation.getDOMImplementation());
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
        hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");

        try (InputStream is = new FileInputStream(svgFile)) {
            TranscoderInput input = new TranscoderInput(is);
            ImageTranscoder t = new ImageTranscoder() {
                @Override
                public BufferedImage createImage(int w, int h) {
                    return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                }
                @Override
                public void writeImage(BufferedImage image, TranscoderOutput out) {
                    imagePointer[0] = image;
                }
            };
            t.setTranscodingHints(hints);
            t.transcode(input, null);
        } catch (TranscoderException e) {
            throw new IOException("Batik error: " + svgFile.getName(), e);
        }
        return imagePointer[0];
    }
}