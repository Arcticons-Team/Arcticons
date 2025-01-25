package com.donnnno.arcticons.helper;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.util.SVGConstants;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ImageCollageGenerator {
    public static void main(String[] args) {
        //String
        String rootDir = System.getProperty("user.dir");
        // Get the path of the root directory
        Path rootPath = Paths.get(rootDir);
        // Get the name of the root directory
        String rootDirName = rootPath.getFileName().toString();
        if (rootDirName.equals("preparehelper")) {
            rootDir = "..";
        }
        String valuesDir = rootDir+"/app/src/main/res/values";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String changelogXml = valuesDir +"/changelog.xml";
        String generatedDir = rootDir +"/generated";
        String IconsPath = rootDir+"/icons/white";       // Path to your icons folder
        // Assume there are 5 new icons in the XML
        generateReleaseImage( generatedDir + "/newdrawables.xml", IconsPath, generatedDir + "/releaseImage.webp");
    }

    public static void generateReleaseImage(String newIconsXml, String IconsPath, String releaseImagePath) {
        try {
            // Step 1: Parse the XML to extract icon names/paths
            File xmlFile = new File(newIconsXml);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            // Assume that the XML contains <icon> elements with paths to the icons
            NodeList itemNodes = doc.getElementsByTagName("item");
            List<String> iconNames = new ArrayList<>();

            for (int i = 0; i < itemNodes.getLength(); i++) {
                String drawable = itemNodes.item(i).getAttributes().getNamedItem("drawable").getNodeValue();
                iconNames.add(drawable); // Collect icon names
            }



            // Step 2: Load SVG icons and calculate collage dimensions
            List<BufferedImage> images = new ArrayList<>();
            int iconSize = 256; // Size for each icon, adjust as needed
            for (String iconName : iconNames) {
                File iconFile = new File(IconsPath + "/" + iconName + ".svg");
                BufferedImage iconImage = rasterize(iconFile);
                // Transcoding the SVG to a BufferedImage
                images.add(iconImage);
            }

            int iconSpacing = 10; // Spacing between icons, adjust as needed
            int pageSpacing = 200;
            int countNew = images.size();

            // Step 3: Create a collage image with black background (square-like size)
            int collageWidth = pageSpacing + (iconSize + iconSpacing ) * (int) Math.ceil(Math.sqrt(countNew)); // Square-like width
            int collageHeight =pageSpacing + (iconSize + iconSpacing ) * (int) Math.round(Math.sqrt(countNew)); // Square-like height
            BufferedImage collageImage = new BufferedImage(collageWidth, collageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = collageImage.createGraphics();

            // Set background color to black
            g2d.setColor(Color.decode("#111111")); // Sets the color to dark gray (#111111)
            g2d.fillRect(0, 0, collageWidth, collageHeight);

            // Step 4: Place images in the collage
            int xOffset = pageSpacing/2;
            int yOffset = pageSpacing/2;
            int iconsPerRow = (int) Math.ceil(Math.sqrt(countNew));

            for (int i = 0; i < images.size(); i++) {
                BufferedImage img = images.get(i);
                g2d.drawImage(img, xOffset, yOffset, iconSize, iconSize, null);
                xOffset += iconSize + iconSpacing;;

                // Move to the next row if the current row is filled
                if ((i + 1) % iconsPerRow == 0) {
                    xOffset = pageSpacing/2;
                    yOffset += iconSize + iconSpacing;
                }
            }

            g2d.dispose();

            // Step 5: Save the collage as a PNG
            File outputFile = new File(releaseImagePath);
            ImageIO.write(collageImage, "webp", outputFile);

            System.out.println("Collage saved to: " + releaseImagePath);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }

    public static BufferedImage rasterize(File svgFile) throws IOException {

        final BufferedImage[] imagePointer = new BufferedImage[1];

        TranscodingHints transcoderHints = new TranscodingHints();
        transcoderHints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, Boolean.FALSE);
        transcoderHints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION,
                SVGDOMImplementation.getDOMImplementation());
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                SVGConstants.SVG_NAMESPACE_URI);
        transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, "svg");

        try {
            TranscoderInput input = new TranscoderInput(new FileInputStream(svgFile));

            ImageTranscoder t = getImageTranscoder(imagePointer, transcoderHints);
            t.transcode(input, null);
        }
        catch (TranscoderException ex) {
            // Requires Java 6
            System.err.println("Couldn't convert " + svgFile);
            throw new IOException("Couldn't convert " + svgFile);
        }
        return imagePointer[0];
    }

    private static ImageTranscoder getImageTranscoder(BufferedImage[] imagePointer, TranscodingHints transcoderHints) {
        ImageTranscoder t = new ImageTranscoder() {

            @Override
            public BufferedImage createImage(int w, int h) {
                return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }

            @Override
            public void writeImage(BufferedImage image, TranscoderOutput out)
                    throws TranscoderException {
                imagePointer[0] = image;
            }
        };
        t.setTranscodingHints(transcoderHints);
        return t;
    }


}
